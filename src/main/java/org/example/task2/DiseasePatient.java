package org.example.task2;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

@Data
public class DiseasePatient {
    @CsvBindByPosition(position = 1)
    private String uriPatient;

    @CsvBindByPosition(position = 0)
    private String uriDisease;

    @CsvBindByPosition(position = 2)
    private String valuePrediction = "0";

    public int compareTo(DiseasePatient other) {
        int cmp = this.uriPatient.compareTo(other.uriPatient);
        if (cmp == 0) {
            return this.uriDisease.compareTo(other.uriDisease);
        }
        return cmp;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        DiseasePatient other = (DiseasePatient) obj;
        return this.uriPatient.equals(other.uriPatient) && this.uriDisease == other.uriDisease;
    }
}
