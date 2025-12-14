package pcd.iskahoot.server;

public class GameConfig {
    final int numEquipas;
    final int jogadoresPorEquipa;
    final int numPerguntas;
    final int tempoPorPergunta;

    public GameConfig(int numEquipas, int jogadoresPorEquipa, int numPerguntas, int tempoPorPergunta) {
        this.numEquipas = numEquipas;
        this.jogadoresPorEquipa = jogadoresPorEquipa;
        this.numPerguntas = numPerguntas;
        this.tempoPorPergunta = tempoPorPergunta;
    }
}
