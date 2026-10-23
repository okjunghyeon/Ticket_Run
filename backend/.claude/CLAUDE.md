# Spring Boot 도메인 개발 가이드

Spring Boot + Spring Data JPA 백엔드의 도메인 단위 코드 작성 규칙. `{Domain}` 은 도메인명으로 치환한다 (예: `Order`).

## 1. 패키지 구조

도메인이 최상위, 레이어가 하위다 (Package by Feature). 필요 없는 패키지는 만들지 않는다.

```
{basePackage}
├── common/
│   ├── config/
│   ├── exception/          GlobalExceptionHandler, CustomException, ErrorCode (인터페이스만)
│   ├── model/              BaseEntity, BaseResponse, PageResponse, ErrorDetail
│   └── client/             외부 서비스 연동 클라이언트
└── domain/{domain}/
    ├── {subDomain}/        도메인이 커지면 분리 (구조 동일)
    ├── api/                {Domain}Controller
    ├── docs/               {Action}{Domain}ApiDocs (요청 시에만 생성, 2장 참고)
    ├── exception/          {Domain}Exception, {Domain}ExceptionCode
    ├── mapper/             {Domain}Mapper
    ├── model/
    │   ├── dto/request/    {Domain}Request, {Domain}SearchRequest, {Action}{Domain}Request
    │   ├── dto/response/   {Domain}Response, {Domain}DetailResponse
    │   └── entity/         {Domain}, 중간 테이블은 {Parent}{Child}
    ├── repository/         {Domain}Repository, {Domain}RepositoryCustom/Impl (QueryDSL 사용 시)
    └── service/            {Domain}Service
```

- 새 도메인은 기존 도메인의 구조와 코드 스타일을 그대로 따른다.
- 다른 도메인과의 협력은 그 도메인의 Service를 통하며, 반환은 존재 확인(`existsById`)이나 필요한 값만 담은 작은 DTO로 제한한다. 다른 도메인의 Entity를 주고받지 않는다.

## 2. Controller

- URL은 `/api/{복수형-kebab-case}`, 리소스는 `/{id}`, 하위 행위는 명사형 서브 경로 (`/{id}/status`, `/my`).
- 상태 코드: POST `201`, GET·PATCH `200` (전체 교체일 때만 PUT), DELETE `204`. 모든 핸들러에 `@ResponseStatus` 명시.
- `@Valid` 를 적용한 DTO를 그대로 Service에 넘긴다. Entity 생성·반환, 비즈니스·권한 판단은 하지 않는다 (인증 여부 확인까지만).
- 인증 정보는 `@AuthenticationPrincipal Long memberId` 한 가지 방식으로만 받는다. principal 은 Access 토큰 subject 의 회원 ID이며, 역할이 필요하면 Service에서 회원을 조회해 판단한다.
- URL 권한: `/api/admin/**` ADMIN, `/api/organizer/**` ORGANIZER, 그 외 인증 필요. 권한 계층은 ADMIN > ORGANIZER > AUDIENCE (`SecurityConfig`).
- 입력
  - JSON: `@RequestBody @Valid`
  - 파일: `multipart/form-data` + `@RequestPart("request")` / `@RequestPart("files")`
  - 검색: 쿼리 파라미터를 `@Valid {Domain}SearchRequest` record로 바인딩
  - 페이징: `@PageableDefault(size = N, sort = "createdAt", direction = DESC) Pageable`
- 응답은 단건 `BaseResponse<T>(code, data, message)`, 목록 `PageResponse<List<T>>`(`code, data, message` + 페이지 정보), 실패 `ErrorDetail(code, errorCode, message, errors)` 로 통일한다.
  - `code` 는 모든 응답에서 실제 HTTP 상태 번호(int)다. 성공 응답은 `ResponseCodeAdvice` 가 응답 직전에 채우므로 컨트롤러는 `new BaseResponse<>(data, message)` / `new BaseResponse<>(message)` 만 쓰고 code를 직접 넘기지 않는다. 상태는 `@ResponseStatus` 또는 `ResponseEntity` 로만 정한다.
  - 실패 응답의 `errorCode` 는 `ErrorCode` enum name이고, `errors` 는 `@Valid` 검증 실패 시에만 포함된다 (7장 참고).

```json
{ "code": 201, "data": { }, "message": "생성했습니다." }
{ "code": 400, "errorCode": "INVALID_INPUT", "message": "입력값이 올바르지 않습니다.",
  "errors": [{ "field": "name", "rejectedValue": "", "reason": "이름은 필수입니다." }] }
```
- **Swagger는 기능 구현이 끝난 뒤 개발자가 요청할 때만 적용한다. 임의로 `@Tag`, `docs/` 어노테이션을 생성하지 않는다.** 요청받으면 `@Tag` 는 클래스에, 메서드에는 `@{Action}{Domain}ApiDocs` 하나만 붙이고 `@Operation`·`@ApiResponses` 는 docs 안에 작성한다.
- 비즈니스 규칙이나 예외 조건이 있는 public 메서드에는 Javadoc(`@param`, `@return`, `@throws`)을 작성한다.

## 3. Service

- 인터페이스 없이 구체 클래스로 작성한다. 주입은 `@RequiredArgsConstructor` + `private final`.
- 트랜잭션은 메서드 단위: 쓰기 `@Transactional`, 조회 `@Transactional(readOnly = true)`. 조회에 쓰기가 섞이면(조회수 증가 등) 쓰기로 둔다. 외부 API·파일 업로드는 트랜잭션 범위를 최소화한다.
- Create/Update/Delete 흐름
  1. 요청자 정보·권한 확인
  2. `repository.findById(id).orElseThrow(() -> new {Domain}Exception(XXX_NOT_FOUND))`
  3. 엔티티 검증 메서드 호출 (`entity.validateUpdate(userId, roles)`)
  4. 연관 데이터 처리 (자식 컬렉션 교체, 파일 처리)
  5. 상태 변경: 수정·삭제는 엔티티 메서드 + Dirty Checking, 신규 생성은 `save()`
  6. Response DTO로 변환해 반환
- Entity를 외부로 반환하지 않는다. 다른 도메인이 호출하는 메서드는 public으로 두고 용도를 주석으로 표시한다.
- DTO 변환: 단일 엔티티 1:1이면 record의 `static from(Entity)`, 그 외에는 `{Domain}Mapper` static 메서드 + `@Builder`.
- 페이징 결과는 `PageResponse` 로 변환한다 (Spring `Page` 노출 금지).
- 외부 서비스는 `client` 를 감싼 private 헬퍼로 호출한다.
- 캐시성 데이터(최근 본 항목, 랭킹)는 전용 Service로 분리하고 키 포맷·TTL·최대 개수를 상수로 선언한다 (예: `"user:%s:recent"`).
- 매직 넘버는 `private static final` 상수로 선언한다. private 헬퍼는 하단 `/* ===== private Method ===== */` 아래에 모은다.

## 4. Repository

- `JpaRepository<Entity, IdType>` 상속. 파생 쿼리 메서드를 우선하고, 이름이 과하게 길어지면 `@Query` JPQL(텍스트 블록 `"""`)을 쓴다.
- 동적 검색: 선택 조건이 2개 이하면 `@Query` 의 `(:x IS NULL OR ...)`, 3개 이상이면 QueryDSL `{Domain}RepositoryCustom` + `{Domain}RepositoryImpl` 로 구현한다.
- 카운터·재고 등 동시 갱신 필드는 `@Modifying` 원자 UPDATE(`SET c = c + 1`)를 쓴다. 같은 트랜잭션에서 엔티티 값을 응답에 쓰면 엔티티에도 반영하거나 재조회하고 이유를 주석으로 남긴다.
- Soft Delete 사용 시 모든 목록·검색 쿼리에 삭제 상태 조건을 포함한다.
- N+1 방지: 목록 조회는 DTO projection 또는 fetch join / `@EntityGraph` 를 쓴다. 컬렉션 fetch join과 페이징은 함께 쓰지 않고 `default_batch_fetch_size` 로 해결한다.
- `spring.jpa.open-in-view` 는 `false` 로 고정한다. Service 밖에서 지연 로딩에 의존하지 않는다.

## 5. Entity

- `@Entity @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)` + `BaseEntity` 상속.
- `@Setter` 금지. 생성은 생성자 위 `@Builder`, 기본값은 생성자에서 세팅한다.
- `@Enumerated(EnumType.STRING)` 만 사용하고 컬럼명은 `@Column(name = "snake_case")` 로 명시한다.
- 연관관계
  - `@ManyToOne` 은 항상 `FetchType.LAZY`.
  - 생명주기가 같은 자식은 `@OneToMany(mappedBy, cascade = ALL, orphanRemoval = true)` + `new ArrayList<>()` 초기화.
  - `@ManyToMany` 대신 중간 엔티티를 쓴다.
  - 다른 서비스·모듈의 데이터와 생명주기가 다른 Aggregate는 ID로만 참조한다.
- 상태 전이(`markAsXxx`), 자식 컬렉션 관리(`addXxx`, `replaceXxx`), 불변식 검증(`validateXxx`)은 엔티티 메서드로 작성하고 위반 시 도메인 예외를 던진다. 메서드 그룹은 `/* ===== 섹션명 ===== */` 로 나눈다.
- 중복 방지 같은 무결성은 `@UniqueConstraint` 로 DB에서도 보장한다.

## 6. DTO

- Java `record`, 파일 1개 = DTO 1개. Entity를 필드로 포함하지 않는다.
- Request는 `jakarta.validation` 어노테이션에 사용자용 `message` 를 명시하고, 선택 파라미터 기본값은 compact constructor에서 보정한다.
- 필드가 많으면 `@Builder` 를 붙인다. `@Schema` 는 Swagger 적용을 요청받았을 때만 작성한다.

## 7. 예외 처리

- `common/exception` 에는 `ErrorCode` 인터페이스와 `CustomException`, `GlobalExceptionHandler` 만 둔다. common은 도메인의 ExceptionCode를 알지 못한다.
- `ErrorCode` 는 `HttpStatus getStatus()`, `String getMessage()`, `String name()` 을 선언하고, `default String getCode() { return name(); }` 로 문자열 에러 코드를 제공한다.
- 각 도메인은 `domain/{domain}/exception/{Domain}ExceptionCode` enum이 `ErrorCode` 를 구현하고, `{Domain}Exception extends CustomException` 이 이를 받는다: `throw new {Domain}Exception(XXX)` (static import).
- 상태는 `int` 가 아닌 `HttpStatus` 로 선언한다. 응답의 `code` 에는 `getStatus()` 의 상태 번호를, `errorCode` 에는 `getCode()`(enum name, 예: `IMAGE_REQUIRED`)를 내려, 같은 상태 코드 안에서도 클라이언트가 구분할 수 있게 한다.
- 응답 변환은 `GlobalExceptionHandler`(`@RestControllerAdvice`) 한 곳에서만 한다. 하위 도메인은 상위 도메인의 예외를 공유한다.
- 새 예외는 해당 도메인의 `{Domain}ExceptionCode` 안에서 HTTP 상태 섹션에 추가하고, 섹션이 없으면 상태 코드 오름차순 위치에 새로 만든다.

```java
@Getter
@RequiredArgsConstructor
public enum ProductPostExceptionCode implements ErrorCode {

    // ===== 400 Bad Request =====
    IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "이미지는 최소 1개 이상 첨부해야 합니다."),

    // ===== 404 Not Found =====
    PRODUCT_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 글은 존재하지 않습니다."),

    // ===== 503 Service Unavailable =====
    EXTERNAL_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "외부 서비스와의 통신 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
```

## 8. build.gradle 의존성

- 의존성은 `// 범주` 주석 아래 묶어 작성한다. `dependencies` 블록 하단에 단순 나열하지 않는다.
- 추가 전 기존 범주를 확인해 유사한 범주가 있으면 그 안에 넣고, 없으면 새 범주를 만든다. 범주 사이는 빈 줄 하나.
- BOM(`platform`)은 해당 범주 안에 둔다. Spring Boot가 버전을 관리하는 의존성은 버전을 생략하고 그 외에는 명시한다.

```groovy
// redis
implementation 'org.springframework.boot:spring-boot-starter-data-redis'

// s3
implementation 'software.amazon.awssdk:s3:2.37.5'
implementation platform("io.awspring.cloud:spring-cloud-aws-dependencies:3.4.0")
implementation 'io.awspring.cloud:spring-cloud-aws-starter-s3'

// test
testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
testCompileOnly 'org.projectlombok:lombok'
testAnnotationProcessor 'org.projectlombok:lombok'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

## 9. 설정 파일 (yml)

- `application.yml`: 모든 환경의 공통 설정.
- `application-dev.yml`: 개발 환경 전용. `application-prod.yml`: 배포 환경 전용.
- `application-secret.yml`: 암호화가 필요한 값(DB 비밀번호, API 키, 토큰 등)만 관리하며 `.gitignore` 에 등록한다. 커밋하거나 응답·로그에 값을 노출하지 않는다.
- 시크릿 값은 다른 yml에 직접 쓰지 않는다. 새 설정을 추가할 때 공통·환경별·시크릿 중 어디에 속하는지 구분해 해당 파일에 넣는다.

## 10. 테스트

- 실행: `./gradlew test` (전체 검증은 `./gradlew build`).
- JUnit5 + Mockito(BDDMockito) + AssertJ를 사용한다. 파일은 `src/test/java` 에 대상과 같은 패키지로 두고 이름은 `{Domain}ServiceTest` 형식으로 한다.
- 계층별: Service는 Mockito 단위 테스트(`@ExtendWith(MockitoExtension.class)`), Repository는 `@DataJpaTest`, Controller는 `@WebMvcTest`.
- 메서드명은 클래스 안에서 `test1`, `test2` … 순번으로 짓고, 의미는 `@DisplayName` 에 한국어 문장으로 쓴다 (`~할 수 있다.` / `~할 수 없다.`).
- 본문은 `// given` `// when` `// then` 주석으로 나누고, 의존성 동작은 `given(...).willReturn(...)` 으로 준비한다. then에서는 `assertThat` 으로 결과를, `verify` 로 호출 여부(`never()` 포함)를 검증한다.

```java
@DisplayName("ADMIN은 상품 게시글을 생성할 수 있다.")
@Test
void test2() {
    // given
    String userId = "admin-123";
    ProductPostRequest request = ProductPostRequest.builder()
            .title("아이폰 15 Pro")
            .price(1200000)
            .build();

    given(userClient.getUser(userId)).willReturn(UserViewFactory.createAdmin(userId));
    given(productPostRepository.save(any(ProductPost.class))).willReturn(savedProductPost);

    // when
    ProductPostResponse result = productPostService.createProductPost(request, userId, imageFiles);

    // then
    assertThat(result).isNotNull();
    assertThat(result.title()).isEqualTo(request.title());

    verify(productPostRepository).save(any(ProductPost.class));
    verify(tagRepository, never()).findAllById(anyList());
}
```

## 11. 코드 스타일

- 주석·Javadoc·응답/검증 메시지는 팀 공용 언어로 통일한다.
- 의도가 드러나지 않는 코드(동시성 처리, flush 호출, 영속성 컨텍스트 보정)에는 이유를 주석으로 남긴다.
- 미구현 항목은 `// TODO:` 로 표시한다.
