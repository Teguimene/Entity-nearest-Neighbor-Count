package org.example.task2;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

@Data
public class MatchDiseasePatient {
    @CsvBindByPosition(position = 0)
    private String uriPatient;

    @CsvBindByPosition(position = 1)
    private String uriDisease;
}
