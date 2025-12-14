package pcd.iskahoot.server;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TeamBarrier {
    private final Lock lock = new ReentrantLock();
    private final Condition trip = lock.newCondition();
    
    private final int totalPlayers;
    private int arrivalCount = 0;
    private boolean timeExpired = false;
    private final Thread timerThread;

    public TeamBarrier(int totalPlayers, int timeoutSeconds) {
        this.totalPlayers = totalPlayers;

        // Internal Timer: Breaks the barrier if players are too slow
        this.timerThread = new Thread(() -> {
            try {
                Thread.sleep(timeoutSeconds * 1000L);
                lock.lock();
                try {
                    timeExpired = true;
                    trip.signalAll(); // Wake up GameLoop!
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) { 
                return; // Timer cancelled
            }
        });
        this.timerThread.setDaemon(true);
        this.timerThread.start();
    }

    // Called by ClientHandler (via GameState)
    public void arrive() {
        lock.lock();
        try {
            if (timeExpired) return;

            arrivalCount++;
            // If the last player arrives, wake up the GameLoop
            if (arrivalCount >= totalPlayers) {
                trip.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    // Called by GameLoop (via GameState)
    public void await() throws InterruptedException {
        lock.lock();
        try {
            // Block while: Time has NOT expired AND Not everyone has arrived
            while (!timeExpired && arrivalCount < totalPlayers) {
                trip.await();
            }
        } finally {
            lock.unlock();
        }
    }
    
    // Called by GameLoop after waking up to kill the timer
    public void cancelTimer() {
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
    }
}