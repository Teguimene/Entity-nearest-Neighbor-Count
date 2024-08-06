package org.example.task3;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;
import org.example.task2.DiseasePatient;
import org.example.task2.DiseasePatientKey;

import java.util.Objects;

public class GeneDiseasekey {
    private final String uri1;
    private final String uri2;
    private final String value;

    public GeneDiseasekey(GeneDisease geneDisease) {
        this.uri1 = geneDisease.getUriGene();
        this.uri2 = geneDisease.getUriDisease();
        this.value = geneDisease.getValuePrediction();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneDiseasekey that = (GeneDiseasekey) o;
        return Objects.equals(value, that.value) && Objects.equals(uri1, that.uri1) && Objects.equals(uri2, that.uri2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri1, uri2, value);
    }
}
