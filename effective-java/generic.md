# 5장 제네릭

제네릭은 자바 5부터 사용 가능

제네릭을 사용하면 컬렉션이 담을 수 있는 타입을 컴파일러에 알려주게 됨 -> 컴파일러는 알아서 형변환 코드를 추가 할 수 있음 -> 엉뚱한 타입 객체를 넣으려는 시도를 컴파일 과정에서 차단 가능 -> 더 안전하고 명확한 프로그램을 만들 수 있음

단, 코드가 복잡해진다는 단점이 있음

5장에서는 제네릭의 이점을 최대로 살리고 단점을 최소화하는 방법을 이야기한다.

[TOC]



## 아이템 26. 로 타입(Raw Type)은 사용하지 말라

-------

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



### List와 같은 로 타입은 안되지만 List<Object>는 괜찮다.

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



### 원소의 타입을 몰라도 되는 로 타입을 쓰고 싶다?  와일드카드 타입을 사용하자.

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



### 로 타입 쓰지 말라는 규칙의 예외사항

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

  

### 핵심정리

- 로타입 사용하면 런타임에 예외 발생 할 수 있으니 사용하면 안된다.

- 로타입은 제네릭 도입되기 이전 코드와의 호환성을 위해 제공될 뿐이다.

- Set<Object>는 어떤 타입의 객체도 저장할 수 있는 매개변수화 타입

- Set<?>는 모종의 타입 객체만 저장할 수 있는 와일드카드 타입

- Set (로타입)은 제네릭 타입 시스템에 속하지 않는다.

- Set<Object>, Set<?>는 안전하지만, Set는 안전하지 않다.



![image-20210112210303624](./images/image-20210112210303624.png)



## 아이템 27. 비검사 경고를 제거하라

-------

제네릭 사용하기 시작하면 수많은 컴파일러 경고를 보게 된다.

- 비검사 형변환 경고
- 비검사 메서드 호출 경고
- 비검사 매개변수화 가변인수 타입 경고
- 비검사 변환 경고

### 할 수 있는 한 모든 비검사 경고를 제거하라

```java
Set<Lark> exaltation = new HashSet(); // required: Set<Lark> found: HashSet 

Set<Lark> exaltation = new HashSet<>(); //다이아몬드 연산자만으로 해결 가능
```

모든 비검사 경고를 제거한다면 그 코드는 타입 안정성이 보장된다.

- 즉, 런타임에  ClassCastException이 발생할 일이 없고, 의도한대로 잘 동작하리라 확신할 수 있다.



### 경고를 제거할 수 없지만 타입 안전하다고 확신한다면 @SuppressWarnings("unchecked") 애너테이션을 달아 경고를 숨기자.

단, 타입 안전함을 검증하지 않은 채 경로를 숨기면 스스로에게 잘못된 보안 인식을 심어주는 꼴이다.

- 경고 없이 컴파일 되지만, 런타임에는 여전히 ClassCastException을 던질 수 있다.

한편, 안전하다고 검증된 비검사 경고를 숨기지 않고 그대로 두면, 진짜 문제를 알리는 새로운 경고가 나와도 눈치채지 못할 수 있다.

- 제거하지 않은 수많은 거짓 경고 속에 새로운 경고가 파묻힐것이기 때문

### @SuppressWarnings 애너테이션은 항상 가능한 좁은 범위에 적용하자.

절대로 클래스 전체에 적용해서는 안된다.

한줄이 넘는 메서드나 생성자에 달린 @SuppressWarnings 애너테이션을 발견하면 지역변수 선언 쪽으로 옮기자.

```java
//ArrayList의 toArray
public <T> T[] toArray(T[] a) {
  if (a.length < size) {
  	return (T[])Arrays.copyOf(elements, size, a.getClass()); // Unchecked cast: 'java.lang.Object[]' to 'T[]' 
  }
  System.arraycopy(elements, 0, a, 0, size);

  if (a.length > size) {
  	a[size] = null;
  }
  return a;
}

// 지역변수를 추가해 @SuppressWarnings의 범위를 좁힌다.
public <T> T[] toArray(T[] a) {
  if (a.length < size) {
    @SuppressWarnings("unchecked")
    T[] result = (T[])Arrays.copyOf(elements, size, a.getClass());
    return result;
  }
  System.arraycopy(elements, 0, a, 0, size);

  if (a.length > size) {
  	a[size] = null;
  }
  return a;
}
```



### @SuppressWarnings("unchecked") 애너테이션을 사용할 때면 그 경고를 무시해도 안전한 이유를 항상 주석으로 남겨라

다른 사람이 그 코드를 이해하는데 도움이 되며, 더 중요하게는, 다른 사람이 그 코드를 잘못 수정하여 타입 안정성을 잃는 상황을 줄여준다.

코드가 안전한 근거가 쉽게 떠오르지 않더라도 끝까지 포기하지 말자. 근거를 찾는 중에 그 코드가 사실은 안전하지 않다는 걸 발견할 수도 있으니 말이다.



### 핵심정리

비검사 경고는 중요하니 무시하지 말자.

모든 비검사 경고는 런타임에 ClassCastException을 일으킬 수 있는 잠재적 가능성을 뜻하니 최선을 다해 제거하라.

경고를 없앨 방법을 찾지 못하겠다면, 그 코드가 타입 안전함을 증명하고 가능한 한 범위를 좁혀 @SuppressWarnings("unchecked") 애너테이션으로 경고를 숨겨라

그런 다음 경고를 숨기기로 한 근거를 주석을 남겨라.



## 아이템 28. 배열보다는 리스트를 사용하라

-------

### 배열 vs 제네릭

배열과 제네릭 타입에는 중요한 차이가 두가지가 있다.

1. 배열은 공변이다.

   - 공변 : 함께 변한다.

   - Sub가 Super의 하위 타입이라면 배열 Sub[]는 배열 Super[]의 하위타입이 된다.

   - Type1,Type2가 있을 때, List<Type1>은 List<Type2>의 하위타입도 아니고 상위타입도 아니다.

     제네릭에 문제가 있다고 생각할 수 있지만, 사실 문제가 있는 건 배열쪽이다.

     ```java
     // 런타임에 실패한다.
     Object[] objectArray = new Long[1];
     objectArray[0] = "타입이 달라 넣을 수 없다."; // Exception in thread "main" java.lang.ArrayStoreException: java.lang.String 
     ```

     ```java
     // 컴파일되지 않음
     List<Object> ol = new ArrayList<Long>(); // 호환되지 않는 타입
     ol.add("타입이 달라 넣을 수 없다.");
     ```

2. 배열은 실체화된다.
   - 배열은 런타임에도 자신이 담기로 한 원소의 타입을 인지하고 확인한다.
   - 제네릭은 타입 정보가 런타임에는 소거된다.
     - 소거는 제네릭이 지원되기 전의 레거시 코드와 제네릭 타입을 함께 사용할 수 있게 해주는 메커니즘으로, 자바 5가 제네릭으로 순조롭게 전환될 수 있도록 해줬다.

위 두가지의 주요 차이로 배열과 제네릭은 잘 어우러지지 못한다.



배열은 제네릭타입,매개변수화 타입, 타입 매개변수로 사용할 수 없다.

즉, `new List<E>[]`,`new List<String>[]`,`new E[]`식으로 작성하면 컴파일할 때 제네릭 배열 생성 오류를 일으킨다.



### 제네릭 배열을 만들지 못하게 막은 이유는 무엇일까?

- 타입이 안전하지 않기 때문이다.

이를 허용하게되면 컴파일러가 자동 생성한 형변환 코드에서 런타임에 ClassCastException이 발생할 수 있다.

런타임에 ClassCastException이 발생하는 일을 막아주겠다는 제네릭 타입 시스템의 취지에 어긋나는 것이다.

다음 코드로 구체적인 상황을 살펴보자.

```java
List<String>[] stringLists = new List<String>[1];       //1
List<Integer> intList = Collections.singletonList(42);  //2
Object[] objects = stringLists;													//3
objects[0] = intList;																		//4
String s = stringLists[0].get(0);												//5
```

제네릭 배열을 생성하는 (1)이 허용된다고 가정해보자.

(2)는 원소가 하나인 List<Integer>를 생성한다.

(3)은 (1)에서 생성한 List<String>배열을 Object 배열에 할당한다. 배열은 공변이니 아무 문제 없다.

(4)는 (2)에서 생성한 List<Integer>의 인스턴스를 Object배열의 첫 원소로 저장한다. 제네릭은 소거 방식으로 구현되어 이 역시 성공한다.

즉, 런타임에는 List<Integer> 인스턴스의 타입이 List가 되고, List<Integer>[] 인스턴스의 타입은 List[]가 된다.

따라서 (4)에서도 ArrayStoreException을 일으키지 않는다.

List<String> 인스턴스만 담겠다고 선언한 stringLists배열에는 지금 List<Integer> 인스턴스가 저장되어 있다.

그리고 (5)는 이 배열의 처음 리스트에서 첫 원소를 꺼내려한다. 

컴파일러는 꺼낸 원소를 자동으로 String으로 형변환하는데, 원소가 Integer이므로 런타임에 ClassCastException이 발생한다.

이런 일을 방지하려면 (1)에서 제네릭 배열이 생성되지 않도록 컴파일 오류를 내야한다.



 `List<E>`,`List<String>`,`E` 같은 타입을 실체화 불가 타입이라 한다.

- 실체화 되지않아서 런타임에는 컴파일 타임보다 타입 정보를 적게 가지는 타입
- 소거 메커니즘 때문에 매개변수화 타입 가운데 실체화 될 수 있는 타입은 List<?>, Map<?,?> 같은 비한정적 와일드 카드 타입뿐이다.
  - 배열을 비한정적 와일드카드 타입으로 만들수 있지만, 유용하게 쓰일 일은 거의 없다.

제네릭 컬렉션에서 자신의 원소타입을 담은 배열을 반환하는게 불가능하다 . 

- 완벽하지 않지만 대부분의 상황에서 이문제를 해결해주는 방법은 아이템33. 타입 안전 이종컨테이너를 고려하라를 참고

제네릭 타입과 가변인수 메서드를 함께 쓰면 해석하기 어려운 경고 메세지를 받게 된다. 가변인수메서드를 호출할 때마다 가변인수 매개변수를 담을 배열이 하나 만들어지는데, 이때 그 배열의 원소가 실체화 불가 타입이라면 경고가 발생하는 것이다.

- 이 문제는 @SafeVarargs 애너테이션으로 대처할 수 있다.(아이템32. 제네릭과 가변인수를 함께 쓸 때는 신중하라)

  ```java
  // warning Possible heap pollution from parameterized vararg type 
  static void dangerous(List<String>... stringLists){
  	List<Integer> integerList = Collections.singletonList(42);
  	Object[] objects = stringLists;
  	objects[0] = integerList;           //힙 오염 발생
  	String s = stringLists[0].get(0);   //ClasCastException
  }
  
  @SafeVarargs
  static void dangerous(List<String>... stringLists){
    //...
  }
  ```



배열로 형변환할 때 제네릭 배열 생성 오류나 비검사 형변화 경고가 뜨는 경우 대부분은 배열인 `E[]` 대신 컬렉션인 `List<E>`를 사용하면 해결된다.

- 코드가 조금 복잡해지고 성능이 살짝 나빠질 수도 있지만, 그 대신 타입 안전성과 상호운용성은 좋아진다.



### 핵심정리

- 배열과 제네릭에는 매우 다른 타입 규칙이 적용된다.
- 배열은 공변이고 실체화된다.
- 제네릭은 불공변이고 타입 정보가 소거된다.
- 그 결과 배열은 런타임에는 타입 안전하지만 컴파일타임에는 그렇지 않다.
  - 제넥릭은 그 반대다
  - 그래서 둘은 섞어 쓰기란 쉽지 않다.
- 둘을 섞어 쓰다가 컴파일 오류나 경고를 만나면, 가장 먼저 배열을 리스트로 대체하는 방법을 적용해보자.



## 아이템 29. 이왕이면 제네릭 타입으로 만들라

클라이언트에서 직접 형변환해야하는 타입보다 제네릭 타입이 더 안전하고 쓰기 편하다.

그러니 새로운 타입을 설계할 때는 형변환 없이도 사용할 수 있도록 하라.

그렇게 하려면 제네릭 타입으로 만들어야 할 경우가 많다.

기존 타입 중 제네릭이 있어야하는게 있다면 제네릭 타입으로 변경하자.

기존 클라이언트에는 아무 영향을 주지 않으면서, 새로운 사용자를 훨씬 편하게 해주는 길이다.



## 아이템 30. 이왕이면 제네릭 메서드로 만들라

메서드도 제네릭으로 만들 수 있다.

매개변수화 타입을 받는 정적 유틸리티 메서드는 보통 제네릭이다.

- ex : Collections.binarySearch(), Collections.sort()

```java
// Raw 타입 사용 - 수용 불가!
public static Set union(Set s1, Set s2) {
  //Unchecked call to 'HashSet(Collection<? extends E>)' as a member of raw type 'java.util.HashSet' 
	Set result = new HashSet(s1);
	//Unchecked call to 'addAll(Collection<? extends E>)' as a member of raw type 'java.util.Set'
	result.addAll(s2);
	return result;
}
```

경고를 없애려면 이 메서드를 타입 안전하게 만들어야 한다.

메서드 선언에서 세 집합(입력2개, 반환1개)의 원소 타입을 타입 매개변수로 명시하고, 메서드 안에서도 이 타입 매개변수만 사용하게 수정하면 된다.

타입 매개변수 목록은 메서드의 제한자와 반환타입 사이에 온다.

```java
public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
	Set<E> result = new HashSet<>(s1);
	result.addAll(s2);
	return result;
}
```

- 경고없이 컴파일
- 타입 안전
- 쓰기 쉬움



### 제네릭 싱글턴 팩터리 패턴

때때로 불변 객체를 여러 타입으로 활용할 수 있게 만들어야 할 때가 있다.

제네릭은 런타입에 타입 정보가 소거되므로 하나의 객체를 어떤 타입으로든 매개변수화 할 수 있다. 하지만 이렇게 하려면 요청한 타입 매개변수에 맞게 매번 그 객체의 타입을 바꿔주는 정적 팩터리를 만들어야 한다.  -> 이 패턴을 제네릭 싱글턴 팩터리라 한다.

- Collections.reverseOrder() 
- Collections.emptySet

제네릭 싱글턴 팩터리 패턴을 활용한 항등 함수 구현

```java
private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;

@SuppressWarnings("unchecked")
public static <T> UnaryOperator<T> identityFunction() {
	return (UnaryOperator<T>)IDENTITY_FN;
}

public static void main(String[] args) {
	String[] strings = {"삼베", "대마", "나일론"};
	UnaryOperator<String> sameString = identityFunction();
	for (String string : strings) {
		System.out.println(sameString.apply(string));
	}

	Number[] numbers = {1, 2.0, 3L};
	UnaryOperator<Number> sameNumber = identityFunction();
	for (Number number : numbers) {
		System.out.println(sameNumber.apply(number));
	}
}
```



### 재귀적 타입 한정(recursive tpye bound)

상대적으로 드물긴 하지만, 자기 자신이 들어간 표현식을 사용하여 타입 매개변수의 허용 범위를 한정 할 수 있다. -> 재귀적 타입 한정

재귀적 타입 한정은 주로 타입의 자연적 순서를 정하는 Comparable 인터페이스와 함께 쓰인다.

Comparable<T>에서 타입 매개변수 T는 비교할 수 있는 원소의 타입을 정의한다.

```java
// 재귀적 타입 한정을 이용해 상호 비교할 수 있음을 표현했다.
public static <E extends Comparable<E>> E max(Collection<E> c)
```

타입 한정인 `<E extends Comparable<E>>`는 `모든 타입 E는 자신과 비교 할 수 있다.`라고 읽을 수 있다.

- 상호 비교 가능하다는 뜻을 아주 정확하게 표현한 것



### 핵심정리

제네릭 타입과 마찬가지로, 클라이언트에서 입력 매개변수와 반환값을 명시적으로 형변환해야하는 메서드보다 제네릭 메서드가 더 안전하며 사용하기도 쉽다.

타입과 마찬가지로, 메서드도 형변환 없이 사용할 수 있는 편이 좋으며, 많은 경우 그렇게 하려면 제네릭 메서드가 되어야 한다.

역시 타입과 마찬가지로, 형변화 해줘야 하는 기존 메서드는 제네릭하게 만들자.

기존 클라이언트는 그대로 둔 채 새로운 사용자의 삶을 훨씬 편하게 만들어 줄 것이다.



## 아이템 31. 한정적 와일카드를 사용해 API 유연성을 높이라

### 유연성을 극대화하려면 원소의 생산자나 소비자용 입력 매개변수에 와일드카드 타입을 사용하라

매개변수화 타입은 불공변

List<String>은 List<Object>가 하는 일을 제대로 수행하지 못하니 하위 타입이 될 수 없다. (리스코프 치환 원칙에 어긋남)

불공변 방식보다 유연한 무언가가 필요할때는?

```java
package com.study.toy.item_31;

import java.util.Stack;

public class StackTest<T> extends Stack<T> {
    // 와일드 카드 타입을 사용하지 않은 pushAll 메서드
    // 컴파일은 되지만 완벽하지 않음
    public void pushAll(Iterable<T> src){
        for (T t:src){
            push(t);
        }
    }
}
```

- Iterable src의 원소타입이 스택의 원소 타입과 일치하면 잘 작동한다.

- Stack<Number>로 선언 후 pushAll(intVal) 호출하면 어떻게 될까? (intVal -> Integer)

  - 매개변수화 타입이 불공변이기 때문에 에러 발생
  - 해결책 : 한정적 와일드 카드 타입 활용
    - pushAll의 입력 매개변수 타입은 'E의 Iterable'이 아니라 'E의 하위 타입의 Iterable'이어야 한다.
    - Iterable<? extends E> = E의 하위 타입의 Iterable
      - 하위타입이란 자기 자신도 포함하긴하지만 자신을 확장하는 것은 아니기때문에 extends라는 키워드가 딱 어울리지는 않음)

  ```java
  public static void main(String[] args) {
    CustomStack<Number> numbers = new CustomStack<>();
    Iterable<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
    numbers.pushAll(integers); // 컴파일에러
  }
  ```

  ```java
  public void pushAll(Iterable<? extends T> src){
     for (T t:src){
       push(t);
     }
  }
  ```

```java
package com.study.toy.item_31;

import java.util.Stack;

public class StackTest<T> extends Stack<T> {
    // 와일드 카드 타입을 사용하지 않은 popAll 메서드
    // 컴파일은 되지만 완벽하지 않음
    public void popAll(Collection<E> dst){
    	while (!isEmpty()){
      	dst.add(pop());
      }
    }
}
```

- Stack<Number>의 원소를 Object용 컬렉션으로 옮기려고 한다면?

  - pushAll와 같이 컴파일 에러 발생
  - Collection<Object>는 Collection<Number>의 하위타입이 아니기 때문
  - 해결책 : 와일드 카드 타입 활용
    - popAll의 입력 매개변수의 타입이 'E의 Collection'이 아니라 'E의 상위 타입의 Collection'이어야 한다.
    - Collection<? Super E> = E의 상위 타입의 Collection

  ```java
  public static void main(String[] args) {
    CustomStack<Number> numbers = new CustomStack<>();
    Iterable<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
    numbers.pushAll(integers);
  
    Collection<Object> objects = new ArrayList<>();
    numbers.popAll(objects); // 컴파일 에러
  }
  ```

  ```java
  public void popAll(Collection<? super E> dst){
    while (!isEmpty()){
    	dst.add(pop());
    }
  }
  ```



**유연성을 극대화하려면 원소의 생산자나 소비자용 입력 매개변수에 와일드카드 타입을 사용하라**

한편, 입력 매개변수가 생산자와 소비자 역할을 동시에 한다면 와일드카드 타입을 써도 좋을게 없다.

- 타입을 정확히 지정해야하는 상황으로, 이때는 와일드카드 타입을 쓰지 말아야 한다.



### PECS : 와일드카드 타입 사용 기본 원칙

다음 공식을 외워두면 어떤 와일드카드 타입을 써야 하는지 기억하는데 도움된다.

```markdown
펙스(PECS) : producer-extends, consumer-super
```

- 매개변수화 타입 T가 생산자라면 <? extends T>를 사용하고 소비자라면 <? Super T>를 사용하라.



### 반환 타입에는 한정적 와일드카드 타입을 사용하면 안된다

아래 예시에서 반환타입은 여전히 Set<E>임에 주목하자.

- 반환 타입에는 한정적 와일드카드 타입을 사용하면 안된다.
- 유연성을 높여주기는커녕 클라이언트 코드에서도 와일드타드 타입을 써야하기 때문이다.

```java
public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2) {
	Set<E> result = new HashSet<>(s1);
	result.addAll(s2);
	return result;
}
```



### Comparable을 직접 구현하지 않고, 직접 구현한 다른 타입을 확장한 타입을 지원하기 위해 와일드카드가 필요

```java
public static <E extends Comparable<E>> E max(Collection<E> c)
//와일드카드 타입을 사용해 다듬은 모습
public static <E extends Comparable<? super E>> E max(Collection<? extends E> c)
```

- 입력 매개변수에서는 E 인스턴스 생산하므로 List<? Extends E>

- 타입 매개변수에서 E가 Comparable<E>를 확장한다고 정의했는데, 이때 Comparable<E>는 E 인스턴스를 소비한다(선후 관계를 뜻하는 정수를 생산)

  그래서 매개변수화 타입 Comparable<E>를 한정적 와일드카드 타입은 Comparable<? super E>를 대체했다.

- Comparable은 언제나 소비자이므로 일반적으로 Comparable<? super E>를 사용하는 편이 낫다. Comparator도 마찬가지다.

이렇게 까지 복잡하게 만들 가치가 있을까?

- 수정 전 max는 이 리스트`List<ScheduledFuture<?>> scheduledFutures = ...`를 처리할 수 없다. ScheduledFuture가 Comparable<ScheduledFuture>를 구현하지 않았기 때문
  - ScheduledFuture가 Delayed의 하위 인터페이스이고, Delayed는 Comparable<Delayed>를 확장했다.
  - 다시말해, ScheduledFuture의 인스턴스는 다른 ScheduledFuture 인스턴스 뿐만 아니라 Delayed 인스턴스와도 비교 할 수 있어서 수정 전 max가 이 리스트를 거부하는 것이다.

좀더 일반화해서 말하면, **Comparable을 직접 구현하지 않고, 직접 구현한 다른 타입을 확장한 타입을 지원하기 위해 와일드카드가 필요**하다.

```java
List<ScheduledFuture<?>> scheduledFutures = ...;

public interface ScheduledFuture<V> extends Delayed, Future<V> {}
public interface Delayed extends Comparable<Delayed> {}
public interface Comparable<T> {}
```



### 메서드 선언에 타입 매개변수가 한번만 나오면 와일드 카드로 대체하라

타입 매개변수와 와일드카드에는 공통되는 부분이 있어서, 메서드를 정의할 때 둘 중 어느 것을 사용해도 괜찮을 때가 많다.

주어진 리스트에서 명시한 두 인덱스의 아이템들을 교환하는 정적메서드를 두 방식 모두 정의해보자.

```java
public static <E> void swap(List<E> list,int i ,int j);
public static void swap(List<?> list,int i,int j);
```

Public API라면 간단한 두번째가 낫다.

대체적으로 기본 규칙은 이러하다.

메서드 선언에 타입 매개변수가 한번만 나오면 와일드 카드로 대체하라.

- List<?>에는 null 외에는 어떤 값도 넣을수 없다. 와일드카드 타입의 실제 타입을 알려주는 메서드를 만들어 활용하면 된다.

  ```JAVA
  public static void swap2(List<?> list,int i,int j){
  	swapHelper(list, i, j);
  }
  
  private static <E> void swapHelper(List<E> list, int i, int j) {
  	list.set(i, list.set(j, list.get(i)));
  }
  ```

swap 메서드 내부에서는 더 복잡한 제네릭 메서드를 이용했지만, 외부에서는 와일드카드 기반의 멋진 선언을 유지할 수 있게 된다.

즉, 메서드를 호출하는 클라이언트는 복잡한 swapHelper의 존재를 모른 채 그 혜택을 누릴 수 있다.

### 핵심정리

- 조금 복잡해지더라도 와일드카드를 적용하면 API가 유연해진다.
- PECS 공식을 기억하자
  - 매개변수화 타입 T가 생산자라면 <? extends T>를 사용하고 소비자라면 <? Super T>
  - Comparable과 Comparator는 언제나 소비자라는 사실을 잊지 말자.



## 아이템32. 제네릭과 가변인수를 함께 쓸 때는 신중하라

가변인수는 메서드에 넘기는 인수의 개수를 클라이언트가 조절할 수 있게 해주는데, 구현 방식에 허점이 있다.

가변인수 메서드를 호출하면 가변인수를 담기 위한 배열이 자동으로 하나 만들어진다. 그런데 내부로 감춰야 했을 이 배열을 그만 클라이언트에 노출하는 문제가 생겼다. 그 결과 varargs 매개변수에 제네릭이나 매개변수화 타입이 포함되면 알기 어려운 컴파일 경고가 발생한다.

실체화 불가타입(아이템28 참고)은 런타임에는 컴파일타임보다 타입 관련 정보를 적게 담고 있다. 그리고 거의 모든 제네릭과 매개변수화 타입은 실체화 되지 않는다. 메서드를 선언할 때 실체화 불가 타입으로 varargs 매개변수를 선언하면 컴파일러가 경고를 보낸다.

가변인수 메서드를 호출할 때도 varargs 매개변수가 실체화 불가 타입으로 추론되면, 그 호출에 대해서도 경고를 낸다.

```java
warning: [unchecked] Possible heap pollution from parameterized vararg type List<String>
```

매개변수화 타입의 변수가 타입이 다른 객체를 참조하면 **힙 오염**이 발생한다.

- 다른 타입 객체를 참조하는 상홯에서는 컴파일러가 자동 생성한 형변화이 실패할 수 있으니, 제네릭 타입 시스템이 약속한 타입 안전성의 근간이 흔들려 버린다.

```java
static void dangerous(List<String>... stringLists) {
  List<Integer> integers = Arrays.asList(42);
  Object[] objects = stringLists;
  objects[0] = integers;              // 힙 오염 발생
  String s = stringLists[0].get(0);   // ClassCastException
}
```

- 마지막 줄에 컴파일러가 생성한(보이지 않는) 형변환이 숨어 있어 ClassCastException이 발생한다.

이처럼 제네릭 varargs 배열 매개변수에 값을 저장하는 것은 안전하지 않다.



### 제네릭 배열을 프로그래머가 직접 생성하는 건 허용하지 않으면서 제네릭 varargs 매개변수를 받는 메서드를 선언 할 수 있게 한 이유는?

제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 메서드가 실무에서 매우 유용하기 때문이다.

자바 라이브러리도 이런 메서드를 여럿 제공한다. (다행히 아래 메서드들은 타입 안전한다.)

- Arrays.asList(T... a)
- Collections.addAll(Collection<? Super T) c, T... elements)
- EnumSet.of(E first, E... rest)



### @SafeVarargs

자바 7에서는 @SafeVarargs 애너테이션이 추가되어 제네릭 가변인수 메서드 작성자가 클라이언트 측에서 발생하는 경고를 숨길 수  있게 되었다.

@SafeVarargs 애너테이션은 메서드 작성자가 그 메서드가 타입 안전함을 보장하는 장치다.

메서드가 안전하게 확실하지 않다면 절대 @SafeVarargs 달아서는 안된다.

#### 메서드가 안전한지 어떻게 확신할 수 있을까?

가변인수 메서드를 호출할 때 varargs 매개변수를 담는 제네릭 배열이 만들어진다.

- 메서드가 이 배열에 아무것도 저장하지 않고

- 그 배열의 참조가 밖으로 노출되지 않는다면

타입안전하다.

즉, varargs 매개변수 배열이 호출자로부터 그 메서드로 순수하게 인수들을 전달하는 일만 한다면(=varargs의 목적) 그 메서드는 안전하다.

varargs 매개변수 배열에 아무것도 저장하지 않고도 타입 안전성을 깰 수 있다.

- 가변인수로 넘어온 매개변수들을 배열에 담아 반환하는 제네릭 메서드

```java
//자신의 제네릭 매개변수 배열의 참조는 노출
static <T> T[] toArray(T... args){
  return args;
}
```

이 메서드가 반환하는 배열의 타입은 이 메서드에 인수를 넘기는 컴파일타입에 결정되는데, 그 시점에는 컴파일러에게 충분한 정보가 주어지지 않아 타입을 잘못 판단할 수 있다. 따라서 자신의 varargs 매개변수 배열을 그대로 반환하면 힙 오염을 이 메서드를 호출한 쪽의 콜스택으로까지 전이하는 결과를 낳을 수 있다.

```java
static <T> T[] pickTwo(T a,T b,T c){
  Random random = new Random();
  switch (random.nextInt(3)){
    case 0: return toArray(a,b);
    case 1: return toArray(a,c);
    case 2: return toArray(b,c);
  }
  throw new AssertionError();
}
```

이 메서드는 제네릭 가변인수를 받는 toArray메서드를 호출한다는 점만 빼면 위험하지 않고 경고도 내지 않을 것이다.

이 메서드를 본 컴파일러는 toArray에 넘길 T 인스턴스 2개를 담을 varargs 매개변수 배열을 만드는 코드를 생성한다. 배열의 타입은 Object[]

- Object[] : pickTwo에 어떤 타입의 객체를 넘기더라도 담을 수 있는 가장 구체적인 타입

그리고 toArray메서드가 돌려준 이 배열이 그대로 pickTwo를 호출한 클라이언트까지 전달된다. 즉, pickTwo는 항상 Object[] 타입배열을 반환한다.

```java
public static void main(String[] args) {
  String[] attributes = pickTwo("좋은","빠른","저렴한");
}
//Exception in thread "main" java.lang.ClassCastException: [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
//	at com.study.toy.item_32.VarArgsTest.main(VarArgsTest.java:19)
//
//Process finished with exit code 1

```

문제가 없는 메서드라 별다른 경고 없이 컴파일된다. 하지만 실행하면 ClassCastException을 던진다.

- pickTwo 반환값을 attributes에 저장하기 위해 String[]로 형변환하는 코드를 컴파일러가 자동 생성하고 있다.
- Object[]은 String[]의 하위 타입이 아니므로 이 형변환은 실패한다.

이 예제는 **제네릭 varargs 매개변수 배열에 다른 메서드가 접근하도록 허용하면 안전하지 않다**는 점을 다시 한번 상기시킨다.

두가지 예외가 있다.

1. @SafeVarargs로 제대로 애노테이트된 또 다른 varargs 메서드에 넘기는 것은 안전하다.
2. 그저 이 배열 내용의 일부 함수를 호출만 하는(varargs를 받지 않는) 일반 메서드에 넘기는 것도 안전하다.



```java
// 제네릭 varargs 매개변수를 안전하게 사용하는 전형적인 예시
// 임의 개수의 리스트를 인수로받아, 받은 순서대로 그 안의 모든 원소를 하나의 리스트로 옮겨 담아 반환한다.
@SafeVarargs
static <T> List<T> flatten(List<? extends T>... lists){
  List<T> result = new ArrayList<>();
  for (List<? extends T> list : lists) {
  	result.addAll(list);
  }
  return result;
}
```



#### @SafeVarargs 사용 규칙

- 제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 모든 메서드에 @SafeVarargs를 달아라

  - 그래야 사용자가 헷갈리게 하는 컴파일러 경고를 없앨 수 있다.
  - 안전하지 않은 varargs 메서드는 절대 작성해서는 안된다.

- 제네릭 varargs 매개변수를 사용하며 힙 오염 경고가 뜨는 메서드가 있다면, 그 메서드가 진짜 안전한지 점검하라

  - 다음 두 조건을 모두 만족하는 제네릭 varargs 메서드는 안전하다.

    - Varargs 매개변수 배열에 아무것도 저장하지 않는다.
    - 그 배열(혹은 복제본)을 신뢰할 수 없는 코드에 노출하지 않는다.

    

@SafeVarargs 애너테이션만이 유일한 정답이 아니다. '배열보다는 리스트를 사용하라'(아이템28)의 조언에 따라 varargs 매개변수를 List 매개변수로 바꿀 수 있다.

- 이 방식의 장점은 컴파일러가 이 메서드의 타입 안전성을 검증 할 수 있다는 데 있다.
- 단점은 클라이언트 코드가 살짝 지저분해지고 속도가 조금 느려질 수 있다.

```java
static <T> List<T> flatten(List<List<? extends T>> lists){
  List<T> result = new ArrayList<>();
  for (List<? extends T> list : lists) {
    result.addAll(list);
  }
  return result;
}
```

```java
static <T> List<T> pickTwo(T a, T b, T c) {
  Random random = new Random();
  switch (random.nextInt(3)) {
    case 0:
    	return Arrays.asList(a, b);
    case 1:
    	return Arrays.asList(a, c);
    case 2:
    	return Arrays.asList(b, c);
  }
  throw new AssertionError();
}
```



### 핵심정리

가변인수와 제네릭은 궁합이 좋지 않다.

가변인수 기능은 배열을 노출하여 추상화가 완벽하지 못하고, 배열과 제네릭의 타입 규칙이 서로 다르기 때문이다.

제네릭 varargs 매개변수는 타입 안전하지는 않지만, 허용된다.

메서드에 제네릭 (혹은 매개변수화된) varargs 매개변수를 사용하고자 한다면, 먼저 그 메서드가 타입 안전한지 확인한 다음 @SafeVarargs 애너테이션을 달아 사용하는데 불편함이 없게끔 하자.



## 아이템 33. 타입 안전 이종 컨테이너를 고려하라

### 컨테이너?

객체의 '저장' 이라는 관점에서 가장 유명한 방법 중 하나는 배열(array)이다. 특히나 원시(primitive)타입의 값들을 저장하여 다룰때 배열을 많이 사용한다.

하지만 '크기가 한번 정해지면 바꿀 수 없다' 라고 하는 것은 배열의 가장 큰 단점이며, 그로인한 제약은 상당히 크다.

이러한 문제의 해결 방안으로 java.util 라이브러리에는 컨테이너(container) 클래스 들이 있으며, 그것의 기본 타입들은 List, Set, Queue, Map 이다.

### 타입 안전 이종 컨테이너 패턴은 언제 쓰일까?

Set<E>, Map<K,V> 처럼 **클래스 레벨에서 매개변수화 할 수 있는 타입의 수는 제한적**이다. (ex. Map 은 2개)

**타입의 수에 제약없이 유연하게 필요한 경우,** **특정 타입 외에 다양한 타입을 지원해야하는 경우가 있을 수 있다.** 

예를 들어 데이터베이스의 행은 임의 개수의 열을 가질 수 있는데, 모든 열 타입을 안전하게 이용 하고 싶을 때이다.

### 어떻게 해야할까?

컨테이너 대신 키를 매개변수화한 다음 컨테이너에 값을 넣거나, 뺄때 키 타입을 제공해주면 된다. 

이렇게 하면 제네릭 타입 시스템이 값의 타입이 키와 같음을 보장해 줄 것이다.

이러한 설계 방식을 타입 안전 이종 컨테이너 패턴이라고 한다. 

```java
public class Favorites{
	public <T> void putFavorite(Class<T> type, T instance);
	public <T> getFavorite(Class<T> type);
}
```

```java
public static void main( final String[] args ){
  Favorites f = new Favorites();

  f.putFavorite(String.class, "java");
  f.putFavorite(Integer.class, 0xcafebabe);
  f.putFavorite(Class.class, Favorites.class);

  String favoriteString = f.getFavorite(String.class);
  int favoriteInteger = f.getFavorite(Integer.class);
  Class<?> favoriteClass = f.getFavorite(Class.class);

  System.out.printf("%s %x %s %n", favoriteString, favoriteInteger, favoriteClass.getName());
}
```



### 핵심정리

컬렉션 API로 대표되는 일반적인 제네릭 형태에서는 한 컨테이너가 다룰 수 있는 타입 매개변수의 수가 고정되어 있다.

하지만 컨테이너 자체가 아닌 키를 타입 매개변수로 바꾸면 이런 제약이 없는 타입 안전 이종 컨테이너를 만들 수 있다.

타입 안전 이종 컨테이너는 Class를 키로 쓰며, 이런 식으로 쓰이는 Class 객체를 타입 토큰이라 한다.

또한, 직접 구현한 키 타입도 쓸 수 있다. 예컨대 데이터베이스의 행(컨테이너)을 표현한 DatabaseRow 타입에는 제네릭 타입인 Column<T>를 키로 사용할 수 있다.