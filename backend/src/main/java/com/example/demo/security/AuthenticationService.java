package com.example.demo.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String userId = jwt.getSubject();
            String username = jwt.getClaimAsString("preferred_username");
            Set<String> roles = extractRoles(jwtAuth);
            return new AuthenticatedUser(userId, username, roles);
        }

        return new AuthenticatedUser("anonymous", "anonymous", Set.of("ROLE_USER", "ROLE_ADMIN"));
    }

    private Set<String> extractRoles(JwtAuthenticationToken jwtAuth) {
        Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
