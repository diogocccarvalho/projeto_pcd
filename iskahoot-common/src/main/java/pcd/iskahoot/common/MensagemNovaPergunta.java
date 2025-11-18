package pcd.iskahoot.common;

public class MensagemNovaPergunta extends Mensagem {
    public final Pergunta pergunta;
    public final TipoPergunta tipoRonda;

    public MensagemNovaPergunta(Pergunta pergunta, TipoPergunta tipoRonda) {
        this.pergunta = pergunta;
        this.tipoRonda = tipoRonda;
    }
}