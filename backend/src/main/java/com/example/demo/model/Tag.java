package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Tag (*) <-----> (*) Task
// Many-to-many: a task can have multiple tags, a tag can be on multiple tasks.
// The owning side (join table definition) is on Task.tags.
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // mappedBy = "tags" means Task.tags owns the @JoinTable.
    // Using Set instead of List for ManyToMany -- avoids Hibernate bag performance issues.
    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();

    @Override
    public String toString() {
        return "Tag{id=" + id + ", name='" + name + "'}";
    }
}
