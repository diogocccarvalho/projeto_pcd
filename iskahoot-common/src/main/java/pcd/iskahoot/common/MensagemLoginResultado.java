package pcd.iskahoot.common;

public class MensagemLoginResultado extends Mensagem {
    public final boolean sucesso;
    public final String mensagemErro;

    public MensagemLoginResultado(boolean sucesso, String mensagemErro) {
        this.sucesso = sucesso;
        this.mensagemErro = mensagemErro;
    }
}