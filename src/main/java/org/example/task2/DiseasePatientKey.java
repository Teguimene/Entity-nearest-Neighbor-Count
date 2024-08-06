package org.example.task2;

import java.util.Objects;

public class DiseasePatientKey {
    private final String uri1;
    private final String uri2;
    private final String value;

    public DiseasePatientKey(DiseasePatient diseasePatient) {
        this.uri1 = diseasePatient.getUriPatient();
        this.uri2 = diseasePatient.getUriDisease();
        this.value = diseasePatient.getValuePrediction();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiseasePatientKey that = (DiseasePatientKey) o;
        return Objects.equals(value, that.value) && Objects.equals(uri1, that.uri1) && Objects.equals(uri2, that.uri2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri1, uri2, value);
    }
}
