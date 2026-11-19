package org.sunbird.scoringengine.models;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResponseTest {

    @Test
    public void idVerAndTsRoundTripThroughGettersAndSetters() {
        Response response = new Response();
        response.setId("id1");
        response.setVer("v1");
        response.setTs("ts1");

        assertEquals("id1", response.getId());
        assertEquals("v1", response.getVer());
        assertEquals("ts1", response.getTs());
    }

    @Test
    public void putAndGetStoreAndRetrieveValuesInResult() {
        Response response = new Response();
        response.put("key1", "value1");

        assertEquals("value1", response.get("key1"));
        assertTrue(response.containsKey("key1"));
        assertFalse(response.containsKey("missing"));
    }

    @Test
    public void putAllCopiesAllEntriesIntoResult() {
        Response response = new Response();
        Map<String, Object> values = new HashMap<>();
        values.put("a", 1);
        values.put("b", 2);

        response.putAll(values);

        assertEquals(1, response.get("a"));
        assertEquals(2, response.get("b"));
        assertEquals(2, response.getResult().size());
    }

    @Test
    public void cloneReturnsEqualButSeparateInstance() {
        Response response = new Response();
        response.setId("id1");

        Response cloned = response.clone(response);

        assertEquals(response.getId(), cloned.getId());
    }
}
