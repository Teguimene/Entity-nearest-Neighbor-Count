package org.example.task1;

import java.util.Objects;

public class ProteinKey {
    private final String uri1;
    private final String uri2;
    private final String value;

    public ProteinKey(Protein protein) {
        this.uri1 = protein.getUriProteinOne();
        this.uri2 = protein.getUriProteinTwo();
        this.value = protein.getValuePrediction();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProteinKey that = (ProteinKey) o;
        return Objects.equals(value, that.value) && Objects.equals(uri1, that.uri1) && Objects.equals(uri2, that.uri2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri1, uri2, value);
    }
}
