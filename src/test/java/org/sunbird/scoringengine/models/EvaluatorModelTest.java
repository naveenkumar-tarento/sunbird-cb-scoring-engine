package org.sunbird.scoringengine.models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class EvaluatorModelTest {

    @Test
    public void getterSettersRoundTripAllFields() {
        EvaluatorModel evaluatorModel = new EvaluatorModel();
        List<CriteriaModel> criteriaModels = new ArrayList<>();
        criteriaModels.add(new CriteriaModel());

        evaluatorModel.setIdentifier("id1");
        evaluatorModel.setRootOrg("rootOrg1");
        evaluatorModel.setOrg("org1");
        evaluatorModel.setResourceId("resource1");
        evaluatorModel.setResourceType("course");
        evaluatorModel.setUserId("user1");
        evaluatorModel.setTemplateId("template1");
        evaluatorModel.setTempleteName("templateName1");
        evaluatorModel.setCompositeScore(10.5);
        evaluatorModel.setMinimunQualifier(2.5);
        evaluatorModel.setFinalMaxScore(100.0);
        evaluatorModel.setFinalMinScore(10.0);
        evaluatorModel.setFinalTotalScore(55.0);
        evaluatorModel.setFinalWeightedAvg(30.0);
        evaluatorModel.setFinalMaxWeightedAvg(60.0);
        evaluatorModel.setFinalMinWeightedAvg(5.0);
        evaluatorModel.setFinalWeightedScore(45.0);
        evaluatorModel.setGetLatestRecordEnabled(true);
        evaluatorModel.setScoreGrade("A");
        evaluatorModel.setQualifiedMinCriteria(false);
        evaluatorModel.setGateCriteriaCheck(true);
        evaluatorModel.setQualified(false);
        evaluatorModel.setCriteriaModels(criteriaModels);
        evaluatorModel.setTimeStamp("2026-01-01 00:00:00");
        evaluatorModel.setVersionKey("v1");

        assertEquals("id1", evaluatorModel.getIdentifier());
        assertEquals("rootOrg1", evaluatorModel.getRootOrg());
        assertEquals("org1", evaluatorModel.getOrg());
        assertEquals("resource1", evaluatorModel.getResourceId());
        assertEquals("course", evaluatorModel.getResourceType());
        assertEquals("user1", evaluatorModel.getUserId());
        assertEquals("template1", evaluatorModel.getTemplateId());
        assertEquals("templateName1", evaluatorModel.getTempleteName());
        assertEquals(10.5, evaluatorModel.getCompositeScore(), 0.0001);
        assertEquals(2.5, evaluatorModel.getMinimunQualifier(), 0.0001);
        assertEquals(100.0, evaluatorModel.getFinalMaxScore(), 0.0001);
        assertEquals(10.0, evaluatorModel.getFinalMinScore(), 0.0001);
        assertEquals(55.0, evaluatorModel.getFinalTotalScore(), 0.0001);
        assertEquals(30.0, evaluatorModel.getFinalWeightedAvg(), 0.0001);
        assertEquals(60.0, evaluatorModel.getFinalMaxWeightedAvg(), 0.0001);
        assertEquals(5.0, evaluatorModel.getFinalMinWeightedAvg(), 0.0001);
        assertEquals(45.0, evaluatorModel.getFinalWeightedScore(), 0.0001);
        assertTrue(evaluatorModel.isGetLatestRecordEnabled());
        assertEquals("A", evaluatorModel.getScoreGrade());
        assertFalse(evaluatorModel.isQualifiedMinCriteria());
        assertTrue(evaluatorModel.isGateCriteriaCheck());
        assertFalse(evaluatorModel.isQualified());
        assertSame(criteriaModels, evaluatorModel.getCriteriaModels());
        assertEquals("2026-01-01 00:00:00", evaluatorModel.getTimeStamp());
        assertEquals("v1", evaluatorModel.getVersionKey());
    }
}
