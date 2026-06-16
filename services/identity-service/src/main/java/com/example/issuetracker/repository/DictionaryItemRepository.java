package com.example.issuetracker.repository;

import com.example.issuetracker.domain.DictionaryItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DictionaryItemRepository extends JpaRepository<DictionaryItem, Long> {

    @EntityGraph(attributePaths = "type")
    List<DictionaryItem> findByTypeId(Long typeId);
}
