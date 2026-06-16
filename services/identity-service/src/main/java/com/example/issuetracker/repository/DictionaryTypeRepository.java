package com.example.issuetracker.repository;

import com.example.issuetracker.domain.DictionaryType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryTypeRepository extends JpaRepository<DictionaryType, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
