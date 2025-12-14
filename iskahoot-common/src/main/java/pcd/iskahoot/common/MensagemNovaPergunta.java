package pcd.iskahoot.common;

public class MensagemNovaPergunta extends Mensagem {
    public final Pergunta pergunta;
    public final TipoPergunta tipoRonda;
    public final int segundos;

    public MensagemNovaPergunta(Pergunta pergunta, TipoPergunta tipoRonda, int segundos) {
        this.pergunta = pergunta;
        this.tipoRonda = tipoRonda;
        this.segundos = segundos;
    }
}