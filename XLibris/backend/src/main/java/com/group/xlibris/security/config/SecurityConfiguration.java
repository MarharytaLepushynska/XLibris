package com.group.xlibris.security.config;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import tools.jackson.databind.ObjectMapper;
import com.group.xlibris.security.error.ProblemDetailAccessDeniedHandler;
import com.group.xlibris.security.error.ProblemDetailAuthenticationEntryPoint;
import com.group.xlibris.security.jwt.JwtAuthenticationFilter;
import com.group.xlibris.security.jwt.JwtLoginFilter;
import com.group.xlibris.security.jwt.JwtService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            ObjectMapper objectMapper,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationEntryPoint authEntryPoint,
            ProblemDetailAuthenticationEntryPoint authenticationEntryPoint,
            ProblemDetailAccessDeniedHandler accessDeniedHandler) throws Exception {

        JwtLoginFilter jwtLoginFilter = new JwtLoginFilter(
                authenticationManager,
                jwtService,
                objectMapper,
                authEntryPoint
        );

        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/books",
                                "/api/v1/books/**",
                                "/api/v1/authors",
                                "/api/v1/authors/**",
                                "/api/v1/genres",
                                "/api/v1/genres/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/books")
                        .hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/books/*")
                        .hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/*")
                        .hasRole("USER")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/authors",
                                "/api/v1/genres")
                        .hasRole("USER")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/authors/*",
                                "/api/v1/genres/*")
                        .hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/authors/*",
                                "/api/v1/genres/*")
                        .hasRole("USER")

                        .requestMatchers("/api/users/*/admin")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users")
                        .hasAnyRole("MODERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/reports",
                                "/api/reports/*")
                        .hasRole("MODERATOR")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/reports/*/review",
                                "/api/reports/*/resolution")
                        .hasRole("MODERATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/reports/*")
                        .hasRole("MODERATOR")

                        .anyRequest().authenticated())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(jwtLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}