package com.ankit.elearning.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    @NotBlank
    private String questionText;

    @Column(nullable = false)
    @NotBlank
    private String optionA;
    @Column(nullable = false)
    @NotBlank
    private String optionB;
    @Column(nullable = false)
    @NotBlank
    private String optionC;
    @Column(nullable = false)
    @NotBlank
    private String optionD;

    @Column(nullable = false)
    @NotBlank
    private String correctAnswer;   // A, B, C, D

    @Positive
    private Integer marks = 1;

    // nullable = true — when author is deleted we set this to null
    // rather than cascade-deleting all questions (they may be in active tests)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = true)
    @JsonIgnore
    private User createdBy;

    public Long    getId()                          { return id; }
    public void    setId(Long id)                   { this.id = id; }
    public String  getQuestionText()                { return questionText; }
    public void    setQuestionText(String q)        { this.questionText = q; }
    public String  getOptionA()                     { return optionA; }
    public void    setOptionA(String o)             { this.optionA = o; }
    public String  getOptionB()                     { return optionB; }
    public void    setOptionB(String o)             { this.optionB = o; }
    public String  getOptionC()                     { return optionC; }
    public void    setOptionC(String o)             { this.optionC = o; }
    public String  getOptionD()                     { return optionD; }
    public void    setOptionD(String o)             { this.optionD = o; }
    public String  getCorrectAnswer()               { return correctAnswer; }
    public void    setCorrectAnswer(String c)       { this.correctAnswer = c; }
    public Integer getMarks()                       { return marks; }
    public void    setMarks(Integer m)              { this.marks = m; }
    public User    getCreatedBy()                   { return createdBy; }
    public void    setCreatedBy(User u)             { this.createdBy = u; }
}
