package com.example.issuetracker.identity;

import com.example.issuetracker.identity.IdentityManagementDtos.DictionaryItemRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.DictionaryItemView;
import com.example.issuetracker.identity.IdentityManagementDtos.DictionaryTypeRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.DictionaryTypeView;
import com.example.issuetracker.identity.IdentityManagementDtos.MenuRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.MenuView;
import com.example.issuetracker.identity.IdentityManagementDtos.ModuleRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.ModuleView;
import com.example.issuetracker.identity.IdentityManagementDtos.OrganizationRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.OrganizationView;
import com.example.issuetracker.identity.IdentityManagementDtos.PermissionRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.PermissionView;
import com.example.issuetracker.identity.IdentityManagementDtos.PostRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.PostView;
import com.example.issuetracker.identity.IdentityManagementDtos.RoleAdminView;
import com.example.issuetracker.identity.IdentityManagementDtos.RoleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/identity")
@PreAuthorize("hasAuthority('identity:manage')")
public class IdentityManagementController {

    private final IdentityManagementService service;

    @GetMapping("/organizations")
    public List<OrganizationView> organizations() {
        return service.listOrganizations();
    }

    @PostMapping("/organizations")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationView createOrganization(@Valid @RequestBody OrganizationRequest request) {
        return service.createOrganization(request);
    }

    @PutMapping("/organizations/{id}")
    public OrganizationView updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequest request) {
        return service.updateOrganization(id, request);
    }

    @DeleteMapping("/organizations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganization(@PathVariable Long id) {
        service.deleteOrganization(id);
    }

    @GetMapping("/modules")
    public List<ModuleView> modules() {
        return service.listModules();
    }

    @PostMapping("/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleView createModule(@Valid @RequestBody ModuleRequest request) {
        return service.createModule(request);
    }

    @PutMapping("/modules/{id}")
    public ModuleView updateModule(@PathVariable Long id, @Valid @RequestBody ModuleRequest request) {
        return service.updateModule(id, request);
    }

    @DeleteMapping("/modules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModule(@PathVariable Long id) {
        service.deleteModule(id);
    }

    @GetMapping("/permissions")
    public List<PermissionView> permissions() {
        return service.listPermissions();
    }

    @PostMapping("/permissions")
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionView createPermission(@Valid @RequestBody PermissionRequest request) {
        return service.createPermission(request);
    }

    @PutMapping("/permissions/{id}")
    public PermissionView updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request) {
        return service.updatePermission(id, request);
    }

    @DeleteMapping("/permissions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermission(@PathVariable Long id) {
        service.deletePermission(id);
    }

    @GetMapping("/roles")
    public List<RoleAdminView> roles() {
        return service.listRoles();
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleAdminView createRole(@Valid @RequestBody RoleRequest request) {
        return service.createRole(request);
    }

    @PutMapping("/roles/{id}")
    public RoleAdminView updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return service.updateRole(id, request);
    }

    @DeleteMapping("/roles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable Long id) {
        service.deleteRole(id);
    }

    @GetMapping("/posts")
    public List<PostView> posts() {
        return service.listPosts();
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostView createPost(@Valid @RequestBody PostRequest request) {
        return service.createPost(request);
    }

    @PutMapping("/posts/{id}")
    public PostView updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
        return service.updatePost(id, request);
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long id) {
        service.deletePost(id);
    }

    @GetMapping("/menus")
    public List<MenuView> menus() {
        return service.listMenus();
    }

    @PostMapping("/menus")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuView createMenu(@Valid @RequestBody MenuRequest request) {
        return service.createMenu(request);
    }

    @PutMapping("/menus/{id}")
    public MenuView updateMenu(@PathVariable Long id, @Valid @RequestBody MenuRequest request) {
        return service.updateMenu(id, request);
    }

    @DeleteMapping("/menus/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenu(@PathVariable Long id) {
        service.deleteMenu(id);
    }

    @GetMapping("/dictionary-types")
    public List<DictionaryTypeView> dictionaryTypes() {
        return service.listDictionaryTypes();
    }

    @PostMapping("/dictionary-types")
    @ResponseStatus(HttpStatus.CREATED)
    public DictionaryTypeView createDictionaryType(@Valid @RequestBody DictionaryTypeRequest request) {
        return service.createDictionaryType(request);
    }

    @PutMapping("/dictionary-types/{id}")
    public DictionaryTypeView updateDictionaryType(
            @PathVariable Long id,
            @Valid @RequestBody DictionaryTypeRequest request) {
        return service.updateDictionaryType(id, request);
    }

    @DeleteMapping("/dictionary-types/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDictionaryType(@PathVariable Long id) {
        service.deleteDictionaryType(id);
    }

    @GetMapping("/dictionary-items")
    public List<DictionaryItemView> dictionaryItems(@RequestParam Long typeId) {
        return service.listDictionaryItems(typeId);
    }

    @PostMapping("/dictionary-items")
    @ResponseStatus(HttpStatus.CREATED)
    public DictionaryItemView createDictionaryItem(@Valid @RequestBody DictionaryItemRequest request) {
        return service.createDictionaryItem(request);
    }

    @PutMapping("/dictionary-items/{id}")
    public DictionaryItemView updateDictionaryItem(
            @PathVariable Long id,
            @Valid @RequestBody DictionaryItemRequest request) {
        return service.updateDictionaryItem(id, request);
    }

    @DeleteMapping("/dictionary-items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDictionaryItem(@PathVariable Long id) {
        service.deleteDictionaryItem(id);
    }
}
