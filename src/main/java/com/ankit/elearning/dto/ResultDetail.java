package com.ankit.elearning.dto;

public class ResultDetail {

    private String question;
    private String selected;
    private String correct;
    private boolean isCorrect;

    public ResultDetail(String question, String selected, String correct, boolean isCorrect) {
        this.question = question;
        this.selected = selected;
        this.correct = correct;
        this.isCorrect = isCorrect;
    }

    // getters
    public String getQuestion() { return question; }
    public String getSelected() { return selected; }
    public String getCorrect() { return correct; }
    public boolean isCorrect() { return isCorrect; }
}