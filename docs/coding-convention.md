# Android Kotlin 코딩 컨벤션

> TU Shuttle 승객 앱의 Kotlin, Jetpack Compose, MVVM Clean Architecture 기준이다.

---

## 0. 규칙 등급

규칙에 별도 표시가 없으면 **MUST**다.

| 등급 | 의미 |
| --- | --- |
| **MUST** | 반드시 지킨다. 위반 시 PR을 병합하지 않는다. |
| **SHOULD** | 합리적인 이유가 없으면 지킨다. 예외는 PR에 이유를 남긴다. |
| **DOMAIN** | `domain` 계층에만 적용하는 규칙이다. |
| **UI** | Compose UI 계층에만 적용하는 규칙이다. |

## 1. Kotlin 네이밍

### 1.1 공통

- 패키지는 모두 소문자, 클래스·인터페이스·enum·data class는 PascalCase, 함수·변수는 camelCase를 사용한다.
- 식별자는 영문으로 작성한다. 한국어 발음을 로마자로 쓰지 않는다.
- 약어는 널리 통용되는 것만 쓴다. `id`, `url`, `api`, `ui`는 허용하고 임의의 축약은 피한다.
- 상수는 `UPPER_SNAKE_CASE`를 쓴다. 파일 내부 전용 상수는 `private const val`로 둔다.
- boolean은 `is`, `has`, `can`, `should`로 시작한다.

```kotlin
private const val LOCATION_TIMEOUT_MS = 10_000L

val hasLocationPermission: Boolean
fun canBoard(bus: Bus): Boolean
```

### 1.2 역할별 접미사

| 역할 | 이름 예시 |
| --- | --- |
| 화면 상태 | `CalendarUiState` |
| ViewModel | `CalendarViewModel` |
| UseCase | `GetRouteSchedulesUseCase` |
| Repository 계약 | `DirectionsRepository` |
| Repository 구현 | `DirectionsRepositoryImpl` |
| 원격 DTO | `DirectionsResponse`, `RouteOption` |
| 데이터 변환 | `toDomain()`, `toEntity()` |
| 화면 | `CalendarScreen` |
| 재사용 UI | `BusInfoCard`, `BottomModal` |

`Manager`, `Helper`, `Util`은 역할이 분명하지 않을 때 쓰지 않는다. 순수 계산은 의미 있는 `UseCase` 또는 `object`로, Android/네트워크 보조 기능은 구체적인 이름의 DataSource 또는 Handler로 만든다.

### 1.3 테스트 이름

- 테스트 클래스는 대상 이름 뒤에 `Test`를 붙인다.
- 테스트 함수는 Kotlin backtick 이름 또는 `given_when_then`을 사용한다.

```kotlin
class ValidateReservationUseCaseTest {
    @Test
    fun `출발지와 도착지가 같으면 SameRoute 오류를 반환한다`() { }
}
```

## 2. Kotlin 선언과 불변성

- 지역 변수와 프로퍼티는 기본적으로 `val`을 쓴다. 변경이 꼭 필요할 때만 `var`를 사용한다.
- nullable 타입은 실제로 값이 없을 수 있을 때만 사용한다. `!!`는 금지한다.
- null 처리에는 early return, `?.let`, Elvis 연산자(`?:`)를 우선 사용한다.
- 외부에 노출하는 컬렉션은 `List`, `Map`, `Set`처럼 읽기 전용 타입으로 선언한다.
- 도메인 모델과 UI 상태는 기본적으로 `data class`와 불변 프로퍼티를 사용한다.
- 제한된 상태·오류·이벤트는 `sealed class` 또는 `enum class`로 표현한다.

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

val nextReservation = reservations.firstOrNull() ?: return null
```

## 3. 포맷과 공백

- 들여쓰기는 공백 4칸이며 탭 문자를 사용하지 않는다.
- 한 줄은 120자 이하를 권장한다. 긴 인자 목록·체이닝은 줄을 나눈다.
- 후행 공백을 남기지 않는다.
- 중괄호는 Kotlin 기본 스타일을 따른다. `else`, `catch`, `finally`는 닫는 중괄호와 같은 줄에 둔다.
- `if`, `when`, `try`는 값을 계산할 수 있으면 표현식으로 사용한다.
- 와일드카드 import는 사용하지 않는다.
- 파일에는 하나의 public 최상위 선언을 기본으로 한다. 같은 모델을 강하게 응집해 둘 필요가 있을 때만 예외로 한다.

```kotlin
val statusText = when (bus.status) {
    BusStatus.RUNNING -> "운행"
    BusStatus.WAITING -> "대기"
    BusStatus.FINISHED -> "운행 종료"
}
```

## 4. 클린 코드 규칙

### 4.1 함수와 클래스

- 함수는 한 가지 책임만 가진다. 이름에 `And`가 필요하면 분리를 검토한다.
- 중첩 조건은 guard clause와 조기 반환으로 줄인다. `else`는 자연스러운 값 선택·`when`을 제외하면 지양한다.
- 함수 인자는 3개 이하를 권장한다. 같은 타입의 인자가 연속되거나 boolean 플래그가 섞이면 모델로 묶는다.
- 클래스가 300줄을 넘거나 책임이 둘 이상이면 분리 여부를 검토한다. 화면은 Route, Section, 재사용 Composable로 나눈다.
- 공개 함수에는 호출자가 알아야 하는 전제·부작용이 있을 때만 KDoc을 쓴다. 코드 자체를 읽으면 알 수 있는 내용을 반복하지 않는다.

### 4.2 도메인 규칙 **(DOMAIN)**

- 도메인 규칙은 UseCase 또는 도메인 모델에 둔다. UI와 Repository 구현체에 비즈니스 판단을 복사하지 않는다.
- 의미 있는 값은 원시값 대신 모델로 묶는 것을 검토한다. 단순 표시·DTO 매핑까지 강제하지 않는다.
- 현재 시간, 위치, 네트워크처럼 변경 가능한 외부 세계는 인터페이스 뒤로 숨긴다.
- 도메인은 Android SDK, Compose, Retrofit, Hilt, `Context`를 import하지 않는다.

```kotlin
class ValidateReservationUseCase @Inject constructor() {
    operator fun invoke(reservation: Reservation): ValidationError? = when {
        reservation.from == reservation.to -> ValidationError.SameRoute
        reservation.days.isEmpty() -> ValidationError.SelectDay
        else -> null
    }
}
```

## 5. MVVM Clean Architecture

### 5.1 디렉터리와 의존성 방향

```text
ui  →  domain  ←  data
        ↑
        di
```

| 계층 | 책임 | 허용 의존성 |
| --- | --- | --- |
| `ui` | Compose 렌더링, 사용자 이벤트, ViewModel | `domain`, AndroidX, Compose |
| `domain` | 모델, UseCase, Repository 인터페이스, 순수 규칙 | Kotlin·coroutines만 |
| `data` | API/DTO, DataSource, Repository 구현, 변환 | `domain`, Retrofit, Android SDK |
| `di` | 구현체와 외부 의존성 연결 | 모든 계층 |
| `service` | Android foreground service 등 플랫폼 수명주기 | `domain`, `data`, Android SDK |

- `ui`는 `data` 구현체·DTO·Retrofit API를 직접 import하지 않는다.
- `domain`은 구현체가 아닌 Repository 인터페이스만 안다.
- `data`는 DTO를 도메인 모델로 변환한 뒤 Repository 계약을 통해 반환한다.
- 계층 경계를 지키기 어려운 공통 Android UI 코드는 `ui/common`에 둔다. domain으로 옮기지 않는다.

### 5.2 Repository와 UseCase

- Repository 인터페이스는 `domain/repository`에, 구현체는 `data/repository`에 둔다.
- Repository는 데이터 출처를 숨긴다. UI가 메모리·DataStore·API 여부를 알면 안 된다.
- UseCase는 단순 위임이라도 화면의 의도를 명확히 하고 테스트 대상을 제공하면 유지한다.
- 외부 응답 DTO를 domain·ui에 노출하지 않는다. `toDomain()` 변환을 Data 계층에 둔다.

```kotlin
interface DirectionsRepository {
    suspend fun getDirections(start: Coordinate, end: Coordinate): AppResult<Directions>
}

class GetDirectionsUseCase @Inject constructor(
    private val repository: DirectionsRepository,
) {
    suspend operator fun invoke(start: Coordinate, end: Coordinate) =
        repository.getDirections(start, end)
}
```

## 6. Presentation: ViewModel과 UI 상태

### 6.1 ViewModel

- ViewModel은 `@HiltViewModel`과 constructor injection을 사용한다. `Context`, View, Activity, Fragment를 보관하지 않는다.
- 화면 상태는 단일 `UiState`의 `StateFlow`로 노출한다. 내부 상태만 `MutableStateFlow`로 둔다.
- 이벤트 함수는 사용자 의도를 나타내게 이름 짓는다. `updateState()` 같은 범용 public 함수는 피한다.
- 비즈니스 판단·네트워크 접근은 UseCase로 넘긴다.
- `viewModelScope`에서 코루틴을 시작하고, 취소 가능한 Flow는 화면 수명주기에 맞게 수집한다.

```kotlin
@HiltViewModel
class MapViewModel @Inject constructor(
    private val getAllBusStops: GetAllBusStopsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun refreshBusStops() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        val stops = getAllBusStops()
        _uiState.update { it.copy(busStops = stops, isLoading = false) }
    }
}
```

### 6.2 UI 상태와 일회성 이벤트

- `UiState`는 불변 `data class`로 만들고 기본값을 제공한다.
- 로딩·내용·오류·선택 상태를 화면 상태에 명시한다. 문자열 오류는 UI에서 표시할 수 있는 메시지로 변환한다.
- Toast, navigation, 권한 요청처럼 재구성에 다시 실행되면 안 되는 동작은 이벤트 또는 UI effect로 분리한다.
- UI가 ViewModel의 내부 구현·Repository·DTO를 알면 안 된다.

### 6.3 Compose **(UI)**

- Route Composable만 `hiltViewModel()`을 호출한다. 하위 Composable은 상태와 콜백을 인자로 받는 stateless UI로 만든다.
- `StateFlow`는 기본적으로 `collectAsStateWithLifecycle()`로 수집한다.
- 비동기 작업은 `LaunchedEffect` 또는 ViewModel에서 시작한다. Composable 본문에서 직접 네트워크 요청·상태 변경을 하지 않는다.
- `remember`는 UI 메모이제이션에만 쓴다. 화면을 다시 열어도 보존되어야 하는 입력은 ViewModel 상태에 둔다.
- `AndroidView`와 지도·권한 같은 플랫폼 API는 Compose lifecycle effect로 생성·해제한다.
- 문자열은 `strings.xml`에 두고 `stringResource()`로 읽는다. 색상·타이포그래피는 theme 토큰을 사용한다.

```kotlin
@Composable
fun CalendarRoute(
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarScreen(
        uiState = uiState,
        onDayClick = viewModel::toggleDay,
    )
}
```

## 7. Data와 네트워크

- Retrofit 인터페이스와 응답 DTO는 `data/api`, `data/model`에 둔다.
- HTTP·직렬화·IO 예외는 Data 계층에서 도메인 오류로 변환한다. 예외 객체를 UI까지 그대로 전달하지 않는다.
- 모든 base URL은 `/`로 끝나야 한다. 코드에 개인 LAN IP·API 키·비밀번호를 하드코딩하지 않는다.
- 앱 비밀값은 `.env` 또는 환경변수에서 읽고, 키 이름만 담은 `.env.example`을 버전 관리한다.
- 디버그 HTTP body 로그는 민감한 요청·응답을 남길 수 있으므로 release 빌드에서 사용하지 않는다.
- API 응답 스키마가 바뀌면 DTO와 `toDomain()` 테스트를 함께 수정한다.

```kotlin
class DirectionsRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : DirectionsRepository {
    override suspend fun getDirections(start: Coordinate, end: Coordinate): AppResult<Directions> =
        NetworkErrorHandler.handleNetworkRequest {
            api.getDirections(start.toQuery(), end.toQuery()).toDomain()
        }
}
```

## 8. DI와 Android 플랫폼 코드

- Hilt는 constructor injection을 우선한다. field injection과 서비스 로케이터는 사용하지 않는다.
- 인터페이스-구현체 바인딩과 외부 SDK 객체는 `di` 모듈에서 제공한다.
- 권한 요청 UI는 Compose/Activity에, 권한 상태 확인과 위치 획득은 DataSource·Repository에 둔다.
- Foreground service는 사용자에게 알리는 동작에만 사용하고, Android 버전별 권한·알림 요구사항을 확인한다.
- `Application`에서 권한이 필요한 작업을 무조건 시작하지 않는다. 권한 승인 후 시작할 수 있게 한다.

## 9. 오류·코루틴·Flow

- 오류 타입은 `domain/error`에서 sealed class로 정의하고, UI 문자열 변환은 `ui/common/ErrorMapper`에 둔다.
- `Result.Success`와 `Result.Error`를 `when`으로 빠짐없이 처리한다.
- `catch`에서 오류를 로그만 남기고 무시하지 않는다. 사용자에게 보여 줄 필요가 없더라도 상태 종료·재시도 정책을 명확히 한다.
- Flow는 Repository가 지속 관찰을 제공해야 할 때만 사용한다. 한 번 조회하는 API는 `suspend` 함수로 충분한지 검토한다.
- 동일한 Flow를 재수집해 중복 요청을 만들지 않는다. 검색 조건이 바뀌는 경우 `flatMapLatest` 등 취소 전략을 사용한다.

## 10. 테스트

- 신규 UseCase와 순수 도메인 계산은 단위 테스트를 작성한다.
- Repository 매핑은 API 응답 fixture로 테스트한다.
- ViewModel은 가짜 Repository/UseCase로 `UiState` 전이와 오류 처리를 테스트한다.
- Compose 화면은 중요한 사용자 흐름과 접근성 문자열을 UI 테스트한다.
- 버그 수정은 재현 테스트를 먼저 추가한다. 테스트를 끄거나 삭제해 빌드를 통과시키지 않는다.

우선 테스트 대상:

1. 예약 유효성 검증과 요일 정렬
2. 시간표·정류장 운행 시간 필터
3. 길찾기 DTO → domain 변환
4. 위치 권한 거부·타임아웃 오류 상태
5. `RUNNING`, `WAITING`, `FINISHED` 버스의 탑승 UI 분기

## 11. 리소스·접근성·보안

- 사용자 노출 문자열, content description, 오류 문구는 `strings.xml`에 둔다.
- 클릭 가능한 아이콘·이미지에는 의미 있는 `contentDescription`을 제공한다. 장식 이미지는 `null`을 명시한다.
- 사용자 위치, 토큰, API 키, 인증 정보는 로그에 남기지 않는다.
- 실제 `.env`, `local.properties`, 서명 키, 생성 APK는 커밋하지 않는다.
- 네트워크 보안 설정은 필요한 호스트와 프로토콜만 허용한다. release에서 cleartext HTTP를 허용하지 않는다.

## 12. Git과 PR

### 12.1 브랜치

- 기본 브랜치에 직접 push하지 않는다.
- 기능은 `feature/{설명}`, 버그는 `fix/{설명}`, 긴급 수정은 `hotfix/{설명}`을 사용한다.
- PR 하나는 하나의 목적을 가진다. 큰 리팩터링과 기능 추가는 가능하면 분리한다.

### 12.2 커밋

- Conventional Commits를 사용한다: `feat`, `fix`, `refactor`, `style`, `docs`, `test`, `build`, `chore`.
- 제목은 변경 이유를 포함해 짧게 작성한다.

```text
feat(map): add directions repository
fix(ride): allow boarding only for running buses
docs: add Android Kotlin coding convention
```

### 12.3 PR 체크리스트

- [ ] `./gradlew :app:compileDebugKotlin` 또는 변경 범위에 맞는 테스트를 통과했다.
- [ ] 새 코드가 `ui → domain ← data` 의존성 방향을 지킨다.
- [ ] API 키·개인 IP·실제 `.env` 파일을 포함하지 않았다.
- [ ] 화면 동작·권한·오류 상태를 확인했다.
- [ ] 문자열과 접근성 설명을 점검했다.

## 13. 개발 환경

- JDK 17을 사용한다.
- Android Studio의 Kotlin 공식 포맷터를 사용하고, 커밋 전 import·format을 정리한다.
- `local.properties`에는 로컬 SDK 경로만 둔다.
- `.env.example`을 복사해 `.env`를 만들고 `NAVER_MAP_CLIENT_ID`, 필요 시 `BASE_URL`을 설정한다.

```bash
cp .env.example .env
./gradlew :app:compileDebugKotlin
```

## 14. 합의 변경

이 문서의 MUST 규칙을 바꾸려면 PR에서 이유와 영향을 설명하고 팀 합의를 남긴다. 규칙이 현재 코드와 충돌할 때는 새 기능부터 적용하고, 기존 코드는 관련 작업을 할 때 점진적으로 정리한다.
