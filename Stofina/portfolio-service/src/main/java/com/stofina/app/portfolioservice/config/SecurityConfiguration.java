package com.stofina.app.portfolioservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stofina.app.commondata.security.jwt.JwtService;
import org.springframework.http.HttpMethod;
import com.stofina.app.commondata.security.handler.RestAccessDeniedHandler;
import com.stofina.app.commondata.security.handler.RestAuthenticationEntryPoint;
import com.stofina.app.portfolioservice.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final ObjectMapper objectMapper;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtService jwtService() {
        return new JwtService(jwtSecret);
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtService());
    }

    @Bean
    public RestAuthenticationEntryPoint authenticationEntryPoint() {
        return new RestAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public RestAccessDeniedHandler accessDeniedHandler() {
        return new RestAccessDeniedHandler(objectMapper);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})          // CORS bean'in varsa çalışır, yoksa aşağıdaki notu gör
                .httpBasic(conf -> conf.disable())
                .formLogin(conf -> conf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/actuator/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/stocks/**").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/transactions/**", "/api/v1/accounts/**", "/api/v1/balances/**")
                        .hasAnyRole("CUSTOMER_SUPER_ADMIN", "CUSTOMER_TRADER")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/transactions/**", "/api/v1/accounts/**", "/api/v1/balances/**")
                        .hasAnyRole("CUSTOMER_SUPER_ADMIN", "CUSTOMER_TRADER")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/transactions/**", "/api/v1/accounts/**", "/api/v1/balances/**")
                        .hasAnyRole("CUSTOMER_SUPER_ADMIN", "CUSTOMER_TRADER")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/transactions/**", "/api/v1/accounts/**", "/api/v1/balances/**")
                        .hasAnyRole("CUSTOMER_SUPER_ADMIN", "CUSTOMER_TRADER")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/transactions/**", "/api/v1/accounts/**", "/api/v1/balances/**")
                        .hasAnyRole("CUSTOMER_SUPER_ADMIN", "CUSTOMER_TRADER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
