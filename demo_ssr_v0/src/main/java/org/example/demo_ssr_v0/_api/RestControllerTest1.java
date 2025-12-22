package org.example.demo_ssr_v0._api;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
public class RestControllerTest1 {

    // 테스트 주소 : https://jsonplaceholder.typicode.com/todos/{id}
    // 자바 코드로 다른 서버에 요청을 해서 응답을 받아볼 예정

    // 우리 서버에서 받아 줄 주소: http://localhost:8080/todos/1
    // http://localhost/8080/todos/1 --> https://jsonplaceholder.typicode.com/todos/{id}
    // --> 우리에게 다시 응답 날라 옴
    @GetMapping("/todos/{id}")
    public ResponseEntity<?> test1(@PathVariable Integer id) {

        // 1. URI 객체 생성 (주소 만들기)
        URI uri = UriComponentsBuilder
                .fromUriString("https://jsonplaceholder.typicode.com") // 진입점
                .path("/todos")
                .path("/" + id)
                .encode() // 주소에 한글이나 특수문자가 있을 경우 안전하게 변환함
                .build()
                .toUri();

        System.out.println("생성된 URI 주소 : " + uri.toString());

        // 2. RestTemplate 객체 생성 (HTTP 통신 도구)
        RestTemplate restTemplate = new RestTemplate();

        // 3. GET 방식으로 요청 보내기
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        // { "userId": 1, "id": 1, "title": "delectus aut autem", "completed": false }
        // String 으로 받으면 response.getBody().userId; 사용 불가(X)
        // 따라서 DTO 반환 필수

        // 4. 결과 확인
        System.out.println("HTTP 상태 코드 확인: " + response.getStatusCode());
        System.out.println("HTTP 헤더 정보 확인 : " + response.getHeaders());
        System.out.println("HTTP 바디 정보 확인: " + response.getBody());

        // 브라우저에 결과도 함께 출력 - 우리가 띄워주는거니까 마음대로
        return ResponseEntity.status(HttpStatus.OK).body(response.getBody());
    }

    // http://localhost:8080/todos-todo/10
    @GetMapping("/todos-todo/{id}")
    public ResponseEntity<?> test2(@PathVariable Integer id) {

        // 1. URI 객체 생성 (주소 만들기)
        URI uri = UriComponentsBuilder
                .fromUriString("https://jsonplaceholder.typicode.com") // 진입점
                .path("/todos")
                .path("/" + id)
                .encode() // 주소에 한글이나 특수문자가 있을 경우 안전하게 변환함
                .build()
                .toUri();

        System.out.println("생성된 URI 주소 : " + uri.toString());

        // 2. RestTemplate 객체 생성 (HTTP 통신 도구)
        RestTemplate restTemplate = new RestTemplate();

        // 3. GET 방식으로 요청 보내기
        ResponseEntity<Todo> response = restTemplate.getForEntity(uri, Todo.class);

        // 4. 결과 확인
        System.out.println("HTTP 상태 코드 확인: " + response.getStatusCode());
        System.out.println("HTTP 헤더 정보 확인 : " + response.getHeaders());
        System.out.println("HTTP 바디 정보 확인: " + response.getBody());
        System.out.println("HTTP 바디 ID 정보 확인: " + response.getBody().getId());
        System.out.println("HTTP 바디 TITLE 정보 확인: " + response.getBody().getTitle());
        System.out.println("HTTP 바디 USERID 정보 확인: " + response.getBody().getUserId());

        // 브라우저에 결과도 함께 출력 - 우리가 띄워주는거니까 마음대로
        return ResponseEntity.status(HttpStatus.OK).body(response.getBody());
    }


    // 우리 서버에서는 GET 요청 매핑 --> 다른 서버에 요청은 POST 요청임 (정보 생성 방법)
    @GetMapping("/posts-test")
    public ResponseEntity<?> test3() {

        // 1. URI 객체 생성 (주소 만들기)
        URI uri = UriComponentsBuilder
                .fromUriString("https://jsonplaceholder.typicode.com") // 진입점
                .path("/posts")
                .encode()
                .build()
                .toUri();

        // 2. 보낼 데이터 만들기
        Post post = Post.builder()
                .title("가입인사드림..")
                .body("나는 ... 입니다. 잘 부탁드립니다")
                .userId(1)
                .build();

        // 3. HTTP 통신 객체 만들기 (POST 요청)
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Post> response = restTemplate.postForEntity(uri, post, Post.class);

        // 브라우저에 결과도 함께 출력 - 우리가 띄워주는거니까 마음대로
        return ResponseEntity.status(HttpStatus.OK).body(response.getBody());
    }

    // exchange 메서드 사용해 보기(헤더 커스텀)
    @GetMapping("/exchange-test")
    public ResponseEntity<?> test4() {

        // 1. URI 객체 생성 (주소 만들기)
        URI uri = UriComponentsBuilder
                .fromUriString("https://jsonplaceholder.typicode.com") // 진입점
                .path("/posts")
                .encode()
                .build()
                .toUri();

        // 2. HTTP 통신 객체 만들기 (POST 요청)
        RestTemplate restTemplate = new RestTemplate();

        // HTTP 메세지 헤더 커스텀
        // 2-1. 헤더 영역 설계
        HttpHeaders headers = new HttpHeaders(); // 커스텀 헤더만들때 해당 new를 통해 필요한거 추가
        // 'Content-type' : 'application/json; charset=UTF-8
        headers.add("Content-type", "application/json; charset=UTF-8");
//        headers.add("Authorization", "sl#$%j23ijpij232j");

        // 2-2. 바디 영역 데이터 생성
        Post post = Post.builder()
                .title("가입인사드림..")
                .body("나는 ... 입니다. 잘 부탁드립니다")
                .userId(1)
                .build();

        // 2-3. 바디 + 헤더 = HttpEntity 만들기
        HttpEntity<Post> requestMessage = new HttpEntity<>(post, headers);

        ResponseEntity<Post> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                requestMessage,
                Post.class
        );

        // 브라우저에 결과도 함께 출력 - 우리가 띄워주는거니까 마음대로
        return ResponseEntity.status(HttpStatus.OK).body(response.getBody().getTitle());
        // response.getBody().getId() 안됨 왜냐면 response에 응답 반환이 String이기 때문
    }

}
