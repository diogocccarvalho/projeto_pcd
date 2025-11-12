package pcd.iskahoot.common;

import java.io.Serializable;

public class Mensagem implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private final TipoMensagem tipo;
    private final Object payload;

    public Mensagem(TipoMensagem tipo, Object payload) {
        this.tipo = tipo;
        this.payload = payload;
    }

    public TipoMensagem getTipo() {
        return tipo;
    }

    public Object getPayload() {
        return payload;
    }

    public enum TipoMensagem implements Serializable {
        // Cliente para Servidor
        PEDIDO_LOGIN,
        ENVIAR_RESPOSTA,
        // Servidor para Cliente
        LOGIN_OK,
        LOGIN_FALHOU,
        NOVA_PERGUNTA,
        MOSTRAR_PLACAR,
        JOGO_TERMINOU
    }
}