package kr.or.hieating.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CSRF 활성화 (CookieCsrfTokenRepository + X-XSRF-TOKEN 헤더 방식)
        // - Thymeleaf 폼은 th:action에 의해 _csrf 필드가 자동 생성됨
        // - JS API 호출 시 XSRF-TOKEN 쿠키 값을 X-XSRF-TOKEN 헤더로 전송
        .csrf(
            csrf -> {
              CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
              handler.setCsrfRequestAttributeName(null);
              csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                  .csrfTokenRequestHandler(handler);
            })
        .httpBasic(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/",
                        "/home",
                        "/login",
                        "/signup",
                        "/signup/**",
                        "/product/**",
                        "/categories/**",
                        "/hot-deals",
                        "/api/hot-deals/products",
                        "/table-decorations",
                        "/api/table-decorations/posts",
                        "/search",
                        "/mcp",
                        "/mcp/**",
                        "/sse",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/fonts/**",
                        "/lotties/**") // 마스코트 Lottie 애니메이션 JSON
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/")
                    .permitAll())
        .logout(
            logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .permitAll());
    return http.build();
  }
}
