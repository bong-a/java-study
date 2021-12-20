# 세마포어

- 락과 마찬가지로 쓰레드간 신호 실종을 방지하기 위한 신호를 보내거나, 크리티컬 섹션을 보호하는 등의 목적을 위해 사용되는 쓰레드 동기화 구조이다.
- Java 5에서 java.util.concurrent 패키지에 세마포어 구현체를 포함하고 있기 때문에 직접 세마포어를 구현 할 필요는 없다
  - 하지만 세마포어 구현 기반 이론 배우는 일은 충분히 가치가 있다

```java
public class Semaphore {
    private boolean signal;

    public synchronized void take() {
        this.signal = true;
        this.notify();
    }

    public synchronized void release() throws InterruptedException {
        while (!this.signal) {
            wait();
        }
        this.signal = false;
    }
}
```



