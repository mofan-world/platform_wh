package com.example.issuetracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "service_modules")
public class ServiceModule extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 120)
    private String routePrefix;

    private String description;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int sortOrder;
}
