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
                        "/css/**",
                        "/js/**",
                        "/images/**") // 추후 코드 병합시 핫딜과 같은 페이지 허용 필요
                    .permitAll()
                    .requestMatchers("/admin", "/admin/**")
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
