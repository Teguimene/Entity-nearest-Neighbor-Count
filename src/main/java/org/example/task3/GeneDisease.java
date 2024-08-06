package org.example.task3;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

@Data
public class GeneDisease {
    @CsvBindByPosition(position = 0)
    private String uriGene;

    @CsvBindByPosition(position = 1)
    private String uriDisease;

    @CsvBindByPosition(position = 2)
    private String valuePrediction = "0";
}
