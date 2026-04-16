package com.ankit.elearning.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String questionText;

    @Column(nullable = false) private String optionA;
    @Column(nullable = false) private String optionB;
    @Column(nullable = false) private String optionC;
    @Column(nullable = false) private String optionD;

    @Column(nullable = false)
    private String correctAnswer; // must be A, B, C, or D

    private int marks = 1;
    private int negativeMarks = 0;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy; // track author

    // getters & setters
    public Long getId() { return id; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String q) { this.questionText = q; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String a) { this.optionA = a; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String b) { this.optionB = b; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String c) { this.optionC = c; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String d) { this.optionD = d; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String ca) { this.correctAnswer = ca; }
    public int getMarks() { return marks; }
    public void setMarks(int m) { this.marks = m; }
    public int getNegativeMarks() { return negativeMarks; }
    public void setNegativeMarks(int nm) { this.negativeMarks = nm; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User u) { this.createdBy = u; }
}