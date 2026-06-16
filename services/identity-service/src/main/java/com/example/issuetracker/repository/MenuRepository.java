package com.example.issuetracker.repository;

import com.example.issuetracker.domain.Menu;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Override
    @EntityGraph(attributePaths = {"parent", "module"})
    List<Menu> findAll();
}
