package pcd.iskahoot.common;

public class MensagemNovoJogador extends Mensagem {
    private static final long serialVersionUID = 1L;
    
    public final String username;

    public MensagemNovoJogador(String username) {
        this.username = username;
    }
}
