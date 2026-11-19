package org.sunbird.scoringengine.util;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ScoringSchemaLoaderTest {

    @Test
    public void loadReadsScoringSchemaJsonFromClasspath() throws Exception {
        ScoringSchemaLoader loader = new ScoringSchemaLoader();

        loader.load();

        assertNotNull(loader.getScoringSchema());
    }
}
