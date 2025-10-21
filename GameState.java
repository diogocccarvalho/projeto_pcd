import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameState {

  public enum GameStatus {
    ESPERA_JOGADORES,
    A_DECORRER,
    FINALIZADO
  }

  private static class RespostaComTimestamp {
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

  private final Map<String, String> jogadoresPorEquipa;
  private final Map<String, Integer> placar;
  private final Map<String, RespostaComTimestamp> respostasDaRonda;

  public GameState(String id_sala, List<Pergunta> quiz) {
    this.id_sala = id_sala;
    this.quiz = quiz;
    this.estado = GameStatus.ESPERA_JOGADORES;
    this.indicePerguntaAtual = -1;

    this.jogadoresPorEquipa = new ConcurrentHashMap<>();
    this.placar = new ConcurrentHashMap<>();
    this.respostasDaRonda = new ConcurrentHashMap<>();
  }

  public String getIdSala() {
    return id_sala;
  }

  public GameStatus getEstado() {
    return estado;
  }

  public void setEstado(GameStatus novoEstado) {
    this.estado = novoEstado;
  }

  public Pergunta getPerguntaAtual() {
    if (indicePerguntaAtual >= 0 && indicePerguntaAtual < quiz.size()) {
      return quiz.get(indicePerguntaAtual);
    }
    return null;
  }

  public void avancarParaProximaPergunta() {
    this.respostasDaRonda.clear();
    this.indicePerguntaAtual++;
  }

  public boolean jogoTerminou() {
    return indicePerguntaAtual >= quiz.size() - 1;
  }

  public Map<String, String> getJogadoresPorEquipa() {
    return jogadoresPorEquipa;
  }

  public Map<String, Integer> getPlacar() {
    return placar;
  }

  public Map<String, RespostaComTimestamp> getRespostasDaRonda() {
    return respostasDaRonda;
  }

  public boolean adicionarJogador(String idJogador, String idEquipa) {
    if (jogadoresPorEquipa.containsKey(idJogador)) {
      return false;
    }
    jogadoresPorEquipa.put(idJogador, idEquipa);
    placar.putIfAbsent(idEquipa, 0);
    return true;
  }

  public void iniciarJogo() {
    if (this.estado == GameStatus.ESPERA_JOGADORES) {
      this.setEstado(GameStatus.A_DECORRER);
      this.avancarParaProximaPergunta();
    }
  }

  public void submeterResposta(String idJogador, int resposta) {
    if (this.estado == GameStatus.A_DECORRER) {
      respostasDaRonda.put(idJogador, new RespostaComTimestamp(resposta));
    }
  }

  public void processarResultadosDaRonda() {
    Pergunta pergunta = getPerguntaAtual();
    if (pergunta == null || this.estado != GameStatus.A_DECORRER) {
      return;
    }

    switch (pergunta.getTipo()) {
      case INDIVIDUAL:
        processarResultadosIndividuais(pergunta);
        break;
      case EQUIPA:
        processarResultadosEquipa(pergunta);
        break;
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
      Map.Entry<String, RespostaComTimestamp> entry = respostasCorretas.get(i);
      String idJogador = entry.getKey();
      String idEquipa = jogadoresPorEquipa.get(idJogador);

      int pontosGanhos = pontosBase;
      if (i < 2) {
        pontosGanhos = pontosBase * 2;
      }

      adicionarPontosEquipa(idEquipa, pontosGanhos);
    }
  }

  private void processarResultadosEquipa(Pergunta pergunta) {
    Map<String, List<String>> equipasComJogadores = new HashMap<>();
    for (Map.Entry<String, String> entry : jogadoresPorEquipa.entrySet()) {
      String idJogador = entry.getKey();
      String idEquipa = entry.getValue();
      equipasComJogadores.computeIfAbsent(idEquipa, k -> new ArrayList<>()).add(idJogador);
    }

    int respostaCorreta = pergunta.getCorrect();
    int pontosBase = pergunta.getPoints();

    for (Map.Entry<String, List<String>> entry : equipasComJogadores.entrySet()) {
      String idEquipa = entry.getKey();
      List<String> membrosDaEquipa = entry.getValue();

      boolean todosAcertaram = true;
      boolean alguemAcertou = false;

      for (String membro : membrosDaEquipa) {
        RespostaComTimestamp resposta = respostasDaRonda.get(membro);
        if (resposta != null && resposta.resposta == respostaCorreta) {
          alguemAcertou = true;
        } else {
          todosAcertaram = false;
        }
      }

      if (todosAcertaram) {
        adicionarPontosEquipa(idEquipa, pontosBase * 2);
      } else if (alguemAcertou) {
        adicionarPontosEquipa(idEquipa, pontosBase);
      }
    }
  }

  private void adicionarPontosEquipa(String idEquipa, int pontosAAdicionar) {
    if (idEquipa != null && pontosAAdicionar > 0) {
      placar.compute(idEquipa,
          (key, pontosAtuais) -> (pontosAtuais == null) ? pontosAAdicionar : pontosAtuais + pontosAAdicionar);
    }
  }

  public void finalizarJogo() {
    this.setEstado(GameStatus.FINALIZADO);
  }
}
