package com.jys.smartbudget.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.jys.smartbudget.exception.JwtAuthenticationEntryPoint;
import com.jys.smartbudget.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

@Configuration  // Spring이 이 클래스를 설정 파일로 인식
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisTokenService redisTokenService;
    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    /**
     * BCryptPasswordEncoder: 비밀번호를 암호화하는 도구
     * 회원가입 시 비밀번호를 그대로 저장하면 위험하니까 암호화해서 저장
     * 로그인 시 입력된 비밀번호를 암호화해서 DB의 암호화된 비밀번호와 비교
     * 
     * @Bean: 이 메서드가 반환하는 객체를 Spring이 관리하는 Bean으로 등록
     * 다른 곳에서 @Autowired나 생성자 주입으로 사용 가능
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JwtAuthFilter를 Bean으로 등록
     * SecurityFilterChain에서 이 필터를 사용하기 위해 Bean으로 만듦
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(redisTokenService, jwtUtil);
    }

    /**
     * SecurityFilterChain: Spring Security의 핵심 설정
     * 어떤 URL을 어떻게 보호할지, 어떤 필터를 사용할지 등을 정의
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // CORS 설정: 다른 도메인에서의 요청 허용 설정
            // React(localhost:3000)에서 Spring(localhost:8080)으로 요청 보낼 때 필요
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF 비활성화
            // CSRF: 사이트 간 요청 위조 공격 방지 기능
            // REST API는 주로 stateless하고 JWT로 인증하므로 CSRF 보호 불필요
            .csrf(csrf -> csrf.disable())

            .exceptionHandling(exception -> exception

            // 인증 실패(401) 시 실행할 에러 핸들러를 지정
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        )
            // URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // OPTIONS 요청은 모두 허용
                // CORS preflight 요청 처리를 위해 필요
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 로그아웃은 반드시 인증(토큰)이 있어야 한다고 명시 
                .requestMatchers("/auth/logout").authenticated()
                
                // /auth/** 경로는 인증 없이 접근 가능
                // 로그인, 회원가입 등은 토큰 없어도 가능해야 하니까
                .requestMatchers("/auth/**").permitAll()
                
                // /budgets/**, /expenses/** 경로는 인증 필요
                // authenticated(): 인증된 사용자만 접근 가능
                .requestMatchers("/budgets/**", "/expenses/**").authenticated()
                
                // 나머지 모든 요청은 허용
                .anyRequest().permitAll()
            )
            
            // JWT 필터를 Spring Security 필터 체인에 추가
            // UsernamePasswordAuthenticationFilter 앞에 배치
            // 즉, Spring Security가 기본 인증 처리하기 전에 JWT 먼저 검증
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정: Cross-Origin Resource Sharing
     * 웹 브라우저의 보안 정책상 다른 출처(도메인, 포트)의 리소스 요청은 제한됨
     * React(3000포트)와 Spring(8080포트)이 다른 출처이므로 CORS 설정 필요
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 허용할 출처(Origin) 지정
        // React 개발 서버 주소
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        
        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 허용할 헤더 (모든 헤더 허용)
        // Authorization 헤더가 포함되어야 하므로
        config.setAllowedHeaders(List.of("*"));
        
        // 클라이언트에게 노출할 헤더
        // 서버가 Authorization 헤더를 응답에 포함할 때 브라우저가 읽을 수 있도록
        config.setExposedHeaders(List.of("Authorization"));
        
        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        config.setAllowCredentials(true);

        // 모든 경로에 대해 위의 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}