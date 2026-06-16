package com.example.issuetracker.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public final class IdentityManagementDtos {

    private IdentityManagementDtos() {
    }

    public record OrganizationView(
            Long id,
            Long parentId,
            String parentName,
            String code,
            String name,
            String type,
            int sortOrder,
            String leader,
            String phone,
            String email,
            String description,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record OrganizationRequest(
            Long parentId,
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{2,50}$") String code,
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 30) String type,
            int sortOrder,
            @Size(max = 100) String leader,
            @Size(max = 50) String phone,
            @Size(max = 255) String email,
            String description,
            boolean enabled
    ) {
    }

    public record ModuleView(
            Long id,
            String code,
            String name,
            String routePrefix,
            String description,
            boolean enabled,
            int sortOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record ModuleRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{2,50}$") String code,
            @NotBlank @Size(max = 100) String name,
            @Size(max = 120) String routePrefix,
            String description,
            boolean enabled,
            int sortOrder
    ) {
    }

    public record PermissionView(
            Long id,
            String code,
            String name,
            Long moduleId,
            String moduleCode,
            String moduleName,
            String description,
            boolean enabled,
            int sortOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record PermissionRequest(
            @NotBlank @Size(max = 100) String code,
            @NotBlank @Size(max = 100) String name,
            Long moduleId,
            String description,
            boolean enabled,
            int sortOrder
    ) {
    }

    public record RoleAdminView(
            Long id,
            String code,
            String name,
            String description,
            boolean enabled,
            int sortOrder,
            List<Long> permissionIds,
            List<String> permissions,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record RoleRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{2,50}$") String code,
            @NotBlank @Size(max = 100) String name,
            String description,
            boolean enabled,
            int sortOrder,
            @NotEmpty Set<@NotNull Long> permissionIds
    ) {
    }

    public record PostView(
            Long id,
            String code,
            String name,
            int sortOrder,
            String description,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record PostRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{2,50}$") String code,
            @NotBlank @Size(max = 100) String name,
            int sortOrder,
            String description,
            boolean enabled
    ) {
    }

    public record MenuView(
            Long id,
            Long parentId,
            String parentName,
            Long moduleId,
            String moduleName,
            String name,
            String path,
            String component,
            String icon,
            String permissionCode,
            int sortOrder,
            boolean visible,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record NavigationMenuView(
            Long id,
            Long parentId,
            String name,
            String path,
            String icon,
            String permissionCode,
            int sortOrder,
            List<NavigationMenuView> children
    ) {
    }

    public record MenuRequest(
            Long parentId,
            Long moduleId,
            @NotBlank @Size(max = 100) String name,
            @Size(max = 200) String path,
            @Size(max = 200) String component,
            @Size(max = 100) String icon,
            @Size(max = 100) String permissionCode,
            int sortOrder,
            boolean visible,
            boolean enabled
    ) {
    }

    public record DictionaryTypeView(
            Long id,
            String code,
            String name,
            String description,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record DictionaryTypeRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{2,50}$") String code,
            @NotBlank @Size(max = 100) String name,
            String description,
            boolean enabled
    ) {
    }

    public record DictionaryItemView(
            Long id,
            Long typeId,
            String typeCode,
            String label,
            String value,
            int sortOrder,
            String remark,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record DictionaryItemRequest(
            @NotNull Long typeId,
            @NotBlank @Size(max = 100) String label,
            @NotBlank @Size(max = 100) String value,
            int sortOrder,
            String remark,
            boolean enabled
    ) {
    }
}
