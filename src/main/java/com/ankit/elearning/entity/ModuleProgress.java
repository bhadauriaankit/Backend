package com.ankit.elearning.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "module_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module_id"}))
public class ModuleProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER — we always need user and module info when reading progress
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    private boolean       completed  = false;
    private LocalDateTime completedAt;

    public Long          getId()                       { return id; }
    public void          setId(Long id)                { this.id = id; }
    public User          getUser()                     { return user; }
    public void          setUser(User u)               { this.user = u; }
    public Module        getModule()                   { return module; }
    public void          setModule(Module m)           { this.module = m; }
    public boolean       isCompleted()                 { return completed; }
    public void          setCompleted(boolean c)       { this.completed = c; }
    public LocalDateTime getCompletedAt()              { return completedAt; }
    public void          setCompletedAt(LocalDateTime t) { this.completedAt = t; }
}