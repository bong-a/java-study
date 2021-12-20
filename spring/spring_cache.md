# Spring Cache

## cache vs buffer

- "버퍼"와 "캐시"라는 용어는 서루 바꿔가면 쓸 수 있다.

- 하지만 버퍼는 빠른 엔티티와 느린 엔티티 중간에 데이터를 임시로 저장하는데 보통 사용된다.

  - 어떤 부분이 대기해야 해서 다른 성능에 영향을 준다면 작은 청크가 아니라 데이터의 전체 블록을  한 번에 옮기게 해서 이 성능 저하를 완화한다.
  - 데이터는 버퍼에서만 읽고 쓰인다. 게다가 버퍼는 버퍼를 알고 있는 최소 한 부분에서는 가시적이다.

- 반면에 캐시는 숨겨져 있고 관련된 부분이 캐싱 된다는 것을 알지도 못한다.

  - 같은 데이터를 빠르게 여러번 읽음으로써 성능을 높인다.


<br/>
  
## `@Cacheable`

캐시할 수 있는 메서드를 지정하는데 사용한다.

결과를 캐시에 저장하므로 뒤이은 호출(같은 인자일 때)에는 실제로 메서드를 실행하지 않고 캐시에 저장된 값을 반환한다.

```java
@Cacheable
public Book findBook(String isbn){...}
```

캐시는 본질적으로 키-밸류 저장소이므로 캐시된 메서드를 호출할 때마다 해당 키로 변환되어야 한다.

캐시 추상화는 다음 알고리즘에 기반을 둔 KeyGenerator를 사용한다.

- 파라미터가 없으면 0을 반환한다
- 파라미터가 하나만 있으면 해당 인스턴스를 반환한다.
- 파라미터가 둘 이상이면 모든 파라미터의 해시를 계산한 키를 반환한다.

이 접근은 객체를 리플랙션하는 hashCode()처럼 일반적인 키를 가진 객체와 잘 동작한다. 이러한 경우가 아니고 분산 혹은 유지되는 환경이라면 객체가 hashCode를 보관하지 않도록 전략을 변경해야한다. 사실 JVM 구현체나 운영하는 환경에 따라 같은 VM 인스턴스에서 hashCode를 다른 객체에서 재사용할 수 있다.
<br/>

### `@Cacheable` logging

```xml
# logback.xml
<logger name="org.springframework.cache" level="trace">
	<appender-ref ref="STDOUT" />
</logger>
```
<br/>
### key generator
다른 키본 키 생성자 제공하려면 KeyGenerator 인터페이스를 구현해야 한다.

KeyGenerator는 bean으로 등록되어야하며, keyGenerator에 bean 이름을 넣어주면 된다.

```java
class CustomKeyGenerator implements KeyGenerator{
  @Override
  Object generate(Object target, Method method,Object... params){
    //....
  }
}

@Configuration
public class cacheConfig(){
  @bean
  public KeyGenerator cacheKeyGen(){
    return new CustomKeyGenerator();
  }
}

@Cacheable(value="book",keyGenerator="cachekeyGen")
public Book findBook(String isbn){...}
```
<br/>

### conditional

때로는 메서드를 항상 캐싱하는 것이 적합하지 않을 수 있다

캐시 어노테이션은 true/false가 되는 SpEL 표현식을 받는 conditional 파라미터로 이러한 기능을 지원한다.

true이면 캐시하고 false이면 메서드가 캐시되지 않은 것처럼 동작해서 해당 값이 캐시가 되었든 인자가 무엇이든 간에 매번 실행된다.

```java
@Cacheable(value="book",condition="#name.length < 32")
public Book findBook(String name)
```

<br/>

## error handling
기본적으로 SimpleCacheErrorHandler가 사용되는데 이 클래스는 단순히 이셉션을 클라이언트에게 던진다.
그러나 실제로 연결 문제 또는 캐시 서버 다운으로 인해 캐시에서 검색에 실패하더라도 주요 기능에는 영향을 미치지 않아야한다.
스프링에서는 이러한 상황을 처라히가 위한 캐시 에러 핸들러 전략인 CacheErrorHandler 인터페이스를 제공한다.

```java
@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {
    @Override
    public void handleCacheGetError(RuntimeException e, Cache cache, Object o) {
        log.error(e.getMessage(), e);
    }

    @Override
    public void handleCachePutError(RuntimeException e, Cache cache, Object o, Object o1) {
        log.error(e.getMessage(), e);
    }

    @Override
    public void handleCacheEvictError(RuntimeException e, Cache cache, Object o) {
        log.error(e.getMessage(), e);
    }

    @Override
    public void handleCacheClearError(RuntimeException e, Cache cache) {
        log.error(e.getMessage(), e);
    }
}
```
CustomCacheErrorHandler는 CachingConfigurerSupport를 상속한 Configuration에 Orverride 해준다
```java
@Configuration
public class CachingConfiguration extends CachingConfigurerSupport {  
    @Override
    public CacheErrorHandler errorHandler() {
        return new CustomCacheErrorHandler();
    }
}
```
출처 : https://hellokoding.com/spring-caching-custom-error-handler

<br/>

## spring redis entity별로 TTL 지정

```java
@Bean(name = "cacheManager")
public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    //default
    RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .entryTtl(Duration.ofSeconds(30))
            .computePrefixWith("cache-prefix")
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

    // entity별 ttl 설정
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
    cacheConfigurations.put("OTHER-CACHE-1", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(60)));
    cacheConfigurations.put("OTHER-CACHE-1", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(120)));

    return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(connectionFactory)
            .cacheDefaults(configuration)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
}
```

출처 : https://www.skyer9.pe.kr/wordpress/?p=1571

<br/>

## feign @cacheable 

spring feign 사용시 feign client에는 @Cacheable 어노테이션을 붙여도 캐싱 로직이 타지 않았음
하지만, 최근문서(https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#feign-caching)를 보면 @Cacheable을 지원하는 것을 볼수 있음

이전 버전 문서들을 보면 @Cacheable 언급이 없는것으로 보아 3.1.0부터 지원하는 것으로 보임.
spring cloud open feign은 spring cloud 2020.0.5버전에서 3.0.6을 사용하는것으로 보아, 실 프로젝트에서 사용하기까지는 시간이 걸릴 것 같음... 스프링 버전또한 올려줘야하니까...

#### spring boot <> spring cloud
spring boot에서 spring cloud 사용할 때는, 반드시 호환되는 버전을 사용해줘야하낟. 그렇지 않으면 부트 기동에 실패를 하게된다.
스프링 버전별 spring cloud 버전 보기 : https://spring.io/projects/spring-cloud
