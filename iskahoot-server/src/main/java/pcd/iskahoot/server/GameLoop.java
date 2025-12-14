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
                
                // 1. Avançar para a próxima pergunta
                gameState.avancarConfiguracaoRonda();
                Pergunta perguntaAtual = gameState.getPerguntaAtual();

                if (perguntaAtual == null) break; // Fim da lista

                System.out.println("[GameLoop] Enviando Pergunta: " + perguntaAtual.getQuestion());

                // 2. Broadcast da Pergunta para todos
                gameState.broadcastMessage(
                    new MensagemNovaPergunta(perguntaAtual, gameState.getTipoRondaAtual()), 
                    null
                );

                // --- PLANO CONCORRÊNCIA (FUTURO) ---
                // Inicializar Latch ou Barreira aqui
                // Ex: currentLatch = new ModifiedCountdownLatch(...)
                
                // 3. AGUARDAR RESPOSTAS
                // Atualmente: Sleep fixo.
                // Futuro: chamar latch.await() ou barrier.await()
                System.out.println("[GameLoop] A aguardar respostas (10s)...");
                Thread.sleep(10000); 

                // 4. Calcular Pontos (Secção Crítica lógica)
                // O estado processa as respostas que chegaram durante o sleep
                gameState.processarResultadosDaRonda();

                // 5. Enviar Placar
                boolean ultimoRound = gameState.jogoTerminou(); // ou check do indice
                // Nota: O método jogoTerminou só retorna true se indice >= size, 
                // aqui verificamos se esta foi a última para avisar o cliente
                
                // Pequena correção lógica: verificar se há mais perguntas
                boolean fim = (gameState.getPerguntaAtual() == null); // Próxima seria null?
                // Simplificação: enviamos false sempre, e no final do while enviamos o final.
                
                gameState.broadcastMessage(
                    new MensagemPlacar(new HashMap<>(gameState.getPlacar()), false), 
                    null
                );
                
                // 6. Pausa para ver o placar
                if (!gameState.jogoTerminou()) {
                    Thread.sleep(5000);
                }
            }

            // --- FIM DO JOGO ---
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