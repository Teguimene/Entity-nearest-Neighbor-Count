package org.example.response;

import lombok.Data;

@Data
public class ScoreSDBResponse {
    private double score = 0.0;
    private double tscore = 0.0;
    private double p_value = 0.0;
}
