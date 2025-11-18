package pcd.iskahoot.common;


public class MensagemLogin extends Mensagem {
    public final String username;
    public final String idEquipa;
    public final String idJogo;

    public MensagemLogin(String username, String idEquipa, String idJogo) {
        this.username = username;
        this.idEquipa = idEquipa;
        this.idJogo = idJogo;
    }
}