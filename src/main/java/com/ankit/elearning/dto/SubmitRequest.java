package com.ankit.elearning.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class SubmitRequest {
    private Long attemptId;
    private Map<Long, String> answers;
}