package pcd.iskahoot.server;

import pcd.iskahoot.common.Pergunta;
import pcd.iskahoot.common.TipoPergunta;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameState {

    public enum GameStatus {
        ESPERA_JOGADORES,
        A_DECORRER,
        FINALIZADO
    }

    public static class RespostaComTimestamp {
        final int resposta;
        final long timestamp;

        RespostaComTimestamp(int resposta) {
            this.resposta = resposta;
            this.timestamp = System.nanoTime();
        }
    }

    private final String id_sala;
    private final List<Pergunta> quiz;

    private GameStatus estado;
    private int indicePerguntaAtual;
    private TipoPergunta tipoRondaAtual; 

    private final Map<String, String> jogadoresPorEquipa;
    private final Map<String, Integer> placar;
    private final Map<String, RespostaComTimestamp> respostasDaRonda;

    public GameState(String id_sala, List<Pergunta> quiz) {
        this.id_sala = id_sala;
        // CORREÇÃO DO ERRO: Copiar e baralhar
        this.quiz = new ArrayList<>(quiz); 
        Collections.shuffle(this.quiz);    
        
        this.estado = GameStatus.ESPERA_JOGADORES;
        this.indicePerguntaAtual = -1;
        this.tipoRondaAtual = null; 

        this.jogadoresPorEquipa = new ConcurrentHashMap<>();
        this.placar = new ConcurrentHashMap<>();
        this.respostasDaRonda = new ConcurrentHashMap<>();
    }

    // --- MÉTODOS DE LÓGICA ---

    public void iniciarJogo() {
        if (this.estado == GameStatus.ESPERA_JOGADORES) {
            this.setEstado(GameStatus.A_DECORRER);
            this.avancarParaProximaPergunta(); 
        }
    }

    public void avancarParaProximaPergunta() {
        this.respostasDaRonda.clear();
        this.indicePerguntaAtual++;
        
        if (jogoTerminou()) {
            finalizarJogo();
            return;
        }

        // Lógica alternada: Se for NULL ou EQUIPA -> Passa a INDIVIDUAL
        if (this.tipoRondaAtual == null || this.tipoRondaAtual == TipoPergunta.EQUIPA) {
            this.tipoRondaAtual = TipoPergunta.INDIVIDUAL;
        } else {
            this.tipoRondaAtual = TipoPergunta.EQUIPA;
        }
        
        System.out.println("[GameState] Pergunta " + (indicePerguntaAtual + 1) + " (" + tipoRondaAtual + ")");
    }

    public boolean adicionarJogador(String idJogador, String idEquipa) {
        if (jogadoresPorEquipa.containsKey(idJogador)) return false;
        jogadoresPorEquipa.put(idJogador, idEquipa);
        placar.putIfAbsent(idEquipa, 0);
        return true;
    }

    public void submeterResposta(String idJogador, int resposta) {
        if (this.estado == GameStatus.A_DECORRER) {
            respostasDaRonda.put(idJogador, new RespostaComTimestamp(resposta));
        }
    }

    public void processarResultadosDaRonda() {
        Pergunta pergunta = getPerguntaAtual();
        if (pergunta == null || this.estado != GameStatus.A_DECORRER) return;

        if (this.tipoRondaAtual == TipoPergunta.INDIVIDUAL) {
            processarResultadosIndividuais(pergunta);
        } else {
            processarResultadosEquipa(pergunta);
        }
    }

    private void processarResultadosIndividuais(Pergunta pergunta) {
        int respostaCorreta = pergunta.getCorrect();
        int pontosBase = pergunta.getPoints();

        List<Map.Entry<String, RespostaComTimestamp>> respostasCorretas = respostasDaRonda.entrySet().stream()
            .filter(entry -> entry.getValue().resposta == respostaCorreta)
            .sorted(Comparator.comparingLong(entry -> entry.getValue().timestamp))
            .collect(Collectors.toList());

        for (int i = 0; i < respostasCorretas.size(); i++) {
            String idJogador = respostasCorretas.get(i).getKey();
            String idEquipa = jogadoresPorEquipa.get(idJogador);
            // Bónus para os 2 primeiros
            int pontosGanhos = (i < 2) ? pontosBase * 2 : pontosBase;
            adicionarPontosEquipa(idEquipa, pontosGanhos);
        }
    }

    private void processarResultadosEquipa(Pergunta pergunta) {
        Map<String, List<String>> equipasComJogadores = new HashMap<>();
        jogadoresPorEquipa.forEach((k, v) -> 
            equipasComJogadores.computeIfAbsent(v, x -> new ArrayList<>()).add(k)
        );

        int respostaCorreta = pergunta.getCorrect();
        int pontosBase = pergunta.getPoints();

        for (Map.Entry<String, List<String>> entry : equipasComJogadores.entrySet()) {
            String idEquipa = entry.getKey();
            List<String> membros = entry.getValue();
            boolean todosAcertaram = true;
            boolean alguemAcertou = false;
            
            for (String membro : membros) {
                RespostaComTimestamp resp = respostasDaRonda.get(membro);
                if (resp == null || resp.resposta != respostaCorreta) todosAcertaram = false;
                if (resp != null && resp.resposta == respostaCorreta) alguemAcertou = true;
            }

            if (todosAcertaram) adicionarPontosEquipa(idEquipa, pontosBase * 2);
            else if (alguemAcertou) adicionarPontosEquipa(idEquipa, pontosBase);
        }
    }

    private void adicionarPontosEquipa(String idEquipa, int pontos) {
        if (idEquipa != null && pontos > 0) {
            placar.merge(idEquipa, pontos, Integer::sum);
        }
    }

    // Getters e Helpers
    public Pergunta getPerguntaAtual() {
        if (indicePerguntaAtual >= 0 && indicePerguntaAtual < quiz.size()) return quiz.get(indicePerguntaAtual);
        return null;
    }
    public TipoPergunta getTipoRondaAtual() { return tipoRondaAtual; }
    public boolean jogoTerminou() { return indicePerguntaAtual >= quiz.size(); }
    public void finalizarJogo() { this.estado = GameStatus.FINALIZADO; }
    public String getIdSala() { return id_sala; }
    public GameStatus getEstado() { return estado; }
    public void setEstado(GameStatus estado) { this.estado = estado; }
    public Map<String, Integer> getPlacar() { return placar; }
}