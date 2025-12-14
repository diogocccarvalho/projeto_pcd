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
            
            Thread.sleep(2000);

            while (!gameState.jogoTerminou()) {
                
                gameState.avancarConfiguracaoRonda();
                Pergunta perguntaAtual = gameState.getPerguntaAtual();

                if (perguntaAtual == null) break;
                int tempo = gameState.getTempoPorPergunta();

                System.out.println("[GameLoop] Enviando Pergunta: " + perguntaAtual.getQuestion());
                System.out.println("[GameLoop] Destinatários: " + gameState.getPlayersInGame());

                // Broadcast Question
                gameState.broadcastMessage(
                    new MensagemNovaPergunta(perguntaAtual, gameState.getTipoRondaAtual(), tempo), 
                    null
                );

                // Wait for answers
                System.out.println("[GameLoop] A aguardar respostas (10s)...");
                Thread.sleep(10000); // REPLACE THIS WITH LATCH.AWAIT() LATER

                // Calculate Points
                gameState.processarResultadosDaRonda();

                // Broadcast Score
                gameState.broadcastMessage(
                    new MensagemPlacar(new HashMap<>(gameState.getPlacar()), false), 
                    null
                );
                
                if (!gameState.jogoTerminou()) {
                    Thread.sleep(5000);
                }
            }

            gameState.setEstado(GameState.GameStatus.FINALIZADO);
            gameState.broadcastMessage(
                new MensagemPlacar(new HashMap<>(gameState.getPlacar()), true), 
                null
            );
            System.out.println("[GameLoop] Jogo terminado.");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}