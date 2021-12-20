package com.study.toy.compare_swap;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyLock {
    private boolean locked = false;

    public synchronized boolean lock() {
        if (!locked) {
            locked = true;
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        AtomicBoolean locked = new AtomicBoolean(false);
        locked.compareAndSet(false, true);
    }
}
