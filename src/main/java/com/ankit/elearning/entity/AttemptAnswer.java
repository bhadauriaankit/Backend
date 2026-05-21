package com.ankit.elearning.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "attempt_answers")
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER — needed when building result DTOs (getQuestion().getQuestionText() etc.)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt attempt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private String  selectedOption;
    private boolean isCorrect;

    public Long        getId()                      { return id; }
    public void        setId(Long id)               { this.id = id; }
    public TestAttempt getAttempt()                 { return attempt; }
    public void        setAttempt(TestAttempt a)    { this.attempt = a; }
    public Question    getQuestion()                { return question; }
    public void        setQuestion(Question q)      { this.question = q; }
    public String      getSelectedOption()          { return selectedOption; }
    public void        setSelectedOption(String s)  { this.selectedOption = s; }
    public boolean     isCorrect()                  { return isCorrect; }
    public void        setCorrect(boolean c)        { this.isCorrect = c; }
}