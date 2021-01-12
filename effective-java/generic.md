## 5장 제네릭

제네릭은 자바 5부터 사용 가능

제네릭을 사용하면 컬렉션이 담을 수 있는 타입을 컴파일러에 알려주게 됨 -> 컴파일러는 알아서 형변환 코드를 추가 할 수 있음 -> 엉뚱한 타입 객체를 넣으려는 시도를 컴파일 과정에서 차단 가능 -> 더 안전하고 명확한 프로그램을 만들 수 있음

단, 코드가 복잡해진다는 단점이 있음

5장에서는 제네릭의 이점을 최대로 살리고 단점을 최소화하는 방법을 이야기한다.

[TOC]



### 아이템 26 로 타입(Raw Type)은 사용하지 말라

> 용어 정리
>
> 제네릭 클래스/제네릭 인터페이스 : 클래스와 인터페이스 선언에 타입 매개변수를 씀  ex) List<E>
>
> 제네릭 클래스와 제네릭 인터페이스를 통틀어 제네릭 타입이라 한다.

제네릭 타입을 하나 정의하면 그에 딸린 로 타입도 함께 정의된다.

로타입이란 제네릭타입에서 타입 매개변수를 전혀 사용하지 않을 때를 말한다.

예를 들면 List<E>의 로타입은 List

로타입은 타입 선언에서 제네릭 타입 정보가 전부 지워진 것처럼 동작하는데, 제네릭이 도래하기 전 코드와 호환되도록 하기 위한 궁여지책이라 할 수 있다.



로타입의 안좋은 예를 보자.

```java
//제네릭 지원하기전 컬렉션 선언 - 컬렉션의 로 타입 -> 따라 하지 말 것!
private final Collection stamps = ...;
// 이 코드를 사용하면 stmap 대신 Coin을 넣어도 아무 오류 없이 컴파일되고 실행된다.  -> unchecked call 경고 나타남
stamps.add(new Coin(...)); 
// 컬렉션에서 동전을 꺼내기 전에는 오류를 알아채지 못함
for (Iterator i = stamps.iterator(); i.hasNext(); ) {
	Stamp stamp = (Stamp) i.next(); // ClassCastException 발생
  stamp.cancel();
}
```

오류는 가능한 한 발생 즉시, 이상적으로는 컴파일할 때 발견하는 것이 좋다. 

위 예제처럼 런타임에야 알아채면 런타임 문제를 겪는 코드와 원인을 제공한 코드가 물리적으로 상당히 떨어져 있을 가능성이 커진다.

```java
// 매개변수화된 컬렉션 타입 - 타입 안정성 확보
private final Collection<Stamp> stamps = ...;
stamps.add(new Coin(...));  // 컴파일 에러 발생
```

이렇게 선언하면 컴파일러는 Stamp 인스턴스만 넣어야 함을 컴파일러가 인지하게 된다. 따라서 아무런 경고 없이 컴파일 된다면 의도대로 동작할 것임을 보장한다.

위의 예제가 억지스러보일 수도 있지만 대표적으로 BigDecimal용 컬렉션에 BigInteger를 넣는 실수가 있다.



로타입을 쓰는 걸 언어 차원에서 막아 놓지는 않았지만 **절대로 써서는 안 된다**.

-> 로타입을 쓰면 제네릭이 안겨주는 안정성과 표현력을 모두 잃게 된다.

그럼 왜 로 타입을 만들어 놓은 것일까? 

- 제네릭이 받아들이기 까지 약 10년이 걸림 -> 제네릭 없이 짠 코드가 이미 많음 -> 기존 코드 모두 수용하면서 제네릭을 사용하는 새로운 코드와 맞물려 돌아가게 해야만 했음
- 마이그레이션 호환성을 위해 로 타입을 지원하고 제네릭 구현에는 소거 방식을 사용하기로 했다.



#### List와 같은 로 타입은 안되지만 **List<Object>는 괜찮다.**

- List는 제네릭 타입에서 완전히 발을 뺀 거

- List<Object>는 모든 타입을 허용한다는 의미를 컴파일러에 명확히 전달하고 있음

  - 매개변수로 List를 받는 메서드에 List<String>을 넘길수는 있지만, List<Object>를 받는 메서드에는 넘길 수 없다.

    ```java
    public static void main(String[] args) {
      List<String> strings = new ArrayList<>();
      unsafeAdd(strings,Integer.valueOf(42));
      String s = strings.get(0); 
    }
    
    public static void unsafeAdd(List list,Object o){
    	list.add(o); // Unchecked call to 'add(E)' as a member of raw type 'java.util.List' 
    }
    
    // 컴파일은 되지만, list.add에서 경고가 발생하며, 프로그램 실행시 strings.get(0)의 결과를 형변환하려 할 때 ClassCastException 던진다.
    // 컴파일런가 Integer를 String으러 변환 시도. 경고를 무시하고 실행하여 에러 발생 한것.
    ```

    ```java
    public static void main(String[] args) {
    	List<String> strings = new ArrayList<>();
    	unsafeAdd(strings,Integer.valueOf(42));  // 컴파일 에러
    	String s = strings.get(0);
    }
    
    public static void unsafeAdd(List<Object> list,Object o){
    	list.add(o);
    }
    ```



#### 원소의 타입을 몰라도 되는 로 타입을 쓰고 싶다?  **와일트카드 타입을 사용하자.**

```java
// 잘못된 예 - 모르는 타입의 원소도 받는 로 타입을 사용했다.
// 메서드는 동작하지만, 로타입을 사용해 안전하지 않다.
static int numElementsInCommon(Set s1, Set s2){
  int result = 0;
  for (Object o1:s1){
    if(s2.contains(o1)){
      result++;
    }
  }
  return result;
}
```

원소타입 몰라도 되는 로타입을 사용하고 싶다면 와일드 카드를 사용하는게 좋다.

제네릭 타입 쓰고 싶은데, 실제 타입 매개변수가 무엇인지 신경 쓰고 싶지 않다면 **물음표(?)**를 사용하자.

- Set<E>의 비한정적 와일드카드 타입은 Set<?>  -> 어떤 타입이라도 담을 수 있는 가장 범용적인 매개변수화 Set 타입

```java
static int numElementsInCommon(Set<?> s1, Set<?> s2){}
```

비한정적 와일드카드 타입(Set<?>)과 로타입(Set)의 차이점은?

- 와일드카드 타입은 안전하고, 로타입은 안전하지 않다.
  - 로타입 컬렉션에는 아무 원소나 넣을 수 있으니 타입 불변식을 훼손하기 쉽다.
  - Collection<?>에는 (null외에는) 어떤 원소도 넣을 수 없다. 다른 원소 넣으려고 하면 컴파일 에러 발생 -> 컬렉션의 타입 불변식을 훼손하지 못하게 막았다.



Collection<?>에 어떤 원소도 넣지 못하고, 꺼낼 수 있는 객체의 타입도 전혀 알 수 없다. 

이러한 제약을 받아 들일 수 없다면 **제네릭 메서드**나 **한정적 와일드카드 타입**을 사용하면 된다.



#### 로 타입 쓰지 말라는 규칙의 예외사항

- class 리터럴에는 로타입을 써야한다.

  - List.class, String[].class -> 허용
  - List<String>.class, List<?>.class -> 허용하지 않음

- instanceof 연산자 쓸 때

  - 런타입에는 제네릭 타입 정보가 지워진다. instanceof 연산자는 비한정적 와일드카드 타입(Set<?>) 이외의 매개변수화 타입에는 적용할 수 없다.
  - 로타입이든 비한정적 와일드카드 타입이든 instanceof 똑같이 동작한다.

  ```java
  # 로 타입을 써도 좋은 예 : instanceof 연산자
  if(o instanceof Set){
  	Set<?> s = (Set<?>) o;
  	//...
  }
  ```

  

#### 핵심정리

- 로타입 사용하면 런타임에 예외 발생 할 수 있으니 사용하면 안된다.

- 로타입은 제네릭 도입되기 이전 코드와의 호환성을 위해 제공될 뿐이다.

- Set<Object>는 어떤 타입의 객체도 저장할 수 있는 매개변수화 타입

- Set<?>는 모종의 타입 객체만 저장할 수 있는 와일드카드 타입

- Set (로타입)은 제네릭 타입 시스템에 속하지 않는다.

- Set<Object>, Set<?>는 안전하지만, Set는 안전하지 않다.



![image-20210112210303624](./images/image-20210112210303624.png)

