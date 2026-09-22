package com.smartspace.trust.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustScoreId implements Serializable {
    private Rating.SubjectType subjectType;
    private Long subjectId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrustScoreId)) return false;
        TrustScoreId that = (TrustScoreId) o;
        return subjectType == that.subjectType && Objects.equals(subjectId, that.subjectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectType, subjectId);
    }
}
