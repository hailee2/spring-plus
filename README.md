# SPRING PLUS

## 서비스
Spring Security 와 JWT를 통해 인증인가를 받은 유저가 할일을 생성,조회,수정,삭제하고 댓글을 달며 할일에 관리자를 설정하는 서비스

## 주요 기능
1. Spring Security + JWT을 사용하여 인증 인가
2. 할일 생성, 삭제, 수정, 삭제
3. 유저 조회, 비밀번호 수정
4. 댓글 생성, 조회
5. 할일 관리자 등록, 조회, 삭제

## HTTP 요청
1. AUTH
 
| HTTP 메서드 | Endpoint      | Request Body                                                                 | Response Body | 상태 코드 |
|-------------|---------------|-------------------------------------------------------------------------------|---------------|-----------|
| POST        | /auth/signup  | { email: string, password: string, userRole: string, nickname: string }      | 없음          | 200 OK    |
| POST        | /auth/signin  | { email: string, password: string }                                          | 없음          | 200 OK    |

2. USER

| HTTP 메서드 | Endpoint              | Request Body                                    | Response Body       | 상태 코드 |
|-------------|-----------------------|------------------------------------------------|--------------------------|-----------|
| PATCH       | /admin/users/{userId} | { role: string }          | 없음                | 200 OK                   |           |
| GET         | /users/{userId}       | 없음                                            | {id: Long, email:string} | 200 OK    |
| PUT         | /users                | { oldPassword: string, newPassword: string }   | 없음                      | 200 OK    |

3. TODO

| HTTP 메서드 | Endpoint            | Request Body                                   | Response Body                                                                                                   | 상태 코드 |
|-------------|---------------------|-----------------------------------------------|------------------------------------------------------------------------------------------------------------------|-----------|
| POST        | /todos              | { title: string, contents: string }            | { id: long, title: string, contents: string, weather: string, user: UserResponse }                       | 200 OK    |
| GET         | /todos              | 없음 (쿼리 파라미터: page, size, weather, startDate, endDate) | Page<{ id: long, title: string, contents: string, weather: string, user: UserResponse, createdAt: datetime, modifiedAt: datetime }>  | 200 OK    |
| GET         | /todos/{todoId}     | 없음                                           | { id: long, title: string, contents: string, weather: string, user: UserResponse, createdAt: datetime, modifiedAt: datetime }| 200 OK    |
| GET         | /todos/search       | 없음 (쿼리 파라미터: types, keywords, pageable) | Page<{ title: string, numOfManager: long, numOfComments: long }>          | 200 OK    |

4. COMMENT

| HTTP 메서드 | Endpoint                     | Request Body                               | Response Body                                                                 | 상태 코드 |
|-------------|------------------------------|-------------------------------------------|-------------------------------------------------------------------------------|-----------|
| POST        | /todos/{todoId}/comments     | { contents: string }                      | { id: long, contents: string, user: UserResponse }                            | 200 OK    |
| GET         | /todos/{todoId}/comments     | 없음                                      | [ { id: long, contents: string, user: UserResponse } ]                         | 200 OK    |

5. MANAGER

| HTTP 메서드 | Endpoint                                | Request Body                                      | Response Body                                                                  | 상태 코드 |
|-------------|-----------------------------------------|--------------------------------------------------|--------------------------------------------------------------------------------|-----------|
| POST        | /todos/{todoId}/managers                | { managerUserId: long }                          | { id: long, user: UserResponse }                                               | 200 OK    |
| GET         | /todos/{todoId}/managers                | 없음                                             | [ { id: long, user: UserResponse } ]                                            | 200 OK    |
| DELETE      | /todos/{todoId}/managers/{managerId}    | 없음                                             | 없음                                                                             | 200 OK    |

## 트러블 슈팅

### 오류 1: 테스트 코드 오류 (TodoController 단위 테스트)

**테스트 목적**  
- `TodoController`에서 `InvalidRequestException` 발생 시 400 Bad Request 반환 확인

**실제 오류**  
- `NoSuchBeanDefinitionException: JwtUtil` 발생  
- 또는 `401 Unauthorized` 발생  

**원인**  
- Spring Security가 테스트 환경에 포함됨  
- `JwtAuthenticationFilter` 로딩 시 `JwtUtil` 빈 필요 → 없음 → 컨텍스트 로딩 실패  
- JWT 인증 필터에서 요청 차단 → 401 Unauthorized 발생  

**해결 방법**  

1. **Security 비활성화 (단위 테스트 목적)**

```java
@WebMvcTest(controllers = TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TodoControllerTest {
    @MockBean private TodoService todoService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private JwtUtil jwtUtil;
}
```

2. Security 활성화 + 가짜 유저 주입 (통합 테스트 목적)
```
@Test
@WithMockUser(username = "testUser", roles = "USER")
void todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다() throws Exception {
    long todoId = 1L;

    when(todoService.getTodo(todoId))
            .thenThrow(new InvalidRequestException("Todo not found"));

    mockMvc.perform(get("/todos/{todoId}", todoId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.message").value("Todo not found"));
}
```

### 오류 2. 403 에러

- **현상**: 토큰을 갖고 todo 생성 등 요청을 보내면 403 에러 발생  
- **원인 분석**:  
  - `setAuthorization()` 메서드 안에서 `getAttribute`로 `userId`, `email`, `userRole` 출력 시 `null` 반환  
  - 유저 값을 가져오지 못하고 있었음  
  - `@Auth` 와 `@AuthenticationPrincipal`이 중복되어 발생한 문제  
- **해결 방법**:  
  - `ArgumentResolver` 삭제 후 정상 동작  
  - → 오류 해결 ✅  


### 오류 3. 데이터베이스 오류

- **현상**: `unknown database 'dacademy'` 에러 메시지 발생  
- **원인**: 데이터베이스 자체가 존재하지 않음  
- **해결 방법**:  
  - `academy` 데이터베이스 다시 생성  
  - → 정상 동작 ✅
```
mysql> CREATE DATABASE academy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; 
mysql> SHOW DATABASES;
+--------------------+
| Database           |
+--------------------+
| academy            |
| information_schema |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
5 rows in set (0.005 sec)
```
