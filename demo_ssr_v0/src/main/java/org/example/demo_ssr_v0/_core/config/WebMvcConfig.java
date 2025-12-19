package org.example.demo_ssr_v0._core.config;


import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v0._core.interceptor.LoginInterceptor;
import org.example.demo_ssr_v0._core.interceptor.SessionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정 클래스
 * @C, @S, @R, @Component, @Configuration
 */
//@Component 클래스 내부에서 @Bean 어노테이션을 사용해야 된다면 @Configuration 사용
@Configuration // 내부에 IoC 대상 여부 확인해주는 어노테이션
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final SessionInterceptor sessionInterceptor;

    // ps. 인터셉터는 당연히 여러개 등록 가능 함
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                        .addPathPatterns("/**");

        // 1. 설정에 LoginInterceptor 를 등록하는 코드
        // 2. 인터셉터가 동작할 URL 패턴 지정 (엔드포인트만 작성하면됨)
        // 3. 어떤 URL 요청이 로그인 여부를 필요할 지 확인 해야함
        //    /board/** <-- 일단 이 엔드포인트 다 검사 시킬거야
        //    /user/** <-- 일단 이 엔드포인트 다 검사 시킬거야
        //      --> 단, 특정 URL은 제외시킬거야
        registry.addInterceptor(loginInterceptor)
                // /** <-- 모든 URL 제외 대상이 됨, 일단 사용 안함
                .addPathPatterns("/board/**", "/user/**", "/reply/**")
                .excludePathPatterns(
                        "/login",
                        "/join",
                        "/logout",
                        "/board/list",
                        "/",
                        "/board/{id:\\d+}",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.io",
                        "/h2-console/**"
                );
        // \\d+ 는 정규 표현식으로 1개 이상의 숫자를 의미함
        // /board/1, board/1234 <-- 허용
        // /board/abc 같은 경우 매칭되지 않음\
        }

    /**
     * 정적 리소스 핸들러
     * 업로드된 이미지 파일을 웹에서 접근할 수 있도록 설정합니다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        /**
         *  /images/** 경로로 요청이 들어오면 나의 폴더 images/ 디렉토리에서 찾게 설정합니다
         */
        // 머스태치 이미지 태그에 src 경로에 /images/** 같은 경로로 설정 되어 있다면
        // 스프링이 알아서 내 폴더 file:(프로젝트 루트 디렉토리)안에 images/ 폴더를 찾게 한다.
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///C:/uploads/");
        //** file:/// 문법설명
        // file: 파일 시스템을 가리킴
        // 파일 시스템에서 절대 경로를 의미하는 URI 표기법은 -> ///:
        // file:images/ 앞에 슬러시가 없기 때문에 상대경로를 의미함
        // file:///D:upload/ <-- 내 컴퓨터 절대 경로를 의미함
    }
}
