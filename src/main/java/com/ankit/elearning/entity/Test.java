package com.ankit.elearning.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tests")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer duration; // minutes

    private boolean published = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.DRAFT;

    @Column(length = 1000)
    private String rejectionReason;

    // ── Author: lazy is fine, always @JsonIgnore'd ───────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;

    // ── Questions: EAGER so Jackson can serialize without open session ────────
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "test_questions",
        joinColumns = @JoinColumn(name = "test_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @JsonIgnoreProperties({"createdBy", "hibernateLazyInitializer", "handler"})
    private List<Question> questions = new ArrayList<>();

    // ── Modules: lazy + JsonIgnore — we never return them inside a Test JSON ──
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Module> modules = new ArrayList<>();

    // ── Getters & setters ─────────────────────────────────────────────────────
    public Long    getId()                      { return id; }
    public void    setId(Long id)               { this.id = id; }
    public String  getTitle()                   { return title; }
    public void    setTitle(String title)       { this.title = title; }
    public String  getDescription()             { return description; }
    public void    setDescription(String d)     { this.description = d; }
    public Integer getDuration()                { return duration; }
    public void    setDuration(Integer d)       { this.duration = d; }
    public boolean isPublished()                { return published; }
    public void    setPublished(boolean p)      { this.published = p; }
    public ApprovalStatus getApprovalStatus()   { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    public String getRejectionReason()          { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public User    getAuthor()                  { return author; }
    public void    setAuthor(User author)       { this.author = author; }
    public List<Question> getQuestions()        { return questions; }
    public void    setQuestions(List<Question> q) { this.questions = q; }
    public List<Module>   getModules()          { return modules; }
    public void    setModules(List<Module> m)   { this.modules = m; }
}
