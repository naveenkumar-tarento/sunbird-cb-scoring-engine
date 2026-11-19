

package org.sunbird.scoringengine.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.sunbird.scoringengine.schema.model.ScoringSchema;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ScoringSchemaLoader {


    private ScoringSchema scoringSchema;

    @PostConstruct
    public void load() throws IOException {

        ClassPathResource classPathResource = new ClassPathResource("ScoringSchema.json");

        try (InputStream inputStream = classPathResource.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            this.scoringSchema = objectMapper.readValue(inputStream, ScoringSchema.class);
        }
    }

    public ScoringSchema getScoringSchema() {
        return scoringSchema;
    }
}
