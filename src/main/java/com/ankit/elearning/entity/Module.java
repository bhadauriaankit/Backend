package com.ankit.elearning.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "modules")
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String title;

    @Column(nullable = false)
    @NotBlank
    private String type;           // VIDEO, READING

    private String  videoUrl;

    @Column(length = 20000)
    private String  content;

    private Integer duration;      // minutes
    @PositiveOrZero
    private Integer orderIndex;

    // EAGER — ModuleService.getModulesByTest() checks test.getAuthor().getEmail()
    // which needs an open session if lazy
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "test_id")
    @JsonIgnore
    private Test test;

    // EAGER — same pattern; avoids "Could not initialize proxy [User#N]"
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id")
    @JsonIgnore
    private User createdBy;

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Long    getId()                        { return id; }
    public void    setId(Long id)                 { this.id = id; }
    public String  getTitle()                     { return title; }
    public void    setTitle(String t)             { this.title = t; }
    public String  getType()                      { return type; }
    public void    setType(String t)              { this.type = t; }
    public String  getVideoUrl()                  { return videoUrl; }
    public void    setVideoUrl(String v)          { this.videoUrl = v; }
    public String  getContent()                   { return content; }
    public void    setContent(String c)           { this.content = c; }
    public Integer getDuration()                  { return duration; }
    public void    setDuration(Integer d)         { this.duration = d; }
    public Integer getOrderIndex()                { return orderIndex; }
    public void    setOrderIndex(Integer o)       { this.orderIndex = o; }
    public Test    getTest()                      { return test; }
    public void    setTest(Test t)                { this.test = t; }
    public User    getCreatedBy()                 { return createdBy; }
    public void    setCreatedBy(User u)           { this.createdBy = u; }
}
