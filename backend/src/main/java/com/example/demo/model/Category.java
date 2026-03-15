package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Category (1) -----> (*) Task
// One category can contain many tasks. Each task belongs to at most one category.
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String color;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // mappedBy = "category" means the Task.category field owns the FK column.
    // This side is the "inverse" -- it doesn't create a column, just reads the relationship.
    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @Override
    public String toString() {
        // Exclude 'tasks' to avoid infinite recursion (Task.toString -> Category.toString -> ...)
        return "Category{id=" + id + ", name='" + name + "', color='" + color + "'}";
    }
}
