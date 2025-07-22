package org.annill.security.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.annill.security.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ui/deal/search")
                .hasAnyRole("CREDIT_USER", "OVERDRAFT_USER", "DEAL_SUPERUSER", "SUPERUSER")
                .requestMatchers("/ui/deal/{id}")
                .hasAnyRole("USER", "CREDIT_USER", "OVERDRAFT_USER", "DEAL_SUPERUSER", "SUPERUSER")
                .requestMatchers("/ui/deal/save", "/ui/deal/change/status", "/ui/deal/search/export")
                .hasAnyRole("DEAL_SUPERUSER", "SUPERUSER")
                .requestMatchers("/ui/contractor/search")
                .hasAnyRole("USER", "CONTRACTOR_RUS", "CONTRACTOR_SUPERUSER", "SUPERUSER")
                .requestMatchers("/ui/contractor/**").hasAnyRole("CONTRACTOR_SUPERUSER", "SUPERUSER")
                .requestMatchers("/ui/contractor-to-role/**").hasAnyRole("CONTRACTOR_SUPERUSER", "SUPERUSER")
                .requestMatchers("/ui/deal-contractor/**").hasAnyRole("CONTRACTOR_SUPERUSER", "SUPERUSER")
                .requestMatchers("/ui/auth/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Не авторизован"))
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
        throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}

