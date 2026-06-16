package com.example.issuetracker.security;

import com.example.issuetracker.domain.Permission;
import com.example.issuetracker.domain.Role;
import com.example.issuetracker.domain.User;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PermissionClaims {

    private PermissionClaims() {
    }

    public static List<String> permissionsFor(User user) {
        Set<String> permissions = new LinkedHashSet<>();
        for (Role role : user.getRoles()) {
            role.getPermissions().stream()
                    .map(Permission::getCode)
                    .forEach(permissions::add);
        }
        return permissions.stream().sorted().toList();
    }
}
