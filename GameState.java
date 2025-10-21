import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

  public enum GameStatus {
    ESPERA_JOGADORES,
    A_DECORRER,
    FINALIZADO
  }

  private final String id_sala;
  private final List<Pergunta> quiz;

  private GameStatus estado;
  private int indicePerguntaAtual;

  private final Map<String, String> jogadoresPorEquipa;
  private final Map<String, Integer> placar;
  private final Map<String, Integer> respostasDaRonda;

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

  public Map<String, Integer> getRespostasDaRonda() {
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
      respostasDaRonda.put(idJogador, resposta);
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

    for (Map.Entry<String, Integer> entry : respostasDaRonda.entrySet()) {
      if (entry.getValue() == respostaCorreta) {
        String idJogador = entry.getKey();
        String idEquipa = jogadoresPorEquipa.get(idJogador);
        int pontosGanhos = calcularPontosGanhos(idJogador, pontosBase);
        adicionarPontosEquipa(idEquipa, pontosGanhos);
      }
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
        if (respostasDaRonda.getOrDefault(membro, -1) == respostaCorreta) {
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

  private int calcularPontosGanhos(String idJogador, int pontosBase) {
    return pontosBase;
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
