package org.example.expert.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String bearerJwt = request.getHeader("Authorization");

        // 비로그인 요청이 허용되는 경로도 있으므로, 헤더가 없으면 그대로 다음 필터로 넘긴다.
        if (bearerJwt == null || bearerJwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = jwtUtil.substringToken(bearerJwt);
            Claims claims = jwtUtil.extractClaims(jwt);

            // JWT payload를 애플리케이션에서 쓰는 AuthUser 객체로 변환한다.
            AuthUser authUser = new AuthUser(
                    Long.parseLong(claims.getSubject()),
                    claims.get("email", String.class),
                    UserRole.of(claims.get("userRole", String.class)),
                    claims.get("nickname", String.class)
            );

            // Spring Security 권한 체계에 맞게 ROLE_ 접두사를 붙여 authority를 만든다.
            Collection<? extends GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + authUser.getUserRole().name())
            );

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(authUser, null, authorities);

            // 이후 컨트롤러에서 @AuthenticationPrincipal로 authUser를 받을 수 있게 SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 토큰 파싱/검증에 실패하면 인증 정보를 비우고 401을 반환한다.
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다.");
        }
    }
}
