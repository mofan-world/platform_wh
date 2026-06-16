package com.example.issuetracker.identity;

import com.example.issuetracker.identity.IdentityManagementDtos.NavigationMenuView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/navigation")
public class NavigationController {

    private final IdentityManagementService service;

    @GetMapping("/menus")
    public List<NavigationMenuView> menus(@RequestParam(required = false) String module) {
        return service.listNavigationMenus(module);
    }
}
