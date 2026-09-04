package com.student.iksu.config;

import com.student.iksu.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 1. 정적 리소스 및 프론트엔드 화면 허용
                        .requestMatchers(
                                "/", "/index.html", "/assets/**", "/*.png", "/*.svg", "/favicon.ico", "/error",
                                "/notice/**", "/info/**", "/gallery/**", "/material/**", "/about",
                                "/election", "/election/**", // 선거 프론트 페이지 경로 추가
                                "/login", "/join", "/find-id", "/reset-password", "/mypage", "/admin"
                        ).permitAll()
                        .requestMatchers("/h2-console/**", "/css/**", "/js/**", "/images/**").permitAll()

                        // 2. 로그인, 회원가입 API 허용
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/check-username", "/api/check-email", "/api/email/**").permitAll()

                        // ★ 2. 학생회 투표 API 전체 허용 (비로그인 학생들도 메일 인증 후 투표해야 하므로 permitAll)
                        .requestMatchers("/api/election/**", "/election/**").permitAll()

                        // 3. 조회(GET) 전용 허용 목록
                        .requestMatchers(HttpMethod.GET,
                                "/api/notices/**",
                                "/api/infos/**",
                                "/api/gallery/**",
                                "/api/executives/**"
                        ).permitAll()

                        // 4. 관리자 전용 API (어드민 페이지 권한)
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN", "STAFF")

                        // 5. 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://skaisrael.com",
                "https://www.skaisrael.com",
                "https://skai-frontend-olive.vercel.app"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}