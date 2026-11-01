package com.learning.order_management.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

            String header =  request.getHeader(HttpHeaders.AUTHORIZATION);
            if(header!=null && header.contains("Bearer ")&& SecurityContextHolder.getContext().getAuthentication()==null ){
                String token = header.substring(7);
                if(jwtService.validateToken(token)){
                    Claims claims = jwtService.parseToken(token);
                    Long userId =  Long.parseLong(claims.getSubject());
                    List<?> roles = claims.get("roles", List.class);
                    List<SimpleGrantedAuthority> authorities = roles.stream().map(Object::toString).map(role -> new SimpleGrantedAuthority("ROLE_"+role)).toList();
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userId,null,authorities);
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }

            }
            filterChain.doFilter(request,response);
    }

}
