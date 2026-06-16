package com.example.issuetracker.repository;

import com.example.issuetracker.domain.ServiceModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceModuleRepository extends JpaRepository<ServiceModule, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    Optional<ServiceModule> findByCode(String code);
}
