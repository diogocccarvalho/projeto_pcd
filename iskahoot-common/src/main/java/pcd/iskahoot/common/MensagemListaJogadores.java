package pcd.iskahoot.common;

import java.util.List;

public class MensagemListaJogadores extends Mensagem {
    private static final long serialVersionUID = 1L;
    
    public final List<String> jogadores;

    public MensagemListaJogadores(List<String> jogadores) {
        this.jogadores = jogadores;
    }
}