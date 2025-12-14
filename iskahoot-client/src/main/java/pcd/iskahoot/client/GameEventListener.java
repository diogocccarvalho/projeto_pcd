package pcd.iskahoot.client;

import pcd.iskahoot.common.Pergunta;
import pcd.iskahoot.common.TipoPergunta;
import java.util.Map;

public interface GameEventListener {
    void onConexaoSucesso();
    void onConexaoErro(String erro);
    
    void onLoginSucesso();
    void onLoginFalha(String motivo);
    
    void onNovaPergunta(Pergunta p, TipoPergunta tipo, int segundos);
    void onPlacarAtualizado(Map<String, Integer> placar, boolean fimDeJogo);
    
    void onFimTempo();

    void onPlayerJoined(String username); // NEW
    void onPlayerListReceived(java.util.List<String> players); // NEW
    
}