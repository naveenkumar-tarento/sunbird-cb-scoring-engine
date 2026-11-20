
package org.sunbird.scoringengine.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.sunbird.scoringengine.models.CriteriaModel;
import org.sunbird.scoringengine.models.EvaluatorModel;
import org.sunbird.scoringengine.models.QualifierModel;
import org.sunbird.scoringengine.schema.model.Criteria;
import org.sunbird.scoringengine.schema.model.Qualifier;
import org.sunbird.scoringengine.schema.model.Range;
import org.sunbird.scoringengine.schema.model.ScoringTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ComputeScores {

    private Logger logger = LoggerFactory.getLogger(ComputeScores.class);

    private SimpleDateFormat formatterDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String identifierPrefix = "lex_score_";

    private ScoringTemplate scoringTemplate;

    private static final String  MIN_SCORE_FAIL_CONST = "fail";

    public ComputeScores(ScoringTemplate scoringTemplate){
        this.scoringTemplate = scoringTemplate;
    }

    /**
     * Compute using scoring schema and with fixed and range type
     * @param evaluatorModel
     */
    public void computeV2(EvaluatorModel evaluatorModel) {

        for (CriteriaModel cm : evaluatorModel.getCriteriaModels()) {

            //to get maxscore , minacceptablescore
            Map<String, Criteria> criteriaMap = scoringTemplate.getCriteria().stream().collect(Collectors.toMap(c -> c.getCriteria(), c -> c));
            Criteria criteria = criteriaMap.get(cm.getCriteria());
            evaluatorModel.setTemplateId(scoringTemplate.getTemplate_id());
            evaluatorModel.setTempleteName(scoringTemplate.getTemplateName());
            evaluatorModel.setRootOrg(scoringTemplate.getRootOrg());
            evaluatorModel.setOrg(scoringTemplate.getOrg());

            double maxScore = criteria.getMax_score();
            cm.setMaxScore(maxScore);
            double minScore = criteria.getMin_acceptable_score();
            cm.setMinScore(minScore);
            double weightage = criteria.getWeightage();
            cm.setWeightage(weightage);


            Map<String, Qualifier> qualifierMap = criteria.getQualifiers().stream().collect(Collectors.toMap(Qualifier::getQualifier, qualifier -> qualifier));

            Map<String, Map<String, Integer>> qualifierFixedScores = criteria.getQualifiers().stream().collect(Collectors.toMap(Qualifier::getQualifier, Qualifier::getFixed_score));
            Map<String, Map<String, Range>> qualifierRangeScores = criteria.getQualifiers().stream().collect(Collectors.toMap(Qualifier::getQualifier, Qualifier::getRange_score));
            int maxScoreExcludedValue = applyQualifierScores(cm, evaluatorModel, qualifierMap, qualifierFixedScores, qualifierRangeScores);
            cm.setMaxScore(cm.getMaxScore() - maxScoreExcludedValue);
            if (Boolean.TRUE.equals(criteria.getMin_score_weightage_enable())) {
                cm.setMinScore(cm.getMaxScore() * criteria.getMin_score_weightage());
            }
            List<Double> scoreVals = cm.getQualifiers().stream().map(q -> q.getScoreValue()).collect(Collectors.toList());
            cm.setTotalScore(MathFunction.sum(scoreVals));
            cm.setWeightedAvg(MathFunction.weightedAvg(scoreVals, weightage));
            cm.setMaxWeightedAvg(MathFunction.maxWeightedAvg(cm.getMaxScore(), weightage));
            cm.setMinWeightedAvg(MathFunction.minWeightedAvg(minScore, weightage));
            cm.setWeightedScore(MathFunction.weightedScore(cm.getTotalScore(), cm.getMaxScore(), weightage));
        }

        List<Double> criteriaScoreValues = evaluatorModel.getCriteriaModels().stream().map(c -> c.getTotalScore()).collect(Collectors.toList());
        evaluatorModel.setFinalTotalScore(MathFunction.sum(criteriaScoreValues));

        List<Double> criteriaWeightedAvgVals = evaluatorModel.getCriteriaModels().stream().map(CriteriaModel::getWeightedAvg).collect(Collectors.toList());
        evaluatorModel.setFinalWeightedAvg(MathFunction.sum(criteriaWeightedAvgVals));


        List<Double> criteriaMaxScoreVals = evaluatorModel.getCriteriaModels().stream().map(CriteriaModel::getMaxScore).collect(Collectors.toList());
        evaluatorModel.setFinalMaxScore(MathFunction.sum(criteriaMaxScoreVals));

        List<Double> criteriaMaxWeightedAvgVals = evaluatorModel.getCriteriaModels().stream().map(CriteriaModel::getMaxWeightedAvg).collect(Collectors.toList());
        evaluatorModel.setFinalMaxWeightedAvg(MathFunction.sum(criteriaMaxWeightedAvgVals));

        List<Double> criteriaMinScoreVals = evaluatorModel.getCriteriaModels().stream().map(CriteriaModel::getMinScore).collect(Collectors.toList());
        evaluatorModel.setFinalMinScore(MathFunction.sum(criteriaMinScoreVals));

        List<Double> criteriaMinWeightedAvgVals = evaluatorModel.getCriteriaModels().stream().map(CriteriaModel::getMinWeightedAvg).collect(Collectors.toList());
        evaluatorModel.setFinalMinWeightedAvg(MathFunction.sum(criteriaMinWeightedAvgVals));

        List<Double> criteriaWeightsVals = evaluatorModel.getCriteriaModels().stream().map(CriteriaModel::getWeightedScore).collect(Collectors.toList());
        evaluatorModel.setFinalWeightedScore(MathFunction.sum(criteriaWeightsVals));

        setScoringStatus(evaluatorModel, scoringTemplate);

        String timeStamp = formatterDateTime.format(Calendar.getInstance().getTime());
        evaluatorModel.setTimeStamp(timeStamp);

        long millsec = System.currentTimeMillis();
        evaluatorModel.setIdentifier(identifierPrefix + millsec);

    }

    private int applyQualifierScores(CriteriaModel cm, EvaluatorModel evaluatorModel, Map<String, Qualifier> qualifierMap,
                                      Map<String, Map<String, Integer>> qualifierFixedScores, Map<String, Map<String, Range>> qualifierRangeScores) {
        int maxScoreExcludedValue = 0;
        for (QualifierModel qm : cm.getQualifiers()) {
            if (qm.getEvaluated() != null) {
                int score = qualifierFixedScores.get(qm.getName()).get(qm.getEvaluated());
                qm.setScoreValue(score);
                qm.setScoringType("fixed");
            }
            else {
                Range range = qualifierRangeScores.get(qm.getName()).get(qm.getScoreRange());
                qm.setScoreValue(range.getAssignedValue());
                qm.setScoringType("ranged");

            }

            if (Boolean.TRUE.equals(qualifierMap.get(qm.getName()).getModify_max_score()) &&
                    qualifierMap.get(qm.getName()).getMax_score_modify_value().containsKey(qm.getEvaluated())) {
                maxScoreExcludedValue += qualifierMap.get(qm.getName()).getMax_score_modify_value().get(qm.getEvaluated());
            }
            qm.setDescription(qualifierMap.get(qm.getName()).getDescription());
            if (evaluatorModel.isQualified() && !CollectionUtils.isEmpty(qualifierMap.get(qm.getName()).getDisqualifyOption())
                    && qualifierMap.get(qm.getName()).getDisqualifyOption().contains(qm.getEvaluated())) {
                evaluatorModel.setQualified(false);
            }
        }
        return maxScoreExcludedValue;
    }

    /**
     *
     * @param evaluatorModel Evaluator Model
     * @param scoringTemplate Scoring Template
     */
    private void setScoringStatus(EvaluatorModel evaluatorModel, ScoringTemplate scoringTemplate) {
        try {
            boolean isMinimumCriteriaPassed = true;
            for (CriteriaModel criteriaModel : evaluatorModel.getCriteriaModels()) {
                if (criteriaModel.getTotalScore() < criteriaModel.getMinScore()) {
                    isMinimumCriteriaPassed = false;
                    criteriaModel.setQualifiedMinCriteria(false);
                    evaluatorModel.setQualifiedMinCriteria(false);
                    if (!scoringTemplate.getStatus_on_min_criteria().isEmpty())
                        evaluatorModel.setScoreGrade(scoringTemplate.getStatus_on_min_criteria().getOrDefault(MIN_SCORE_FAIL_CONST, ""));
                }
            }
            if (isMinimumCriteriaPassed && !scoringTemplate.getScore_grades().isEmpty()) {
                for (HashMap<String, String> hm : scoringTemplate.getScore_grades()) {
                    Map.Entry<String, String> entry = hm.entrySet().iterator().next();
                    double min = Double.parseDouble(entry.getKey().split("-")[0]);
                    double max = Double.parseDouble(entry.getKey().split("-")[1]);
                    if (min < evaluatorModel.getFinalWeightedScore() && max > evaluatorModel.getFinalWeightedScore()) {
                        evaluatorModel.setScoreGrade(entry.getValue());
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Error occurred while setting the score status!");
            logger.error(ex.toString());
        }
    }

}
