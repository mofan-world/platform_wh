package com.example.issuetracker.repository;

import com.example.issuetracker.domain.Permission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @Override
    @EntityGraph(attributePaths = "module")
    List<Permission> findAll();
}
