package kr.or.hieating.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
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
        // Form 로그인을 활용하는경우 (JWT에는 필요없음)
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/")
                    .permitAll())
        .logout(
            logout ->
                logout.logoutUrl("/logout").logoutSuccessUrl("/").invalidateHttpSession(true));
    return http.build();
  }
}
