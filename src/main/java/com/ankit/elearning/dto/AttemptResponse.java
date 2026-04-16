package com.ankit.elearning.dto;

public class AttemptResponse {

    private int score;
    private int correct;
    private int wrong;
    private int skipped;

    public AttemptResponse(int score, int correct, int wrong, int skipped) {
        this.score = score;
        this.correct = correct;
        this.wrong = wrong;
        this.skipped = skipped;
    }

    // getters
}