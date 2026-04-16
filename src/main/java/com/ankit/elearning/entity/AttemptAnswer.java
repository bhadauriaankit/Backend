package com.ankit.elearning.entity;
import com.ankit.elearning.entity.Question;
import com.ankit.elearning.entity.TestAttempt;
import jakarta.persistence.*;
@Entity
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TestAttempt attempt;

    @ManyToOne
    private Question question;

    private String selectedOption;

    // ✅ ADD THESE
    public TestAttempt getAttempt() { return attempt; }
    public void setAttempt(TestAttempt attempt) { this.attempt = attempt; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public String getSelectedOption() { return selectedOption; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
}