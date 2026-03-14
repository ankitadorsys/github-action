package com.example.demo.model;

import com.example.demo.api.model.TaskPriority;
import com.example.demo.api.model.TaskStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    private LocalDate dueDate;

    @Column(name = "user_id")
    private String userId;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // --- RELATIONSHIPS ---

    // @ManyToOne: many tasks belong to one category.
    // FetchType.LAZY = don't load the category until it's actually accessed.
    // @JoinColumn = the FK column name in the tasks table.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // @ManyToMany: a task can have many tags, a tag can be on many tasks.
    // This is the OWNING side -- it defines the join table.
    // JoinTable creates: task_tags(task_id, tag_id)
    @ManyToMany
    @JoinTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // @OneToMany: one task has many comments.
    // CascadeType.ALL = when you save/delete a task, its comments are saved/deleted too.
    // orphanRemoval = if a comment is removed from this list, delete it from the DB.
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskComment> comments = new ArrayList<>();

    // --- HELPER METHODS for bidirectional sync ---

    public void addComment(TaskComment comment) {
        comments.add(comment);
        comment.setTask(this);
    }

    public void removeComment(TaskComment comment) {
        comments.remove(comment);
        comment.setTask(null);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getTasks().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getTasks().remove(this);
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "', status=" + status + "}";
    }
}
