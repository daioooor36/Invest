# 자산 투자 서비스
## 설명
### 1. 개요
>**투자 서비스 API 개발**

- 사용자는 원하는 상품에 투자한다.
- 투자상품이 오픈될 때, 다수의 고객이 동시에 투자한다.
- 투자되면 누적 투자모집 금액, 투자자 수가 증가한다.

### 2. 기능 요구 사항
- UI를 제외한 간소화 REST API를 구현한다.
- 각 기능/제약사항에 대한 단위테스트를 작성한다.
- Parameter는 HTTP Header를 통해서 전달한다.
- 에러응답/코드에 대해 정의한다.
- 다수의 서버의 다수의 인스턴스 동작을 고려해야 한다.

### 3. 요구 사항에 따른 상세 기술 구현 사항
- **전체 투자상품 조회 API**
- **투자하기 API**
- **나의 투자상품 조회 API**

---

## 설계
### 1. 개발 프레임워크
- **Spring Boot**
- **JDK 1.8**
- **JPA**
- **JUnit4**
- **H2 DataBase**

### 2. 문제해결 전략
1. **프레임워크 설계**
	+ Spring Boot
		```
		- 원활한 개발을 위해 내장 톰캣 활용
		- 필요 의존성 통합관리로 유지보수 용이(개별 버전관리는 나쁠 수있음)
		```
	+ Packaging 전략 (com.assets.invest.*)
		> com
		>> assets
		>>> invest
		>>>> controller
		>>>> <br>
		>>>> domain
		>>>> <br>
		>>>> enums
		>>>> <br>
		>>>> function
		>>>> <br>
		>>>> persistence
		>>>> <br>
		>>>> service
	+ @RestController
		```
		- REST API 구현
		- Json형태로 반환
		- 적합한 Http Method 사용 강제
		```
	+ JPA
		```
		- Java ORM 표준 준수
		- 특정 DBMS 종속성 제거(Native Query제외)
		- DB OBject DDL 명시화
		```
	+ Database
		```
		- H2 : 비 설치, 콘솔 사용 간편 및 JDBC API를 지원
		```
	+ Dependencies
		```
		- Lombok
		- Spring Web
		- Spring Boot DevTools
		- JPA
		- H2 Database
		```

<br>

2. **전역 코딩 전략**
	+ 객체지향 생활체조 일부 준수
		```
		- 한 코드블록 내에서 한 단계의 들여쓰기만 한다.(메소드 분리 지향)
		- 원시값과 문자열을 포장한다.
		- 한 줄에 점 하나만 사용한다.(예외가능)
		- 필드명은 줄여쓰지 않는다.
		- Setter를 쓰지 않는다.
		```
	+ 투자 주문 Entity 내 Static Factory Method 선언
		```
		- 총 모집 금액에 벗어나는 금액의 인스턴스를 생성하는 것을 방지한다.
		- 테스트 코드에서의 '투자' Insert시 Data Integrity를 보장한다.
		```
	+ final 활용
		```
		final : 인스턴스의 불변 필드
		static final : 클래스의 불변 필드
		```

<br>

3. **Controller 예외 처리(\@RestControllerAdvice)**
	+ Missing Request Header Exception
		* 투자하기() 의 파라미터가 하나라도 누락된 경우
	+ Number Format Exception
		* 투자하기() 의 파라미터 자료형이 잘못 들어온 경우
	+ Validation Exception
		* 투자하기() 에서 투자 불가능한 금액으로 '투자' 객체의 인스턴스를 생성하는 경우
	+ Null Pointer Exception
		* 나의 투자상품 조회() 의 조회 값이 없을 경우

### 3. Entity 설계
#### IV_USER(사용자 정보)
| USER_ID(PK) | USER_NM | PASSWORD | LAST_PW_CHANGED_AT | CREATED_AT | CREATED_BY | UPDATED_AT | UPDATED_BY |
| -- | ------ | --------- | ----- | ----- | ---- | ---- | ---- |
| int | String | String | Date | Date | String | Date | String |
| 101 | 홍길동 | 1234 | 2021-03-01 08:00:00 | 2021-03-01 08:00:00 | SYSTEM     | 2021-03-02 09:00:00 | SYSTEM  |

#### IV_PRODUCT(투자 상품 정보)
| PRODUCT_ID(PK) | PRODUCT_NM          | TOTAL_INVESTING_AMOUNT | STARTED_AT          | FINISHED_AT         | CREATED_AT          | CREATED_BY | UPDATED_AT          | UPDATED_BY |
| -------------- | ------------------- | ---------------------- | ------------------- | ------------------- | ------------------- | ---------- | ------------------- | ---------- |
| int | String          | int | Date          | Date         | Date          | String | Date          | String |
| 1,001            | 개인신용 포트폴리오 | 1,000,000              | 2021-03-01 00:00:00 | 2021-03-08 00:00:00 | 2021-03-01 06:00:00 | SYSTEM     | 2021-03-01 07:00:00 | SYSTEM     |
| 1,002            | 부동산 포트폴리오   | 5,000,000              | 2021-03-02 00:00:00 | 2021-03-09 00:00:00 | 2021-03-01 06:00:00 | SYSTEM     | 2021-03-01 06:30:00 | SYSTEM     |

#### IV_INVEST_ORDER(투자 주문 내역)

| ORDER_ID(PK) | USER_ID(FK) | PRODUCT_ID(FK) | INVESTING_AMOUNT | INVESTED_AT         | CREATED_AT          | CREATED_BY | UPDATED_AT          | UPDATED_BY |
| ------------ | ----------- | -------------- | ---------------- | ------------------- | ------------------- | ---------- | ------------------- | ---------- |
| int | int | int | int | Date         | Date          | String | Date          | String |
| 10,001        | 101  | 1,001            | 300,000          | 2021-03-03 12:00:00 | 2021-03-03 12:00:00 | SYSTEM     | 2021-03-03 12:00:00 | SYSTEM     |

---

## 기능 개발 전략
#### 1. API 전략
1. **전체 투자 상품 조회 API**
	>+ URL : **~/Invest/products**
	>+ 파라미터 : 없음
	>+ 특징
	>	* **투자자 수**를 구할 때, 동일 상품의 투자자는 한 명으로 본다.

2. **투자하기 API**
	>+ URL : **~/Invest/invest/{productId}**
	>+ 파라미터(Header)
	>	* X-USER-ID : 사용자 식별 값
	>	* X-INVESTING-AMOUNT : 투자 금액
	>+ 특징
	>	* REST API를 위해 상품 ID를 URI로써 적용함
	>	* 동일 상품에 대한 유저의 **투자 횟수 제한**은 없음

3. **나의 투자상품 조회 API**
	>+ URL : **~/Invest/search/myInvests**
	>+ 파라미터(Header)
	>	* X-USER-ID : 사용자 식별 값

#### 2. 다수의 서버에서 다수의 인스턴스로의 동작 보장
+ 동시성 제어
	* 인스턴스 메소드 내 **sychronized** 코드블록 사용
	> **synchonized**
	>
> > Multi thread환경에서 최신 값 read/write를 보장 **(가장 적합)**
	
	> volatile
>
	> > Multi thread환경에서 최신 값 read를 보장
	
	> Atomic
	>> 특정 Thread의 블록 lock으로 wait되는 것을 개선
>> <br>
	>> 메모리 비교 후, 작업 성공 시까지 무한루프
	
+ 트랜잭션 수행
	* 인스턴스 메소드에 **@Transacional** 선언
	> **@Transactional**
	>> 명시한 Class, Method에 대해 트랜잭션 생성
	>> <br>
	>> synchronized한 블록 종료 후, commit되도록 함

#### 3. 나(USER)의 투자(INVEST_ORDER) 상품(PRODUCT) 조회
+ **ManyToOne**, **OneToMany** 양방향 연관 관계 설정
	> INVEST_ORDER : USER = Many : One
	> <br>
	> INVEST_ORDER : PRODUCT = Many : One

+ **ManyToOne(FetchType=LAZY)** 로 설정
	
	> 참조된 USER, PRODUCT를 사용하지 않는 경우의 처리속도 제고를 위함

#### 4. 결과 메시지 Return
+ **Enum**
	* 결과 상태/코드/메시지 를 class 상수로 제어하다보니 코드 가독성이 떨어짐.
	* Enum으로 감싸서 getter메소드를 통해 Read하도록 함.
+ **Functional Interface**
	* Enum을 Map으로 변환하는 메소드를 interface화 시킴.
	* 동일한 입력의 동일한 출력(Enum의 값을 그대로 Map화)을 보장하기 위함.

<div style="text-align: right"> ■ </div>
