package com.example.tools_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tool_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double replacementValue;

    @Column(name = "tariff_id", nullable = false)
    private Long tariffId;

    @OneToMany(mappedBy = "toolGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference  // ← AÑADIR ESTO
    private List<ToolUnitEntity> units = new ArrayList<>();
}