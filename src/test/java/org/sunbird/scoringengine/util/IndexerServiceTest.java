package org.sunbird.scoringengine.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexerServiceTest {

    @Mock
    private RestHighLevelClient esClient;

    private IndexerService indexerService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        indexerService = new IndexerService();
        ReflectionTestUtils.setField(indexerService, "esClient", esClient);
    }

    private IndexerService newServiceForInit(boolean esAuthEnabled) {
        IndexerService service = new IndexerService();
        ReflectionTestUtils.setField(service, "esHost", "localhost");
        ReflectionTestUtils.setField(service, "esPort", "9200");
        ReflectionTestUtils.setField(service, "esUsername", "user");
        ReflectionTestUtils.setField(service, "esPassword", "pass");
        ReflectionTestUtils.setField(service, "esAuthEnabled", esAuthEnabled);
        return service;
    }

    @Test
    public void initCreatesClientWhenAuthDisabled() {
        IndexerService service = newServiceForInit(false);

        service.init();

        assertNotNull(ReflectionTestUtils.getField(service, "esClient"));
    }

    @Test
    public void initCreatesClientWhenAuthEnabled() {
        IndexerService service = newServiceForInit(true);

        service.init();

        assertNotNull(ReflectionTestUtils.getField(service, "esClient"));
    }

    @Test
    public void addEntityReturnsStatusOnSuccess() throws IOException {
        IndexResponse indexResponse = mock(IndexResponse.class);
        when(indexResponse.status()).thenReturn(RestStatus.CREATED);
        when(esClient.index(any(IndexRequest.class), any(RequestOptions.class))).thenReturn(indexResponse);

        RestStatus status = indexerService.addEntity("index1", "type1", "entity1", new HashMap<>());

        assertEquals(RestStatus.CREATED, status);
    }

    @Test
    public void addEntityReturnsNullOnIOException() throws IOException {
        when(esClient.index(any(IndexRequest.class), any(RequestOptions.class))).thenThrow(new IOException("boom"));

        RestStatus status = indexerService.addEntity("index1", "type1", "entity1", new HashMap<>());

        assertNull(status);
    }

    @Test
    public void updateEntityReturnsStatusOnSuccess() throws IOException {
        UpdateResponse updateResponse = mock(UpdateResponse.class);
        when(updateResponse.status()).thenReturn(RestStatus.OK);
        when(esClient.update(any(UpdateRequest.class), any(RequestOptions.class))).thenReturn(updateResponse);

        RestStatus status = indexerService.updateEntity("index1", "type1", "entity1", new HashMap<>());

        assertEquals(RestStatus.OK, status);
    }

    @Test
    public void updateEntityReturnsNullOnIOException() throws IOException {
        when(esClient.update(any(UpdateRequest.class), any(RequestOptions.class))).thenThrow(new IOException("boom"));

        RestStatus status = indexerService.updateEntity("index1", "type1", "entity1", new HashMap<>());

        assertNull(status);
    }

    @Test
    public void readEntityReturnsSourceMap() throws IOException {
        GetResponse getResponse = mock(GetResponse.class);
        Map<String, Object> source = new HashMap<>();
        source.put("field1", "value1");
        when(getResponse.getSourceAsMap()).thenReturn(source);
        when(esClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);

        Map<String, Object> result = indexerService.readEntity("index1", "type1", "entity1");

        assertEquals(source, result);
    }

    @Test
    public void searchReturnsResultsAndAppliesLatestRecordSort() throws IOException {
        SearchHit hit = mock(SearchHit.class);
        when(hit.getSourceAsString()).thenReturn("{\"field1\":\"value1\"}");
        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.iterator()).thenReturn(Arrays.asList(hit).iterator());
        SearchResponse searchResponse = mock(SearchResponse.class);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(esClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);

        Map<String, Object> searchQuery = new HashMap<>();
        searchQuery.put("isGetLatestRecordEnabled", true);
        searchQuery.put("field1", "value1");
        searchQuery.put("field2", "");
        searchQuery.put("field3", null);

        JsonNode result = indexerService.search("index1", searchQuery);

        assertEquals(1, result.size());
        assertEquals("value1", result.get(0).get("field1").asText());
    }

    @Test
    public void searchReturnsResultsWithoutLatestRecordSort() throws IOException {
        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.iterator()).thenReturn(Arrays.<SearchHit>asList().iterator());
        SearchResponse searchResponse = mock(SearchResponse.class);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(esClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);

        Map<String, Object> searchQuery = new HashMap<>();
        searchQuery.put("isGetLatestRecordEnabled", false);

        JsonNode result = indexerService.search("index1", searchQuery);

        assertEquals(0, result.size());
    }
}
