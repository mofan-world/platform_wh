package com.example.issuetracker.identity;

import com.example.issuetracker.common.BusinessException;
import com.example.issuetracker.domain.DictionaryItem;
import com.example.issuetracker.domain.DictionaryType;
import com.example.issuetracker.domain.Menu;
import com.example.issuetracker.domain.Organization;
import com.example.issuetracker.domain.Permission;
import com.example.issuetracker.domain.Post;
import com.example.issuetracker.domain.Role;
import com.example.issuetracker.domain.ServiceModule;
import com.example.issuetracker.domain.User;
import com.example.issuetracker.identity.IdentityManagementDtos.DictionaryItemRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.DictionaryItemView;
import com.example.issuetracker.identity.IdentityManagementDtos.DictionaryTypeRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.DictionaryTypeView;
import com.example.issuetracker.identity.IdentityManagementDtos.MenuRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.MenuView;
import com.example.issuetracker.identity.IdentityManagementDtos.ModuleRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.ModuleView;
import com.example.issuetracker.identity.IdentityManagementDtos.NavigationMenuView;
import com.example.issuetracker.identity.IdentityManagementDtos.OrganizationRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.OrganizationView;
import com.example.issuetracker.identity.IdentityManagementDtos.PermissionRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.PermissionView;
import com.example.issuetracker.identity.IdentityManagementDtos.PostRequest;
import com.example.issuetracker.identity.IdentityManagementDtos.PostView;
import com.example.issuetracker.identity.IdentityManagementDtos.RoleAdminView;
import com.example.issuetracker.identity.IdentityManagementDtos.RoleRequest;
import com.example.issuetracker.repository.DictionaryItemRepository;
import com.example.issuetracker.repository.DictionaryTypeRepository;
import com.example.issuetracker.repository.MenuRepository;
import com.example.issuetracker.repository.OrganizationRepository;
import com.example.issuetracker.repository.PermissionRepository;
import com.example.issuetracker.repository.PostRepository;
import com.example.issuetracker.repository.RoleRepository;
import com.example.issuetracker.repository.ServiceModuleRepository;
import com.example.issuetracker.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IdentityManagementService {

    private final OrganizationRepository organizationRepository;
    private final ServiceModuleRepository moduleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PostRepository postRepository;
    private final MenuRepository menuRepository;
    private final DictionaryTypeRepository dictionaryTypeRepository;
    private final DictionaryItemRepository dictionaryItemRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<OrganizationView> listOrganizations() {
        return organizationRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Organization::getSortOrder).thenComparing(Organization::getId))
                .map(this::toOrganizationView)
                .toList();
    }

    @Transactional
    public OrganizationView createOrganization(OrganizationRequest request) {
        requireUniqueOrganizationCode(request.code(), null);
        Organization organization = new Organization();
        applyOrganization(organization, request);
        return toOrganizationView(organizationRepository.save(organization));
    }

    @Transactional
    public OrganizationView updateOrganization(Long id, OrganizationRequest request) {
        Organization organization = requireOrganization(id);
        requireUniqueOrganizationCode(request.code(), id);
        if (request.parentId() != null && request.parentId().equals(id)) {
            throw BusinessException.badRequest("INVALID_PARENT", "上级组织不能选择自身");
        }
        applyOrganization(organization, request);
        return toOrganizationView(organizationRepository.save(organization));
    }

    @Transactional
    public void deleteOrganization(Long id) {
        organizationRepository.delete(requireOrganization(id));
    }

    @Transactional(readOnly = true)
    public List<ModuleView> listModules() {
        return moduleRepository.findAll(Sort.by("sortOrder").and(Sort.by("id"))).stream()
                .map(this::toModuleView)
                .toList();
    }

    @Transactional
    public ModuleView createModule(ModuleRequest request) {
        requireUniqueModuleCode(request.code(), null);
        ServiceModule module = new ServiceModule();
        applyModule(module, request);
        return toModuleView(moduleRepository.save(module));
    }

    @Transactional
    public ModuleView updateModule(Long id, ModuleRequest request) {
        ServiceModule module = requireModule(id);
        requireUniqueModuleCode(request.code(), id);
        applyModule(module, request);
        return toModuleView(moduleRepository.save(module));
    }

    @Transactional
    public void deleteModule(Long id) {
        moduleRepository.delete(requireModule(id));
    }

    @Transactional(readOnly = true)
    public List<PermissionView> listPermissions() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Permission::getSortOrder).thenComparing(Permission::getId))
                .map(this::toPermissionView)
                .toList();
    }

    @Transactional
    public PermissionView createPermission(PermissionRequest request) {
        requireUniquePermissionCode(request.code(), null);
        Permission permission = new Permission();
        applyPermission(permission, request);
        return toPermissionView(permissionRepository.save(permission));
    }

    @Transactional
    public PermissionView updatePermission(Long id, PermissionRequest request) {
        Permission permission = requirePermission(id);
        requireUniquePermissionCode(request.code(), id);
        applyPermission(permission, request);
        return toPermissionView(permissionRepository.save(permission));
    }

    @Transactional
    public void deletePermission(Long id) {
        permissionRepository.delete(requirePermission(id));
    }

    @Transactional(readOnly = true)
    public List<RoleAdminView> listRoles() {
        return roleRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Role::getSortOrder).thenComparing(Role::getId))
                .map(this::toRoleAdminView)
                .toList();
    }

    @Transactional
    public RoleAdminView createRole(RoleRequest request) {
        requireUniqueRoleCode(request.code(), null);
        Role role = new Role();
        applyRole(role, request);
        return toRoleAdminView(roleRepository.save(role));
    }

    @Transactional
    public RoleAdminView updateRole(Long id, RoleRequest request) {
        Role role = requireRole(id);
        requireUniqueRoleCode(request.code(), id);
        applyRole(role, request);
        return toRoleAdminView(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = requireRole(id);
        if ("ADMIN".equals(role.getCode())) {
            throw BusinessException.badRequest("ADMIN_ROLE_LOCKED", "不能删除内置管理员角色");
        }
        roleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public List<PostView> listPosts() {
        return postRepository.findAll(Sort.by("sortOrder").and(Sort.by("id"))).stream()
                .map(this::toPostView)
                .toList();
    }

    @Transactional
    public PostView createPost(PostRequest request) {
        requireUniquePostCode(request.code(), null);
        Post post = new Post();
        applyPost(post, request);
        return toPostView(postRepository.save(post));
    }

    @Transactional
    public PostView updatePost(Long id, PostRequest request) {
        Post post = requirePost(id);
        requireUniquePostCode(request.code(), id);
        applyPost(post, request);
        return toPostView(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.delete(requirePost(id));
    }

    @Transactional(readOnly = true)
    public List<MenuView> listMenus() {
        return menuRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Menu::getSortOrder).thenComparing(Menu::getId))
                .map(this::toMenuView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NavigationMenuView> listNavigationMenus(String moduleCode) {
        User user = currentUser.require();
        Set<String> permissions = currentUser.permissions(user);
        boolean admin = user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getCode()));
        String normalizedModuleCode = moduleCode == null ? null : moduleCode.trim().toUpperCase(Locale.ROOT);

        List<Menu> visibleMenus = menuRepository.findAll().stream()
                .filter(Menu::isEnabled)
                .filter(Menu::isVisible)
                .filter(menu -> belongsToModule(menu, normalizedModuleCode))
                .filter(menu -> canAccessMenu(menu, permissions, admin))
                .sorted(Comparator.comparingInt(Menu::getSortOrder).thenComparing(Menu::getId))
                .toList();

        Map<Long, NavigationMenuNode> nodes = new LinkedHashMap<>();
        for (Menu menu : visibleMenus) {
            nodes.put(menu.getId(), new NavigationMenuNode(menu));
        }

        List<NavigationMenuNode> roots = new ArrayList<>();
        for (NavigationMenuNode node : nodes.values()) {
            Long parentId = node.menu.getParent() == null ? null : node.menu.getParent().getId();
            NavigationMenuNode parent = parentId == null ? null : nodes.get(parentId);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.children.add(node);
            }
        }

        return roots.stream().map(this::toNavigationMenuView).toList();
    }

    @Transactional
    public MenuView createMenu(MenuRequest request) {
        Menu menu = new Menu();
        applyMenu(menu, request);
        return toMenuView(menuRepository.save(menu));
    }

    @Transactional
    public MenuView updateMenu(Long id, MenuRequest request) {
        Menu menu = requireMenu(id);
        if (request.parentId() != null && request.parentId().equals(id)) {
            throw BusinessException.badRequest("INVALID_PARENT", "上级菜单不能选择自身");
        }
        applyMenu(menu, request);
        return toMenuView(menuRepository.save(menu));
    }

    @Transactional
    public void deleteMenu(Long id) {
        menuRepository.delete(requireMenu(id));
    }

    @Transactional(readOnly = true)
    public List<DictionaryTypeView> listDictionaryTypes() {
        return dictionaryTypeRepository.findAll(Sort.by("code")).stream()
                .map(this::toDictionaryTypeView)
                .toList();
    }

    @Transactional
    public DictionaryTypeView createDictionaryType(DictionaryTypeRequest request) {
        requireUniqueDictionaryTypeCode(request.code(), null);
        DictionaryType type = new DictionaryType();
        applyDictionaryType(type, request);
        return toDictionaryTypeView(dictionaryTypeRepository.save(type));
    }

    @Transactional
    public DictionaryTypeView updateDictionaryType(Long id, DictionaryTypeRequest request) {
        DictionaryType type = requireDictionaryType(id);
        requireUniqueDictionaryTypeCode(request.code(), id);
        applyDictionaryType(type, request);
        return toDictionaryTypeView(dictionaryTypeRepository.save(type));
    }

    @Transactional
    public void deleteDictionaryType(Long id) {
        dictionaryTypeRepository.delete(requireDictionaryType(id));
    }

    @Transactional(readOnly = true)
    public List<DictionaryItemView> listDictionaryItems(Long typeId) {
        requireDictionaryType(typeId);
        return dictionaryItemRepository.findByTypeId(typeId).stream()
                .sorted(Comparator.comparingInt(DictionaryItem::getSortOrder).thenComparing(DictionaryItem::getId))
                .map(this::toDictionaryItemView)
                .toList();
    }

    @Transactional
    public DictionaryItemView createDictionaryItem(DictionaryItemRequest request) {
        DictionaryItem item = new DictionaryItem();
        applyDictionaryItem(item, request);
        return toDictionaryItemView(dictionaryItemRepository.save(item));
    }

    @Transactional
    public DictionaryItemView updateDictionaryItem(Long id, DictionaryItemRequest request) {
        DictionaryItem item = requireDictionaryItem(id);
        applyDictionaryItem(item, request);
        return toDictionaryItemView(dictionaryItemRepository.save(item));
    }

    @Transactional
    public void deleteDictionaryItem(Long id) {
        dictionaryItemRepository.delete(requireDictionaryItem(id));
    }

    private void applyOrganization(Organization organization, OrganizationRequest request) {
        organization.setParent(request.parentId() == null ? null : requireOrganization(request.parentId()));
        organization.setCode(normalizeCode(request.code()));
        organization.setName(request.name().trim());
        organization.setType(request.type().trim().toUpperCase(Locale.ROOT));
        organization.setSortOrder(request.sortOrder());
        organization.setLeader(trimToNull(request.leader()));
        organization.setPhone(trimToNull(request.phone()));
        organization.setEmail(trimToNull(request.email()));
        organization.setDescription(trimToNull(request.description()));
        organization.setEnabled(request.enabled());
    }

    private void applyModule(ServiceModule module, ModuleRequest request) {
        module.setCode(normalizeCode(request.code()));
        module.setName(request.name().trim());
        module.setRoutePrefix(trimToNull(request.routePrefix()));
        module.setDescription(trimToNull(request.description()));
        module.setEnabled(request.enabled());
        module.setSortOrder(request.sortOrder());
    }

    private void applyPermission(Permission permission, PermissionRequest request) {
        permission.setCode(request.code().trim());
        permission.setName(request.name().trim());
        permission.setModule(request.moduleId() == null ? null : requireModule(request.moduleId()));
        permission.setDescription(trimToNull(request.description()));
        permission.setEnabled(request.enabled());
        permission.setSortOrder(request.sortOrder());
    }

    private void applyRole(Role role, RoleRequest request) {
        role.setCode(normalizeCode(request.code()));
        role.setName(request.name().trim());
        role.setDescription(trimToNull(request.description()));
        role.setEnabled(request.enabled());
        role.setSortOrder(request.sortOrder());
        role.setPermissions(requirePermissions(request.permissionIds()));
    }

    private void applyPost(Post post, PostRequest request) {
        post.setCode(normalizeCode(request.code()));
        post.setName(request.name().trim());
        post.setSortOrder(request.sortOrder());
        post.setDescription(trimToNull(request.description()));
        post.setEnabled(request.enabled());
    }

    private void applyMenu(Menu menu, MenuRequest request) {
        Menu parent = request.parentId() == null ? null : requireMenu(request.parentId());
        validateMenuParent(menu, parent);
        menu.setParent(parent);
        menu.setModule(request.moduleId() == null ? null : requireModule(request.moduleId()));
        menu.setName(request.name().trim());
        menu.setPath(trimToNull(request.path()));
        menu.setComponent(trimToNull(request.component()));
        menu.setIcon(trimToNull(request.icon()));
        menu.setPermissionCode(trimToNull(request.permissionCode()));
        menu.setSortOrder(request.sortOrder());
        menu.setVisible(request.visible());
        menu.setEnabled(request.enabled());
    }

    private void validateMenuParent(Menu menu, Menu parent) {
        if (parent == null) {
            return;
        }
        if (menu.getId() != null && parent.getId().equals(menu.getId())) {
            throw BusinessException.badRequest("INVALID_PARENT", "上级菜单不能选择自身");
        }
        if (menu.getId() != null && isDescendantOf(parent, menu)) {
            throw BusinessException.badRequest("INVALID_PARENT", "上级菜单不能选择当前菜单的下级菜单");
        }
        if (menuDepth(parent) >= 3) {
            throw BusinessException.badRequest("MENU_DEPTH_LIMIT", "菜单最多只能有三级");
        }
    }

    private boolean isDescendantOf(Menu candidate, Menu ancestor) {
        Menu current = candidate;
        while (current != null) {
            if (current.getParent() != null && current.getParent().getId().equals(ancestor.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private int menuDepth(Menu menu) {
        int depth = 1;
        Menu current = menu;
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    private void applyDictionaryType(DictionaryType type, DictionaryTypeRequest request) {
        type.setCode(normalizeCode(request.code()));
        type.setName(request.name().trim());
        type.setDescription(trimToNull(request.description()));
        type.setEnabled(request.enabled());
    }

    private void applyDictionaryItem(DictionaryItem item, DictionaryItemRequest request) {
        item.setType(requireDictionaryType(request.typeId()));
        item.setLabel(request.label().trim());
        item.setValue(request.value().trim());
        item.setSortOrder(request.sortOrder());
        item.setRemark(trimToNull(request.remark()));
        item.setEnabled(request.enabled());
    }

    private Set<Permission> requirePermissions(Set<Long> permissionIds) {
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw BusinessException.badRequest("INVALID_PERMISSION", "包含不存在的权限");
        }
        return Set.copyOf(permissions);
    }

    private Organization requireOrganization(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("组织机构不存在"));
    }

    private ServiceModule requireModule(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("微服务模块不存在"));
    }

    private Permission requirePermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("权限不存在"));
    }

    private Role requireRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("角色不存在"));
    }

    private Post requirePost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("岗位不存在"));
    }

    private Menu requireMenu(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("菜单不存在"));
    }

    private DictionaryType requireDictionaryType(Long id) {
        return dictionaryTypeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("字典类型不存在"));
    }

    private DictionaryItem requireDictionaryItem(Long id) {
        return dictionaryItemRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("字典项不存在"));
    }

    private void requireUniqueOrganizationCode(String code, Long id) {
        if (id == null ? organizationRepository.existsByCodeIgnoreCase(code)
                : organizationRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw BusinessException.badRequest("ORG_CODE_EXISTS", "组织编码已存在");
        }
    }

    private void requireUniqueModuleCode(String code, Long id) {
        if (id == null ? moduleRepository.existsByCodeIgnoreCase(code)
                : moduleRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw BusinessException.badRequest("MODULE_CODE_EXISTS", "模块编码已存在");
        }
    }

    private void requireUniquePermissionCode(String code, Long id) {
        if (id == null ? permissionRepository.existsByCodeIgnoreCase(code)
                : permissionRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw BusinessException.badRequest("PERMISSION_CODE_EXISTS", "权限编码已存在");
        }
    }

    private void requireUniqueRoleCode(String code, Long id) {
        if (id == null ? roleRepository.existsByCodeIgnoreCase(code)
                : roleRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw BusinessException.badRequest("ROLE_CODE_EXISTS", "角色编码已存在");
        }
    }

    private void requireUniquePostCode(String code, Long id) {
        if (id == null ? postRepository.existsByCodeIgnoreCase(code)
                : postRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw BusinessException.badRequest("POST_CODE_EXISTS", "岗位编码已存在");
        }
    }

    private void requireUniqueDictionaryTypeCode(String code, Long id) {
        if (id == null ? dictionaryTypeRepository.existsByCodeIgnoreCase(code)
                : dictionaryTypeRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw BusinessException.badRequest("DICT_CODE_EXISTS", "字典编码已存在");
        }
    }

    private OrganizationView toOrganizationView(Organization organization) {
        Organization parent = organization.getParent();
        return new OrganizationView(
                organization.getId(),
                parent == null ? null : parent.getId(),
                parent == null ? null : parent.getName(),
                organization.getCode(),
                organization.getName(),
                organization.getType(),
                organization.getSortOrder(),
                organization.getLeader(),
                organization.getPhone(),
                organization.getEmail(),
                organization.getDescription(),
                organization.isEnabled(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }

    private ModuleView toModuleView(ServiceModule module) {
        return new ModuleView(
                module.getId(),
                module.getCode(),
                module.getName(),
                module.getRoutePrefix(),
                module.getDescription(),
                module.isEnabled(),
                module.getSortOrder(),
                module.getCreatedAt(),
                module.getUpdatedAt()
        );
    }

    private PermissionView toPermissionView(Permission permission) {
        ServiceModule module = permission.getModule();
        return new PermissionView(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                module == null ? null : module.getId(),
                module == null ? null : module.getCode(),
                module == null ? null : module.getName(),
                permission.getDescription(),
                permission.isEnabled(),
                permission.getSortOrder(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }

    private RoleAdminView toRoleAdminView(Role role) {
        List<Permission> permissions = role.getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .toList();
        return new RoleAdminView(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isEnabled(),
                role.getSortOrder(),
                permissions.stream().map(Permission::getId).toList(),
                permissions.stream().map(Permission::getCode).toList(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    private PostView toPostView(Post post) {
        return new PostView(
                post.getId(),
                post.getCode(),
                post.getName(),
                post.getSortOrder(),
                post.getDescription(),
                post.isEnabled(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private MenuView toMenuView(Menu menu) {
        Menu parent = menu.getParent();
        ServiceModule module = menu.getModule();
        return new MenuView(
                menu.getId(),
                parent == null ? null : parent.getId(),
                parent == null ? null : parent.getName(),
                module == null ? null : module.getId(),
                module == null ? null : module.getName(),
                menu.getName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getIcon(),
                menu.getPermissionCode(),
                menu.getSortOrder(),
                menu.isVisible(),
                menu.isEnabled(),
                menu.getCreatedAt(),
                menu.getUpdatedAt()
        );
    }

    private NavigationMenuView toNavigationMenuView(NavigationMenuNode node) {
        Menu menu = node.menu;
        Menu parent = menu.getParent();
        return new NavigationMenuView(
                menu.getId(),
                parent == null ? null : parent.getId(),
                menu.getName(),
                menu.getPath(),
                menu.getIcon(),
                menu.getPermissionCode(),
                menu.getSortOrder(),
                node.children.stream().map(this::toNavigationMenuView).toList()
        );
    }

    private boolean belongsToModule(Menu menu, String moduleCode) {
        if (moduleCode == null || moduleCode.isBlank()) {
            return true;
        }
        ServiceModule module = menu.getModule();
        return module != null && moduleCode.equalsIgnoreCase(module.getCode());
    }

    private boolean canAccessMenu(Menu menu, Set<String> permissions, boolean admin) {
        if (admin) {
            return true;
        }
        String permissionCode = menu.getPermissionCode();
        if (permissionCode == null || permissionCode.isBlank()) {
            return true;
        }
        for (String candidate : permissionCode.split("[,;|\\s]+")) {
            if (candidate.isBlank()) {
                continue;
            }
            if (permissions.contains(candidate) || hasPermissionAlias(candidate, permissions)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermissionAlias(String permission, Set<String> permissions) {
        return "ticket:read:own".equals(permission) && permissions.contains("ticket:read:all");
    }

    private DictionaryTypeView toDictionaryTypeView(DictionaryType type) {
        return new DictionaryTypeView(
                type.getId(),
                type.getCode(),
                type.getName(),
                type.getDescription(),
                type.isEnabled(),
                type.getCreatedAt(),
                type.getUpdatedAt()
        );
    }

    private DictionaryItemView toDictionaryItemView(DictionaryItem item) {
        DictionaryType type = item.getType();
        return new DictionaryItemView(
                item.getId(),
                type.getId(),
                type.getCode(),
                item.getLabel(),
                item.getValue(),
                item.getSortOrder(),
                item.getRemark(),
                item.isEnabled(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static final class NavigationMenuNode {
        private final Menu menu;
        private final List<NavigationMenuNode> children = new ArrayList<>();

        private NavigationMenuNode(Menu menu) {
            this.menu = menu;
        }
    }
}
