package com.study.toy.semaphore;

public class ReceivingThread extends Thread {
    private final CustomSemaphore semaphore;

    public ReceivingThread(CustomSemaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        while (true) {

            try {
                this.semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //receive signal, then do something
            try {
                sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("ReceivingThread");
        }
    }
}
