package org.sunbird.scoringengine.util;

import org.junit.Test;
import org.sunbird.scoringengine.models.CriteriaModel;
import org.sunbird.scoringengine.models.EvaluatorModel;
import org.sunbird.scoringengine.models.QualifierModel;
import org.sunbird.scoringengine.schema.model.Criteria;
import org.sunbird.scoringengine.schema.model.Qualifier;
import org.sunbird.scoringengine.schema.model.Range;
import org.sunbird.scoringengine.schema.model.ScoringTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ComputeScoresTest {

    private Criteria buildCriteria(String name, double maxScore, double minAcceptableScore, double weightage, Qualifier... qualifiers) {
        Criteria criteria = new Criteria();
        criteria.setCriteria(name);
        criteria.setMax_score(maxScore);
        criteria.setMin_acceptable_score(minAcceptableScore);
        criteria.setWeightage(weightage);
        criteria.setQualifiers(new ArrayList<>(Collections.unmodifiableList(java.util.Arrays.asList(qualifiers))));
        return criteria;
    }

    private ScoringTemplate buildTemplate(Criteria criteria) {
        ScoringTemplate template = new ScoringTemplate();
        template.setTemplate_id("temp-01");
        template.setTemplateName("Test Template");
        template.setRootOrg("rootOrg1");
        template.setOrg("org1");
        template.setCriteria(Collections.singletonList(criteria));
        template.setScore_grades(new ArrayList<>());
        template.setStatus_on_min_criteria(new HashMap<>());
        return template;
    }

    private EvaluatorModel buildEvaluatorModel(String criteriaName, QualifierModel... qualifierModels) {
        CriteriaModel criteriaModel = new CriteriaModel();
        criteriaModel.setCriteria(criteriaName);
        criteriaModel.setQualifiers(new ArrayList<>(Collections.unmodifiableList(java.util.Arrays.asList(qualifierModels))));

        EvaluatorModel evaluatorModel = new EvaluatorModel();
        List<CriteriaModel> criteriaModels = new ArrayList<>();
        criteriaModels.add(criteriaModel);
        evaluatorModel.setCriteriaModels(criteriaModels);
        return evaluatorModel;
    }

    @Test
    public void computeV2UsesRangedScoringWhenEvaluatedIsNull() {
        Qualifier qualifier = new Qualifier();
        qualifier.setQualifier("q1");
        HashMap<String, Range> rangeScores = new HashMap<>();
        Range range = new Range();
        range.setMin(10);
        range.setMax(20);
        range.setAssignedValue(3);
        rangeScores.put("10-20", range);
        qualifier.setRange_score(rangeScores);

        Criteria criteria = buildCriteria("c1", 10.0, 2.0, 0.5, qualifier);
        ScoringTemplate template = buildTemplate(criteria);

        QualifierModel qualifierModel = new QualifierModel();
        qualifierModel.setName("q1");
        qualifierModel.setEvaluated(null);
        qualifierModel.setScoreRange("10-20");

        EvaluatorModel evaluatorModel = buildEvaluatorModel("c1", qualifierModel);

        new ComputeScores(template).computeV2(evaluatorModel);

        assertEquals("ranged", qualifierModel.getScoringType());
        assertEquals(3.0, qualifierModel.getScoreValue(), 0.0001);
    }

    @Test
    public void computeV2AppliesMaxScoreModifierAndDisqualifies() {
        Qualifier qualifier = new Qualifier();
        qualifier.setQualifier("q1");
        HashMap<String, Integer> fixedScores = new HashMap<>();
        fixedScores.put("No", 0);
        qualifier.setFixed_score(fixedScores);
        qualifier.setModify_max_score(true);
        HashMap<String, Integer> maxScoreModifyValue = new HashMap<>();
        maxScoreModifyValue.put("No", 4);
        qualifier.setMax_score_modify_value(maxScoreModifyValue);
        qualifier.setDisqualifyOption(Collections.singletonList("No"));

        Criteria criteria = buildCriteria("c1", 10.0, 2.0, 0.5, qualifier);
        ScoringTemplate template = buildTemplate(criteria);

        QualifierModel qualifierModel = new QualifierModel();
        qualifierModel.setName("q1");
        qualifierModel.setEvaluated("No");

        EvaluatorModel evaluatorModel = buildEvaluatorModel("c1", qualifierModel);

        new ComputeScores(template).computeV2(evaluatorModel);

        assertEquals(6.0, evaluatorModel.getCriteriaModels().get(0).getMaxScore(), 0.0001);
        assertFalse(evaluatorModel.isQualified());
    }

    @Test
    public void computeV2AppliesMinScoreWeightageWhenEnabled() {
        Qualifier qualifier = new Qualifier();
        qualifier.setQualifier("q1");
        HashMap<String, Integer> fixedScores = new HashMap<>();
        fixedScores.put("Yes", 5);
        qualifier.setFixed_score(fixedScores);

        Criteria criteria = buildCriteria("c1", 10.0, 2.0, 0.5, qualifier);
        criteria.setMin_score_weightage_enable(true);
        criteria.setMin_score_weightage(0.5);
        ScoringTemplate template = buildTemplate(criteria);

        QualifierModel qualifierModel = new QualifierModel();
        qualifierModel.setName("q1");
        qualifierModel.setEvaluated("Yes");

        EvaluatorModel evaluatorModel = buildEvaluatorModel("c1", qualifierModel);

        new ComputeScores(template).computeV2(evaluatorModel);

        CriteriaModel criteriaModel = evaluatorModel.getCriteriaModels().get(0);
        assertEquals(criteriaModel.getMaxScore() * 0.5, criteriaModel.getMinScore(), 0.0001);
    }

    @Test
    public void computeV2SetsScoreGradeFromStatusOnMinCriteriaWhenBelowMinScore() {
        Qualifier qualifier = new Qualifier();
        qualifier.setQualifier("q1");
        HashMap<String, Integer> fixedScores = new HashMap<>();
        fixedScores.put("Low", 1);
        qualifier.setFixed_score(fixedScores);

        Criteria criteria = buildCriteria("c1", 10.0, 8.0, 0.5, qualifier);
        ScoringTemplate template = buildTemplate(criteria);
        HashMap<String, String> statusOnMinCriteria = new HashMap<>();
        statusOnMinCriteria.put("fail", "Needs Improvement");
        template.setStatus_on_min_criteria(statusOnMinCriteria);

        QualifierModel qualifierModel = new QualifierModel();
        qualifierModel.setName("q1");
        qualifierModel.setEvaluated("Low");

        EvaluatorModel evaluatorModel = buildEvaluatorModel("c1", qualifierModel);

        new ComputeScores(template).computeV2(evaluatorModel);

        assertEquals("Needs Improvement", evaluatorModel.getScoreGrade());
        assertFalse(evaluatorModel.isQualifiedMinCriteria());
    }

    @Test
    public void computeV2SetsScoreGradeFromScoreGradesWhenMinCriteriaPassed() {
        Qualifier qualifier = new Qualifier();
        qualifier.setQualifier("q1");
        HashMap<String, Integer> fixedScores = new HashMap<>();
        fixedScores.put("High", 5);
        qualifier.setFixed_score(fixedScores);

        Criteria criteria = buildCriteria("c1", 10.0, 1.0, 1.0, qualifier);
        ScoringTemplate template = buildTemplate(criteria);
        HashMap<String, String> nonMatchingGrade = new HashMap<>();
        nonMatchingGrade.put("200-300", "Grade B");
        HashMap<String, String> matchingGrade = new HashMap<>();
        matchingGrade.put("0-100", "Grade A");
        template.setScore_grades(java.util.Arrays.asList(nonMatchingGrade, matchingGrade));

        QualifierModel qualifierModel = new QualifierModel();
        qualifierModel.setName("q1");
        qualifierModel.setEvaluated("High");

        EvaluatorModel evaluatorModel = buildEvaluatorModel("c1", qualifierModel);

        new ComputeScores(template).computeV2(evaluatorModel);

        assertEquals("Grade A", evaluatorModel.getScoreGrade());
    }

    @Test
    public void computeV2SwallowsExceptionWhenScoreGradeKeyIsMalformed() {
        Qualifier qualifier = new Qualifier();
        qualifier.setQualifier("q1");
        HashMap<String, Integer> fixedScores = new HashMap<>();
        fixedScores.put("High", 5);
        qualifier.setFixed_score(fixedScores);

        Criteria criteria = buildCriteria("c1", 10.0, 1.0, 1.0, qualifier);
        ScoringTemplate template = buildTemplate(criteria);
        HashMap<String, String> grade = new HashMap<>();
        grade.put("malformed", "Grade A");
        template.setScore_grades(Collections.singletonList(grade));

        QualifierModel qualifierModel = new QualifierModel();
        qualifierModel.setName("q1");
        qualifierModel.setEvaluated("High");

        EvaluatorModel evaluatorModel = buildEvaluatorModel("c1", qualifierModel);

        new ComputeScores(template).computeV2(evaluatorModel);

        assertNull(evaluatorModel.getScoreGrade());
        assertTrue(evaluatorModel.getIdentifier().startsWith("lex_score_"));
    }
}
