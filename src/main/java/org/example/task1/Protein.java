package org.example.task1;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

@Data
public class Protein {
    @CsvBindByPosition(position = 0)
    private String uriProteinOne;

    @CsvBindByPosition(position = 1)
    private String uriProteinTwo;

    @CsvBindByPosition(position = 2)
    private String valuePrediction;
}
