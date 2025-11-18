package pcd.iskahoot.common;

public class MensagemEnviarResposta extends Mensagem {
    public final int indiceResposta;

    public MensagemEnviarResposta(int indiceResposta) {
        this.indiceResposta = indiceResposta;
    }
}