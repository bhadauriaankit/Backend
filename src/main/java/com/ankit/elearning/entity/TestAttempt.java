package com.ankit.elearning.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_attempts")
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "test_id", nullable = false)
    @JsonIgnore
    private Test test;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private AttemptStatus status;

    private Integer score;
    private Integer totalQuestions;
    private Double  percentage;
    private Boolean passed = false;   // explicit pass/fail flag

    public Long          getId()                         { return id; }
    public void          setId(Long id)                  { this.id = id; }
    public User          getUser()                       { return user; }
    public void          setUser(User u)                 { this.user = u; }
    public Test          getTest()                       { return test; }
    public void          setTest(Test t)                 { this.test = t; }
    public LocalDateTime getStartTime()                  { return startTime; }
    public void          setStartTime(LocalDateTime t)   { this.startTime = t; }
    public LocalDateTime getEndTime()                    { return endTime; }
    public void          setEndTime(LocalDateTime t)     { this.endTime = t; }
    public AttemptStatus getStatus()                     { return status; }
    public void          setStatus(AttemptStatus s)      { this.status = s; }
    public Integer       getScore()                      { return score; }
    public void          setScore(Integer s)             { this.score = s; }
    public Integer       getTotalQuestions()             { return totalQuestions; }
    public void          setTotalQuestions(Integer t)    { this.totalQuestions = t; }
    public Double        getPercentage()                 { return percentage; }
    public void          setPercentage(Double p)         { this.percentage = p; }
    public Boolean       getPassed()                     { return passed; }
    public void          setPassed(Boolean p)            { this.passed = p; }
}