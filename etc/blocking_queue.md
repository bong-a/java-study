# BlockingQueue

- 특정 상황에 쓰레드를 대기하도록 하는 큐
- 큐에서 엘레멘트를 빼려고 시도했는데 큐가 비어있다거나, 큐에 엘레먼트를 넣으려 했는데 큐에 넣을 수 있는 공간이 없다거나 할때 디큐/인큐 호출 쓰레드를 대기하도록 한다
- Java 5는 java.util.concurrent 패키지에 블로킹 큐를 포함

```java
package com.study.toy.queue;

import java.util.LinkedList;
import java.util.List;

public class CustomBlockingQueue<T> {
    private List<T> queue = new LinkedList<>();
    private int limit = 10;

    public CustomBlockingQueue(int limit) {
        this.limit = limit;
    }

    public synchronized void enqueue(T item) throws InterruptedException {
        while (this.queue.size() == this.limit) {
            wait();
        }
        if (this.queue.size() == 0) {
            notifyAll();
        }
        this.queue.add(item);
    }

    public synchronized T dequeue() throws InterruptedException {
        while (this.queue.isEmpty()) {
            wait();
        }
        if (this.queue.size() == this.limit) {
            notifyAll();
        }
        return this.queue.remove(0);
    }
}

```





