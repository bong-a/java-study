package com.study.toy.semaphore;

public class SendingThread extends Thread {
    private final CustomSemaphore semaphore;

    public SendingThread(CustomSemaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        while (true) {
            // do something, then signal
            try {
                sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("SendingThread");
            this.semaphore.take();
        }
    }
}
