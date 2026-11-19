package org.sunbird.scoringengine.models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class CriteriaModelTest {

    @Test
    public void getterSettersRoundTripAllFields() {
        CriteriaModel criteriaModel = new CriteriaModel();
        List<QualifierModel> qualifiers = new ArrayList<>();
        qualifiers.add(new QualifierModel());

        criteriaModel.setCriteria("criteria1");
        criteriaModel.setTotalScore(10.0);
        criteriaModel.setWeightage(0.5);
        criteriaModel.setWeightedAvg(5.0);
        criteriaModel.setMaxScore(20.0);
        criteriaModel.setMinScore(2.0);
        criteriaModel.setMaxWeightedAvg(10.0);
        criteriaModel.setMinWeightedAvg(1.0);
        criteriaModel.setWeightedScore(50.0);
        criteriaModel.setDescription("description");
        criteriaModel.setQualifiers(qualifiers);
        criteriaModel.setQualifiedMinCriteria(false);

        assertEquals("criteria1", criteriaModel.getCriteria());
        assertEquals(10.0, criteriaModel.getTotalScore(), 0.0001);
        assertEquals(0.5, criteriaModel.getWeightage(), 0.0001);
        assertEquals(5.0, criteriaModel.getWeightedAvg(), 0.0001);
        assertEquals(20.0, criteriaModel.getMaxScore(), 0.0001);
        assertEquals(2.0, criteriaModel.getMinScore(), 0.0001);
        assertEquals(10.0, criteriaModel.getMaxWeightedAvg(), 0.0001);
        assertEquals(1.0, criteriaModel.getMinWeightedAvg(), 0.0001);
        assertEquals(50.0, criteriaModel.getWeightedScore(), 0.0001);
        assertEquals("description", criteriaModel.getDescription());
        assertSame(qualifiers, criteriaModel.getQualifiers());
        assertFalse(criteriaModel.isQualifiedMinCriteria());
    }
}
