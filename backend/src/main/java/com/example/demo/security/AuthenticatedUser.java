package com.example.demo.security;

import java.util.Set;

public record AuthenticatedUser(
        String userId,
        String username,
        Set<String> roles
) {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}
