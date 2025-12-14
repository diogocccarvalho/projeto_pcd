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
        // Futuro: int bonusMultiplier;

        RespostaComTimestamp(int resposta) {
            this.resposta = resposta;
            this.timestamp = System.nanoTime();
        }
    }

    private final String id_sala;
    private final List<Pergunta> quiz;
    private final int maxEquipas;
    private final int maxJogadoresPorEquipa;

    private volatile GameStatus estado;
    private int indicePerguntaAtual;
    private TipoPergunta tipoRondaAtual;

    // Dados do jogo
    private final Map<String, String> jogadoresPorEquipa = new ConcurrentHashMap<>();
    private final Map<String, Integer> placar = new ConcurrentHashMap<>();
    private final Map<String, RespostaComTimestamp> respostasDaRonda = new ConcurrentHashMap<>();
    
    // Outputs para Broadcast
    private final Map<String, ObjectOutputStream> playerOutputStreams = new ConcurrentHashMap<>();

    public GameState(String id_sala, List<Pergunta> quiz, GameConfig config) {
        this.id_sala = id_sala;
        this.quiz = new ArrayList<>(quiz); 
        this.maxEquipas = config.numEquipas;
        this.maxJogadoresPorEquipa = config.jogadoresPorEquipa;
        this.estado = GameStatus.ESPERA_JOGADORES;
        this.indicePerguntaAtual = -1;
    }

    // --- MÉTODOS DE ESTADO ---

    public boolean isSalaCheia() {
        return getTotalJogadores() == (maxEquipas * maxJogadoresPorEquipa);
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
            System.out.println("[GameState] Resposta recebida de " + idJogador + ": " + resposta);
            
            // Regista a resposta
            respostasDaRonda.putIfAbsent(idJogador, new RespostaComTimestamp(resposta));
            
            // --- PLANO CONCORRÊNCIA (FUTURO) ---
            // Aqui chamarás: latch.countDown() ou barrier.check();
        }
    }

    // Chamado pelo GameLoop para preparar a próxima ronda
    public void avancarConfiguracaoRonda() {
        this.respostasDaRonda.clear();
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
        int respostaCorreta = pergunta.getCorrect();
        int pontosBase = pergunta.getPoints();

        // Ordena por tempo (simplificado, futuro usará o Latch multiplier)
        List<Map.Entry<String, RespostaComTimestamp>> respostasCorretas = respostasDaRonda.entrySet().stream()
            .filter(entry -> entry.getValue().resposta == respostaCorreta)
            .sorted(Comparator.comparingLong(entry -> entry.getValue().timestamp))
            .collect(Collectors.toList());

        for (int i = 0; i < respostasCorretas.size(); i++) {
            String idJogador = respostasCorretas.get(i).getKey();
            String idEquipa = jogadoresPorEquipa.get(idJogador);
            int pontosGanhos = (i < 2) ? pontosBase * 2 : pontosBase; 
            placar.merge(idEquipa, pontosGanhos, Integer::sum);
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
}