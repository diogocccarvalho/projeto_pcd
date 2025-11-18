package pcd.iskahoot.common;

import java.util.Map;

public class MensagemPlacar extends Mensagem {
    public final Map<String, Integer> pontuacoes;
    public final boolean jogoAcabou;

    public MensagemPlacar(Map<String, Integer> pontuacoes, boolean jogoAcabou) {
        this.pontuacoes = pontuacoes;
        this.jogoAcabou = jogoAcabou;
    }
}
