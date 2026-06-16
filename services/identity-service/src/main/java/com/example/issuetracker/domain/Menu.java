package com.example.issuetracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "menus")
public class Menu extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private ServiceModule module;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String path;

    @Column(length = 200)
    private String component;

    @Column(length = 100)
    private String icon;

    @Column(length = 100)
    private String permissionCode;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean visible = true;

    @Column(nullable = false)
    private boolean enabled = true;
}
