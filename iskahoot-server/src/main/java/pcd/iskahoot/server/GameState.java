package pcd.iskahoot.server;

import pcd.iskahoot.common.Mensagem;
import pcd.iskahoot.common.Pergunta;
import pcd.iskahoot.common.TipoPergunta;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class GameState {

    public enum GameStatus {
        ESPERA_JOGADORES,
        A_DECORRER,
        FINALIZADO
    }

    public static class RespostaComTimestamp {
        final int resposta;
        final long timestamp;
        final int multiplier;

        RespostaComTimestamp(int resposta, int multiplier) {
            this.resposta = resposta;
            this.timestamp = System.nanoTime();
            this.multiplier = multiplier;
        }
    }
    private final String id_sala;
    private final List<Pergunta> quiz;
    private final int maxEquipas;
    private final int maxJogadoresPorEquipa;
    private final int tempoPorPergunta;
    private ModifiedCountdownLatch currentLatch;
    private TeamBarrier currentBarrier;
    private volatile GameStatus estado;
    private int indicePerguntaAtual;
    private TipoPergunta tipoRondaAtual;
        
    // Dados do jogo
    private final Map<String, String> jogadoresPorEquipa = new ConcurrentHashMap<>();
    private final Map<String, Integer> placar = new ConcurrentHashMap<>();
    private final Map<String, RespostaComTimestamp> respostasDaRonda = new ConcurrentHashMap<>();
    private final Set<String> equipasQueJaResponderam = new HashSet<>();
    
    // Outputs para Broadcast
    private final Map<String, ObjectOutputStream> playerOutputStreams = new ConcurrentHashMap<>();

    public GameState(String id_sala, List<Pergunta> quiz, GameConfig config) {
        this.id_sala = id_sala;
        this.quiz = new ArrayList<>(quiz); 
        this.maxEquipas = config.numEquipas;
        this.maxJogadoresPorEquipa = config.jogadoresPorEquipa;
        this.estado = GameStatus.ESPERA_JOGADORES;
        this.indicePerguntaAtual = -1;
        this.tempoPorPergunta = config.tempoPorPergunta;
    }

    // --- MÉTODOS DE ESTADO ---

    public boolean isSalaCheia() {
        return getTotalJogadores() == (maxEquipas * maxJogadoresPorEquipa);
    }

    public synchronized void iniciarSincronizacao() {
        this.currentLatch = null;
        this.currentBarrier = null;

        if (tipoRondaAtual == TipoPergunta.INDIVIDUAL) {
            // Latch: 2x multiplier, 2 bonuses, time from config, total players
            this.currentLatch = new ModifiedCountdownLatch(2, 2, tempoPorPergunta, getTotalJogadores());
        } else {
            // Barrier: Total teams, time from config
            this.currentBarrier = new TeamBarrier(this.maxEquipas, tempoPorPergunta);
        }
    }

    public void esperarPeloFimDaRonda() throws InterruptedException {
        if (currentLatch != null) {
            currentLatch.await();
        } else if (currentBarrier != null) {
            currentBarrier.await();
            // Barrier might have a running timer we want to kill immediately after waking up
            currentBarrier.cancelTimer(); 
        }
    }

    public synchronized boolean adicionarJogador(String idJogador, String idEquipa) {
        if (this.estado != GameStatus.ESPERA_JOGADORES) return false;
        if (jogadoresPorEquipa.containsKey(idJogador)) return false;

        long numJogadoresNaEquipa = jogadoresPorEquipa.values().stream().filter(e -> e.equals(idEquipa)).count();
        
        if (placar.containsKey(idEquipa)) { 
            if (numJogadoresNaEquipa >= maxJogadoresPorEquipa) return false;
        } else { 
            if (placar.size() >= maxEquipas) return false;
        }

        jogadoresPorEquipa.put(idJogador, idEquipa);
        placar.putIfAbsent(idEquipa, 0);
        return true;
    }

    // Chamado pelo ClientHandler quando recebe input
    public void registarResposta(String idJogador, int resposta) {
        if (this.estado == GameStatus.A_DECORRER) {
            if (respostasDaRonda.containsKey(idJogador)) {
                return;
            }
            int mult = 1;

            // 1. Interaction with Concurrency Objects
            if (tipoRondaAtual == TipoPergunta.INDIVIDUAL && currentLatch != null) {
                // Returns 2 if fast, 1 otherwise
                mult = currentLatch.countdown(); 
            } else if (tipoRondaAtual == TipoPergunta.EQUIPA && currentBarrier != null) {
                String idEquipa = jogadoresPorEquipa.get(idJogador);
                if (idEquipa != null && equipasQueJaResponderam.add(idEquipa)) {
                    currentBarrier.arrive();
                }
            }

            // 2. Store the answer with the calculated multiplier
            RespostaComTimestamp novaResposta = new RespostaComTimestamp(resposta, mult);
            respostasDaRonda.putIfAbsent(idJogador, novaResposta);
            
            System.out.println("[GameState] Resposta de " + idJogador + " (mult=" + mult + ")");
        }
    }

    // Chamado pelo GameLoop para preparar a próxima ronda
    public void avancarConfiguracaoRonda() {
        this.respostasDaRonda.clear();
        this.equipasQueJaResponderam.clear();
        this.indicePerguntaAtual++;
        
        // Alterna o tipo de ronda
        if (this.tipoRondaAtual == null || this.tipoRondaAtual == TipoPergunta.EQUIPA) {
            this.tipoRondaAtual = TipoPergunta.INDIVIDUAL;
        } else {
            this.tipoRondaAtual = TipoPergunta.EQUIPA;
        }
    }

    // --- CÁLCULO DE PONTOS (LÓGICA DE NEGÓCIO) ---

    public void processarResultadosDaRonda() {
        Pergunta pergunta = getPerguntaAtual();
        if (pergunta == null) return;

        if (this.tipoRondaAtual == TipoPergunta.INDIVIDUAL) {
            processarResultadosIndividuais(pergunta);
        } else {
            processarResultadosEquipa(pergunta);
        }
    }

    private void processarResultadosIndividuais(Pergunta pergunta) {
        int correct = pergunta.getCorrect();
        int points = pergunta.getPoints();

        // Iterate through all answers
        for (Map.Entry<String, RespostaComTimestamp> entry : respostasDaRonda.entrySet()) {
            // Check correctness
            if (entry.getValue().resposta == correct) {
                String idJogador = entry.getKey();
                String idEquipa = jogadoresPorEquipa.get(idJogador);
                
                // Calculate: Base Points * Multiplier (1 or 2)
                int pontosGanhos = points * entry.getValue().multiplier;
                
                // Add to team score
                placar.merge(idEquipa, pontosGanhos, Integer::sum);
            }
        }
    }

    private void processarResultadosEquipa(Pergunta pergunta) {
        // Agrupa equipas
        Map<String, List<String>> equipas = new HashMap<>();
        jogadoresPorEquipa.forEach((k, v) -> equipas.computeIfAbsent(v, x -> new ArrayList<>()).add(k));

        int respostaCorreta = pergunta.getCorrect();
        int pontosBase = pergunta.getPoints();

        for (Map.Entry<String, List<String>> entry : equipas.entrySet()) {
            String idEquipa = entry.getKey();
            List<String> membros = entry.getValue();
            boolean todosAcertaram = true;
            boolean alguemAcertou = false;
            
            for (String membro : membros) {
                RespostaComTimestamp resp = respostasDaRonda.get(membro);
                if (resp == null || resp.resposta != respostaCorreta) todosAcertaram = false;
                if (resp != null && resp.resposta == respostaCorreta) alguemAcertou = true;
            }

            if (todosAcertaram) placar.merge(idEquipa, pontosBase * 2, Integer::sum);
            else if (alguemAcertou) placar.merge(idEquipa, pontosBase, Integer::sum);
        }
    }

    // --- COMUNICAÇÃO ---

    public synchronized void addPlayerOutputStream(String idJogador, ObjectOutputStream out) {
        playerOutputStreams.put(idJogador, out);
    }

    public synchronized void removePlayerOutputStream(String idJogador) {
        playerOutputStreams.remove(idJogador);
    }

    public synchronized void broadcastMessage(Mensagem message, String excludeUsername) {
        playerOutputStreams.forEach((user, out) -> {
            if (!user.equals(excludeUsername)) {
                try {
                    out.writeObject(message);
                    out.flush();
                    out.reset();
                } catch (IOException e) {
                    System.err.println("Erro broadcast para " + user);
                }
            }
        });
    }


    @Override
    public String toString() {
        return String.format("Sala: %-8s | Estado: %-16s | Jogadores: %d/%d", 
            id_sala, 
            estado, 
            getTotalJogadores(), 
            (maxEquipas * maxJogadoresPorEquipa)
        );
    }

    // --- GETTERS ---
    public String getIdSala() { return id_sala; }
    public GameStatus getEstado() { return estado; }
    public void setEstado(GameStatus estado) { this.estado = estado; }
    public int getTotalJogadores() { return jogadoresPorEquipa.size(); }
    public Map<String, Integer> getPlacar() { return placar; }
    public Pergunta getPerguntaAtual() {
        if (indicePerguntaAtual >= 0 && indicePerguntaAtual < quiz.size()) return quiz.get(indicePerguntaAtual);
        return null;
    }
    public TipoPergunta getTipoRondaAtual() { return tipoRondaAtual; }
    public boolean jogoTerminou() { return indicePerguntaAtual >= quiz.size(); }
    public List<String> getPlayersInGame() { return new ArrayList<>(jogadoresPorEquipa.keySet()); }
    public int getTempoPorPergunta() { return tempoPorPergunta; }
}