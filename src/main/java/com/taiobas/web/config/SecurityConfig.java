package com.taiobas.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean UserDetailsService usuarios() {
        var encoder=new BCryptPasswordEncoder();
        return new InMemoryUserDetailsManager(
            User.withUsername("admin").password(encoder.encode("admin123")).roles("ADMIN").build(),
            User.withUsername("operador").password(encoder.encode("operador123")).roles("OPERADOR").build(),
            User.withUsername("auditor").password(encoder.encode("auditor123")).roles("AUDITOR").build());
    }
    @Bean org.springframework.security.crypto.password.PasswordEncoder encoder() { return new BCryptPasswordEncoder(); }
    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a
            .requestMatchers("/","/index.html","/css/**","/js/**","/assets/**","/api/sessao","/error").permitAll()
            .requestMatchers(HttpMethod.GET,"/api/**").authenticated()
            .requestMatchers(HttpMethod.POST,"/api/movimentacoes").hasAnyRole("ADMIN","OPERADOR")
            .requestMatchers("/api/**").hasRole("ADMIN").anyRequest().authenticated())
            .formLogin(f -> f.loginProcessingUrl("/login")
                .successHandler((q,r,a)->r.setStatus(200)).failureHandler((q,r,e)->r.sendError(401,"Login inválido")))
            .logout(l -> l.logoutUrl("/logout").logoutSuccessHandler((q,r,a)->r.setStatus(200)))
            .exceptionHandling(e -> e.authenticationEntryPoint((q,r,x)->r.sendError(401)));
        return http.build();
    }
}
