package pcd.iskahoot.server;

public class GameConfig {
    final int numEquipas;
    final int jogadoresPorEquipa;
    final int numPerguntas;

    public GameConfig(int numEquipas, int jogadoresPorEquipa, int numPerguntas) {
        this.numEquipas = numEquipas;
        this.jogadoresPorEquipa = jogadoresPorEquipa;
        this.numPerguntas = numPerguntas;
    }
}
