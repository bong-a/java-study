package com.study.toy.semaphore;

public class BoundedSemaphore {
    private int signals = 0;
    private int bound = 0;

    public BoundedSemaphore(int upperBound) {
        this.bound = upperBound;
    }

    // 저장된 신호의 수가 한도에 다다르면 호출 쓰레드는 블록된다.
    // release()가 호출될 때까지 take() 호출한 쓰레드는 신호를 보낼 수 없다.
    public synchronized void take() throws InterruptedException {
        while (this.signals == bound) {
            wait();
        }
        this.signals++;
        this.notify();
    }

    public synchronized void release() throws InterruptedException {
        while (this.signals == 0) {
            wait();
        }
        this.signals--;
        this.notify();
    }
}
