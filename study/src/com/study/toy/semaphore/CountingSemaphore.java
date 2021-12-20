package com.study.toy.semaphore;

public class CountingSemaphore {
    // 신호의 최대치에 대한 제한이 없다
    private int signals = 0;

    public synchronized void take(){
        this.signals++;
        this.notify();
    }

    public synchronized void release() throws InterruptedException{
        while (this.signals==0){
            wait();
        }
        this.signals--;
    }
}
