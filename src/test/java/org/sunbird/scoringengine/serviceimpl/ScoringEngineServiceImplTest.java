package org.sunbird.scoringengine.serviceimpl;

import org.elasticsearch.rest.RestStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.sunbird.scoringengine.exception.BadRequestException;
import org.sunbird.scoringengine.models.CriteriaModel;
import org.sunbird.scoringengine.models.EvaluatorModel;
import org.sunbird.scoringengine.models.QualifierModel;
import org.sunbird.scoringengine.models.Response;
import org.sunbird.scoringengine.schema.model.Criteria;
import org.sunbird.scoringengine.schema.model.Qualifier;
import org.sunbird.scoringengine.schema.model.Range;
import org.sunbird.scoringengine.schema.model.ScoringSchema;
import org.sunbird.scoringengine.schema.model.ScoringTemplate;
import org.sunbird.scoringengine.util.IndexerService;
import org.sunbird.scoringengine.util.ScoringSchemaLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScoringEngineServiceImplTest {

    @Mock
    private ScoringSchemaLoader schemaLoader;

    @Mock
    private IndexerService indexerService;

    private ScoringEngineServiceImpl service;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ScoringEngineServiceImpl(schemaLoader, indexerService);
        ReflectionTestUtils.setField(service, "esIndex", "scorecard_index");
        ReflectionTestUtils.setField(service, "esIndexType", "resources");
        ReflectionTestUtils.setField(service, "scoringTemplateId", "temp-01");
        ReflectionTestUtils.setField(service, "esScoringEnabled", false);
    }

    private ScoringSchema buildScoringSchema() {
        Range range1020 = new Range();
        range1020.setMin(10);
        range1020.setMax(20);
        range1020.setAssignedValue(3);

        HashMap<String, Integer> fixedScores = new HashMap<>();
        fixedScores.put("Agree", 3);
        fixedScores.put("StronglyAgree", 5);

        HashMap<String, Range> rangeScores = new HashMap<>();
        rangeScores.put("10-20", range1020);

        Qualifier qualifier = new Qualifier();
        qualifier.setQualifier("qualifier1");
        qualifier.setFixed_score(fixedScores);
        qualifier.setRange_score(rangeScores);
        qualifier.setModify_max_score(false);
        qualifier.setMax_score_modify_value(new HashMap<>());
        qualifier.setDisqualifyOption(new ArrayList<>());

        Criteria criteria = new Criteria();
        criteria.setCriteria("criteria1");
        criteria.setMax_score(10.0);
        criteria.setMin_acceptable_score(4.0);
        criteria.setWeightage(0.5);
        criteria.setMin_score_weightage_enable(false);
        criteria.setQualifiers(Collections.singletonList(qualifier));

        ScoringTemplate template = new ScoringTemplate();
        template.setTemplate_id("temp-01");
        template.setTemplateName("Test Template");
        template.setRootOrg("rootOrg1");
        template.setOrg("org1");
        template.setCriteria(Collections.singletonList(criteria));
        template.setScore_grades(new ArrayList<>());
        template.setStatus_on_min_criteria(new HashMap<>());

        ScoringSchema schema = new ScoringSchema();
        schema.setScoringTemplates(Collections.singletonList(template));
        return schema;
    }

    private EvaluatorModel buildEvaluatorModel() {
        QualifierModel qualifierModel = new QualifierModel();
        qualifierModel.setName("qualifier1");
        qualifierModel.setEvaluated("Agree");

        CriteriaModel criteriaModel = new CriteriaModel();
        criteriaModel.setCriteria("criteria1");
        criteriaModel.setQualifiers(Collections.singletonList(qualifierModel));

        EvaluatorModel evaluatorModel = new EvaluatorModel();
        evaluatorModel.setTemplateId("temp-01");
        evaluatorModel.setResourceId("res1");
        evaluatorModel.setResourceType("course");
        evaluatorModel.setUserId("user1");
        evaluatorModel.setCriteriaModels(new ArrayList<>(Collections.singletonList(criteriaModel)));
        return evaluatorModel;
    }

    @Test
    public void addV3ComputesScoreAndReturnsResultWhenEsScoringDisabled() throws Exception {
        when(schemaLoader.getScoringSchema()).thenReturn(buildScoringSchema());

        Response response = service.addV3(buildEvaluatorModel());

        assertEquals("Successfully operation", response.get("Message"));
        assertNotNull(response.get("result"));
    }

    @Test
    public void addV3FallsBackToDefaultTemplateIdWhenNotProvided() throws Exception {
        when(schemaLoader.getScoringSchema()).thenReturn(buildScoringSchema());

        EvaluatorModel evaluatorModel = buildEvaluatorModel();
        evaluatorModel.setTemplateId("");

        Response response = service.addV3(evaluatorModel);

        assertEquals("temp-01", evaluatorModel.getTemplateId());
        assertEquals("Successfully operation", response.get("Message"));
    }

    @Test
    public void addV3FallsBackToDefaultTemplateIdWhenNull() throws Exception {
        when(schemaLoader.getScoringSchema()).thenReturn(buildScoringSchema());

        EvaluatorModel evaluatorModel = buildEvaluatorModel();
        evaluatorModel.setTemplateId(null);

        Response response = service.addV3(evaluatorModel);

        assertEquals("temp-01", evaluatorModel.getTemplateId());
        assertEquals("Successfully operation", response.get("Message"));
    }

    @Test
    public void addV3SkipsElasticsearchIndexingWhenGateCriteriaCheckTrueEvenIfEsScoringEnabled() throws Exception {
        ReflectionTestUtils.setField(service, "esScoringEnabled", true);
        when(schemaLoader.getScoringSchema()).thenReturn(buildScoringSchema());

        EvaluatorModel evaluatorModel = buildEvaluatorModel();
        evaluatorModel.setGateCriteriaCheck(true);

        Response response = service.addV3(evaluatorModel);

        assertEquals("Successfully operation", response.get("Message"));
        assertNotNull(response.get("result"));
    }

    @Test
    public void addV3IndexesIntoElasticsearchWhenEsScoringEnabledAndNotGateCriteriaCheck() throws Exception {
        ReflectionTestUtils.setField(service, "esScoringEnabled", true);
        when(schemaLoader.getScoringSchema()).thenReturn(buildScoringSchema());
        when(indexerService.addEntity(anyString(), anyString(), anyString(), anyMap())).thenReturn(RestStatus.OK);

        EvaluatorModel evaluatorModel = buildEvaluatorModel();
        evaluatorModel.setGateCriteriaCheck(false);

        Response response = service.addV3(evaluatorModel);

        assertEquals(RestStatus.OK, response.get("status"));
        assertNotNull(response.get("id"));
        verify(indexerService).addEntity(anyString(), anyString(), anyString(), anyMap());
    }

    @Test(expected = Exception.class)
    public void addV3ThrowsWhenTemplateNotFound() throws Exception {
        ScoringSchema emptySchema = new ScoringSchema();
        emptySchema.setScoringTemplates(new ArrayList<>());
        when(schemaLoader.getScoringSchema()).thenReturn(emptySchema);

        service.addV3(buildEvaluatorModel());
    }

    @Test(expected = Exception.class)
    public void searchV2ThrowsWhenUserIdAndResourceIdMissing() throws Exception {
        service.searchV2(new EvaluatorModel());
    }

    @Test
    public void searchV2ReturnsResourcesWhenUserIdProvided() throws Exception {
        EvaluatorModel evaluatorModel = new EvaluatorModel();
        evaluatorModel.setUserId("user1");
        when(indexerService.search(anyString(), anyMap())).thenReturn(null);

        Response response = service.searchV2(evaluatorModel);

        assertEquals("Successfully operation", response.get("Message"));
        verify(indexerService).search(anyString(), anyMap());
    }

    @Test
    public void searchV2ReturnsResourcesWhenOnlyUserIdEmptyButResourceIdProvided() throws Exception {
        EvaluatorModel evaluatorModel = new EvaluatorModel();
        evaluatorModel.setUserId("");
        evaluatorModel.setResourceId("res1");
        when(indexerService.search(anyString(), anyMap())).thenReturn(null);

        Response response = service.searchV2(evaluatorModel);

        assertEquals("Successfully operation", response.get("Message"));
        verify(indexerService).search(anyString(), anyMap());
    }

    @Test(expected = Exception.class)
    public void searchV2ThrowsWhenUserIdEmptyAndResourceIdNull() throws Exception {
        EvaluatorModel evaluatorModel = new EvaluatorModel();
        evaluatorModel.setUserId("");

        service.searchV2(evaluatorModel);
    }

    @Test
    public void searchV2ReturnsResourcesWhenResourceIdAndIdentifierProvided() throws Exception {
        EvaluatorModel evaluatorModel = new EvaluatorModel();
        evaluatorModel.setResourceId("res1");
        evaluatorModel.setIdentifier("lex_score_123");
        when(indexerService.search(anyString(), anyMap())).thenReturn(null);

        Response response = service.searchV2(evaluatorModel);

        assertEquals("Successfully operation", response.get("Message"));
        verify(indexerService).search(anyString(), anyMap());
    }

    @Test
    public void getTemplateReturnsTemplateWhenFound() throws Exception {
        when(schemaLoader.getScoringSchema()).thenReturn(buildScoringSchema());

        Response response = service.getTemplate("temp-01", "rootOrg1", "org1");

        assertEquals("Successfully operation", response.get("Message"));
        assertNotNull(response.get("result"));
    }

    @Test(expected = BadRequestException.class)
    public void getTemplateThrowsWhenTemplateIdMissing() throws Exception {
        service.getTemplate("", "rootOrg1", "org1");
    }

    @Test(expected = BadRequestException.class)
    public void getTemplateThrowsWhenTemplateNotFound() throws Exception {
        ScoringSchema emptySchema = new ScoringSchema();
        emptySchema.setScoringTemplates(new ArrayList<>());
        when(schemaLoader.getScoringSchema()).thenReturn(emptySchema);

        service.getTemplate("unknown-template", "rootOrg1", "org1");
    }
}
