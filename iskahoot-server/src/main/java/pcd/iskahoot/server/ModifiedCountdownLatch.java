/*
NOTES:
Cria se a latch no inicio de cada ronde e cada ClientHandler chama latch.countdown quando recebe a resposta do seu jogador
gameState vai 


*/


package pcd.iskahoot.server;

public class ModifiedCountdownLatch {
    private final int bonusFactor;          //bonus
    private final int bonusCount;           //quantos recebem o bonus
    private final int wait;
    
    private int count;                      //quantas respostas faltam
    private int respostasRecebidas = 0;
    private boolean tempoEsgotado = false;
    private boolean canceled = false;

    private Thread timerThread;

    public ModifiedCountdownLatch (int bonusFactor, int bonusCount, int wait, int count) {
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.wait = wait;
        this.count = Math.max(count,0);
        iniciarTimer();
    }

    
    
    /*
    Inicia uma thread separada que espera wait segundos
    quando o tempo acaba desbloqueia await()
    */

    //synchronized é um lock
    
    private void iniciarTimer(){
        Thread t = new Thread (() -> {
            try {
                Thread.sleep(wait * 1000L);
                } catch (InterruptedException e) {

                synchronized (this) {
                    tempoEsgotado = true;
                    notifyAll();
                }
                return;
            }

            // Tempo expirou naturalmente
            synchronized (this) {
                tempoEsgotado = true;
                notifyAll();
            }
        });

        timerThread.setDaemon(true);
        timerThread.start();
    }

    /*
    bloqueia até que todos os jogadores responderem ou ate quando o tempo espire
    */
    public synchronized void await() throws InterruptedException {
        if (count == 0 || tempoEsgotado) return;
        while (count > 0 && !tempoEsgotado){wait();}
    }

    public synchronized int countdown() {
        if (tempoEsgotado) {
            return 0; //tempo acabou logo sem bonus
        }

        respostasRecebidas++;
        count--;

        int mult = (respostasRecebidas <= bonusCount) ? bonusFactor : 1;
        if (count == 0) {
            notifyAll(); //todos os que responderam a tempo
        }

        return mult;
    }

    public synchronized void cancel() {
        canceled = true;
        tempoEsgotado = true;
        notifyAll();

        if (timerThread != null) {
            timerThread.interrupt();
        }
    }

    public synchronized boolean isTempoEsgotado() {
        return tempoEsgotado;
    }

    public synchronized int getRemaining() {
        return count;
    }

    public synchronized int getRespostasRecebidas() {
        return respostasRecebidas;
    }

}

