package org.example.response;

import lombok.Data;

@Data
public class ResponseAccuracy {
    private Double accuracy;
    private long goodPrediction;
}
