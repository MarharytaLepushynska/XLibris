package com.group.xlibris.security.jwt;

import tools.jackson.databind.ObjectMapper;
import com.group.xlibris.security.dto.AuthResponse;
import com.group.xlibris.security.dto.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtLoginFilter(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            ObjectMapper objectMapper,
            AuthenticationEntryPoint authenticationEntryPoint) {
        super(authenticationManager);
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
        this.authenticationEntryPoint = authenticationEntryPoint;
        setRequiresAuthenticationRequestMatcher(PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, "/api/auth/login"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equalsIgnoreCase("POST")) {
            throw new AuthenticationServiceException("Request method is not supported: " + request.getMethod());
        }
        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            if (loginRequest == null || loginRequest.email() == null || loginRequest.password() == null) {
                throw new BadCredentialsException("Email and password are required");
            }
            var authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                    loginRequest.email().trim(), loginRequest.password());
            setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException ex) {
            throw new AuthenticationServiceException("Authentication request reading error", ex);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        long expiresInSeconds = jwtService.getExpirationMillis() / 1000;
        AuthResponse authResponse = AuthResponse.of(token, expiresInSeconds);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), authResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(request, response, failed);
    }
}
