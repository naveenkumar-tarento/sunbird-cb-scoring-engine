package org.sunbird.scoringengine.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.sunbird.scoringengine.models.EvaluatorModel;
import org.sunbird.scoringengine.models.Response;
import org.sunbird.scoringengine.service.ScoringEngineService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScoringControllerTest {

    @Mock
    private ScoringEngineService scoringEngineService;

    private ScoringController scoringController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        scoringController = new ScoringController(scoringEngineService);
    }

    @Test
    public void addSetsRootOrgAndOrgThenDelegatesToService() throws Exception {
        EvaluatorModel evaluatorModel = new EvaluatorModel();
        Response serviceResponse = new Response();
        when(scoringEngineService.addV3(evaluatorModel)).thenReturn(serviceResponse);

        ResponseEntity<Response> result = scoringController.add(evaluatorModel, "rootOrg1", "org1");

        assertEquals("rootOrg1", evaluatorModel.getRootOrg());
        assertEquals("org1", evaluatorModel.getOrg());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(serviceResponse, result.getBody());
    }

    @Test
    public void searchSetsRootOrgAndOrgThenDelegatesToService() throws Exception {
        EvaluatorModel evaluatorModel = new EvaluatorModel();
        Response serviceResponse = new Response();
        when(scoringEngineService.searchV2(evaluatorModel)).thenReturn(serviceResponse);

        ResponseEntity<Response> result = scoringController.search(evaluatorModel, "rootOrg1", "org1");

        assertEquals("rootOrg1", evaluatorModel.getRootOrg());
        assertEquals("org1", evaluatorModel.getOrg());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(serviceResponse, result.getBody());
    }

    @Test
    public void getTemplateConfigurationDelegatesToServiceWithPathVariable() throws Exception {
        Response serviceResponse = new Response();
        when(scoringEngineService.getTemplate("template1", "rootOrg1", "org1")).thenReturn(serviceResponse);

        ResponseEntity<Response> result = scoringController.getTemplateConfiguration("rootOrg1", "org1", "template1");

        verify(scoringEngineService).getTemplate("template1", "rootOrg1", "org1");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(serviceResponse, result.getBody());
    }
}
