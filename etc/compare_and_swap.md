# compare and swap

- 동시성 알고리즘을 설계할 때 사용되는 기술
- 변수의 예상 값과 실제 값을 비교하여 둘의 값이 일치하면 실제 값을 새로운 값으로 교체한다.

## 컴페어 스왑은 어디에 쓰이는가

여러 프로그램들과 동시성 알고리즘에서 널리 발견되는 패턴은 '체크-액트'이다

체크-액트 패턴은 변수의 값을 확인하여 이 값을 전제로 다음 동작을 수행한다.

멀티 쓰레드 어플리케이션에서 '체크-액트' 연산이 제대로 수행되려면 연산의 원자성이 필요하다.

원자적인 코드 블록을 실행하는 쓰레드는 코드 싱행의 시작과 끝까지 다른 쓰레드의 간섭을 받지 않는다.

```java
public class MyLock {
    private boolean locked = false;

    public synchronized boolean lock() {
        if (!locked) {
            locked = true;
            return true;
        }
        return false;
    }
}
```

Lock() 메소드는 실제로 '컴페어 스왑'의 한 예라고 볼 수 있다. Lock() 메소드는 변수 locked 의 예상값 false로 비교하여 실제 값이 false이면 변수의 값을 true로 바꾼다.



