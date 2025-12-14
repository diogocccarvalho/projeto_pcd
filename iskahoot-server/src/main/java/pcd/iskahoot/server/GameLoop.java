package pcd.iskahoot.server;

import pcd.iskahoot.common.*;
import java.util.HashMap;

public class GameLoop implements Runnable {

    private final GameState gameState;

    public GameLoop(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void run() {
        try {
            System.out.println("[GameLoop] Jogo a iniciar na sala " + gameState.getIdSala());
            gameState.setEstado(GameState.GameStatus.A_DECORRER);
            
            // Pausa inicial antes da primeira pergunta
            Thread.sleep(2000);

            // --- CICLO DO JOGO ---
            while (!gameState.jogoTerminou()) {
                
                // 1. Avançar para a próxima pergunta e obter configurações
                gameState.avancarConfiguracaoRonda();
                Pergunta perguntaAtual = gameState.getPerguntaAtual();

                if (perguntaAtual == null) break; // Fim da lista de perguntas

                // Obter o tempo definido para esta ronda (configurado no inicio)
                int tempo = gameState.getTempoPorPergunta();

                // 2. [NOVO] Inicializar Primitivas de Sincronização (Latch ou Barreira)
                // Isto prepara o GameState para receber respostas e bloquear a thread
                gameState.iniciarSincronizacao();

                System.out.println("[GameLoop] Enviando Pergunta: " + perguntaAtual.getQuestion());
                System.out.println("[GameLoop] Tipo: " + gameState.getTipoRondaAtual() + " | Tempo: " + tempo + "s");

                // 3. Broadcast da Pergunta para todos (incluindo o tempo)
                gameState.broadcastMessage(
                    new MensagemNovaPergunta(perguntaAtual, gameState.getTipoRondaAtual(), tempo), 
                    null
                );

                System.out.println("[GameLoop] A aguardar respostas...");
                
                // 4. [NOVO] Bloquear até todos responderem OU o tempo acabar
                // Substitui o Thread.sleep(10000) antigo.
                gameState.esperarPeloFimDaRonda(); 

                // 5. Calcular Pontos (Acontece imediatamente após o desbloqueio)
                System.out.println("[GameLoop] Ronda terminada. A processar resultados...");
                gameState.processarResultadosDaRonda();

                // 6. Enviar Placar
                boolean fim = (gameState.getPerguntaAtual() == null); 
                
                gameState.broadcastMessage(
                    new MensagemPlacar(new HashMap<>(gameState.getPlacar()), false), 
                    null
                );
                
                // 7. Pausa para os jogadores verem o placar
                if (!gameState.jogoTerminou()) {
                    Thread.sleep(5000);
                }
            }

            // --- FIM DO JOGO ---
            gameState.setEstado(GameState.GameStatus.FINALIZADO);
            
            // Enviar placar final com a flag true
            gameState.broadcastMessage(
                new MensagemPlacar(new HashMap<>(gameState.getPlacar()), true), 
                null
            );
            System.out.println("[GameLoop] Jogo terminado na sala " + gameState.getIdSala());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}