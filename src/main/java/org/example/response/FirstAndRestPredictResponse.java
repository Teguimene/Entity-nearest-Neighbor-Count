package org.example.response;

import lombok.Data;
import org.example.task2.DiseasePatient;

import java.util.ArrayList;
import java.util.List;

@Data
public class FirstAndRestPredictResponse {
    DiseasePatient firstPrediction = new DiseasePatient();
    DiseasePatient restOfPrediction = new DiseasePatient();
}
