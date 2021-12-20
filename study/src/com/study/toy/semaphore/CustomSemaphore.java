package com.study.toy.semaphore;

public class CustomSemaphore {
    private boolean signal;

    public synchronized void take() {
        this.signal = true;

        this.notify();
        System.out.println("notify");
    }

    public synchronized void release() throws InterruptedException {
        while (!this.signal) {
            System.out.println("release() > wait");
            wait();
        }
        this.signal = false;
    }

    public static void main(String[] args) {
        CustomSemaphore semaphore = new CustomSemaphore();
        SendingThread sendingThread = new SendingThread(semaphore);
        ReceivingThread receivingThread = new ReceivingThread(semaphore);

        receivingThread.start();
        sendingThread.start();
    }
}
