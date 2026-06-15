package com.example.issuetracker.security;

import com.example.issuetracker.domain.Permission;
import com.example.issuetracker.domain.Role;
import com.example.issuetracker.domain.User;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PermissionClaims {

    private static final Set<String> TRAVEL_ADMIN_PERMISSIONS = Set.of(
            "travel:ticket:read",
            "travel:ticket:create",
            "travel:ticket:update",
            "travel:ticket:delete",
            "travel:ticket:approve",
            "travel:risk:read",
            "travel:search:reindex",
            "travel:ops:read"
    );

    private PermissionClaims() {
    }

    public static List<String> permissionsFor(User user) {
        Set<String> permissions = new LinkedHashSet<>();
        Set<String> roles = new LinkedHashSet<>();
        for (Role role : user.getRoles()) {
            roles.add(role.getCode());
            role.getPermissions().stream()
                    .map(Permission::getCode)
                    .forEach(permissions::add);
        }
        if (roles.contains("ADMIN") || roles.contains("MANAGER") || roles.contains("TRAVEL_ADMIN")) {
            permissions.addAll(TRAVEL_ADMIN_PERMISSIONS);
        }
        return permissions.stream().sorted().toList();
    }
}
