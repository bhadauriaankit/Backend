package com.ankit.elearning.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "tests")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private int duration; // in minutes

    private boolean published = false; // admin must approve

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "test_questions",
            joinColumns = @JoinColumn(name = "test_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public int getDuration() { return duration; }
    public void setDuration(int d) { this.duration = d; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean p) { this.published = p; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> q) { this.questions = q; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User u) { this.createdBy = u; }
}