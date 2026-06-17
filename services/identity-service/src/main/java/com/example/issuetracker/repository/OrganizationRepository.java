package com.example.issuetracker.repository;

import com.example.issuetracker.domain.Organization;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    Optional<Organization> findByCodeIgnoreCase(String code);

    @Override
    @EntityGraph(attributePaths = "parent")
    List<Organization> findAll();
}
