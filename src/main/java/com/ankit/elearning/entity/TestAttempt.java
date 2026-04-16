package com.ankit.elearning.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Test test;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int score;

    // ✅ GETTERS & SETTERS

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}