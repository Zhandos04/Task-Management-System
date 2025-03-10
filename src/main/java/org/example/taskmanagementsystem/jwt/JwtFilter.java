package org.example.taskmanagementsystem.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.taskmanagementsystem.service.TokenBlacklistService;
import org.example.taskmanagementsystem.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final List<String> PUBLIC_URLS = List.of(
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/auth/**"
    );
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        boolean isPublicUrl = PUBLIC_URLS.stream()
                .anyMatch(publicUrl -> publicUrl.equals(requestUri) || matchWithWildcard(publicUrl, requestUri));

        if (isPublicUrl) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (tokenBlacklistService.isTokenBlacklisted(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token is invalid (logged out)");
                    return;
                }
                String username = jwtService.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userService.loadUserByUsername(username);
                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Authentication failed: " + e.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
    private boolean matchWithWildcard(String pattern, String requestUri) {
        if (pattern.endsWith("/**")) {
            String basePattern = pattern.substring(0, pattern.length() - 3);
            return requestUri.startsWith(basePattern);
        }
        if (pattern.endsWith("/*")) {
            String basePattern = pattern.substring(0, pattern.length() - 2);
            return requestUri.startsWith(basePattern) && !requestUri.substring(basePattern.length()).contains("/");
        }
        return false;
    }
}