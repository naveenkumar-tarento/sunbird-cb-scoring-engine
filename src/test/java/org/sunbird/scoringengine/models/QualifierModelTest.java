package org.sunbird.scoringengine.models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class QualifierModelTest {

    @Test
    public void getterSettersRoundTripAllFields() {
        QualifierModel qualifierModel = new QualifierModel();
        List<HashMap<String, Object>> options = new ArrayList<>();
        options.add(new HashMap<>());

        qualifierModel.setName("qualifier1");
        qualifierModel.setEvaluated("yes");
        qualifierModel.setDescription("description");
        qualifierModel.setScoreValue(4.5);
        qualifierModel.setScoreRange("0-10");
        qualifierModel.setScoringType("fixed");
        qualifierModel.setOptions(options);

        assertEquals("qualifier1", qualifierModel.getName());
        assertEquals("yes", qualifierModel.getEvaluated());
        assertEquals("description", qualifierModel.getDescription());
        assertEquals(4.5, qualifierModel.getScoreValue(), 0.0001);
        assertEquals("0-10", qualifierModel.getScoreRange());
        assertEquals("fixed", qualifierModel.getScoringType());
        assertSame(options, qualifierModel.getOptions());
    }
}
