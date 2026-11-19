
package org.sunbird.scoringengine.serviceimpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.sunbird.scoringengine.exception.BadRequestException;
import org.sunbird.scoringengine.models.EvaluatorModel;
import org.sunbird.scoringengine.models.PropertyFilterMixIn;
import org.sunbird.scoringengine.models.Response;
import org.sunbird.scoringengine.schema.model.ScoringTemplate;
import org.sunbird.scoringengine.service.ScoringEngineService;
import org.sunbird.scoringengine.util.ComputeScores;
import org.sunbird.scoringengine.util.IndexerService;
import org.sunbird.scoringengine.util.ScoringSchemaLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ScoringEngineServiceImpl implements ScoringEngineService {


	private final ScoringSchemaLoader schemaLoader;

	private final IndexerService indexerService;

	public ScoringEngineServiceImpl(ScoringSchemaLoader schemaLoader, IndexerService indexerService) {
		this.schemaLoader = schemaLoader;
		this.indexerService = indexerService;
	}

	private ObjectMapper mapper = new ObjectMapper();
	@Value("${es.score.index}")
	private String esIndex;

	@Value("${es.score.index.type}")
	private String esIndexType;

	@Value("${scoring.template.id}")
	private String scoringTemplateId;

	@Value("${es.scoring.enabled}")
	private boolean esScoringEnabled;

	private static final String[] ignorableFields = {"rootOrg", "org", "version", "weightage", "max_score", "min_acceptable_score",
			"fixed_score", "range_score", "modify_max_score", "10-20", "20-30", "30-40", "modify_max_score", "max_score_modify_value",
			"min_score_weightage_enable", "min_score_weightage", "score_grades", "isQualifiedMinCriteria", "status_on_min_criteria", "disqualifyOption"};

	private static final String MESSAGE_KEY = "Message";
	private static final String SUCCESS_MESSAGE = "Successfully operation";

	@Override
	public Response addV3(EvaluatorModel evaluatorModel) throws Exception {
		Response response = new Response();
		try {
			String templateId = (evaluatorModel.getTemplateId() == null || evaluatorModel.getTemplateId().isEmpty()) ? scoringTemplateId
			: evaluatorModel.getTemplateId();

			Optional<ScoringTemplate> scoringTemplateOptional = schemaLoader.getScoringSchema().getScoringTemplates().stream().filter(t -> t.getTemplate_id().equals(templateId)).findFirst();

			if(!scoringTemplateOptional.isPresent())
				throw new BadRequestException("Template is not found on given template id");
			ScoringTemplate scoringTemplate = scoringTemplateOptional.get();
			
			ComputeScores computeScores = new ComputeScores(scoringTemplate);
			computeScores.computeV2(evaluatorModel);
			// post the data into ES index
			if (esScoringEnabled && !evaluatorModel.isGateCriteriaCheck()) {
				Map<String, Object> indexDocument = mapper.convertValue(evaluatorModel, new TypeReference<Map<String, Object>>() {
				});
				RestStatus status = indexerService.addEntity(esIndex, esIndexType, evaluatorModel.getIdentifier(), indexDocument);
				response.put("status", status);
				response.put("id", evaluatorModel.getIdentifier());

			} else {
				response.put("result", evaluatorModel);
			}

			response.put(MESSAGE_KEY, SUCCESS_MESSAGE);

		} catch (Exception e) {
			throw new Exception(e);
		}

		return response;
	}

	@Override
	public Response searchV2(EvaluatorModel evaluatorModel) throws Exception{
		Response response = new Response();
		try{
			if ((null == evaluatorModel.getUserId() || evaluatorModel.getUserId().isEmpty()) && (null == evaluatorModel.getResourceId() || evaluatorModel.getResourceId().isEmpty())) {
				throw new BadRequestException("Required fields, userId or resourceId is not valid ");
			}
			// post the data into ES index
			Map<String, Object> searchQuery = new HashMap<>();
			searchQuery.put("userId", evaluatorModel.getUserId());
			searchQuery.put("resourceId", evaluatorModel.getResourceId());
			searchQuery.put("isGetLatestRecordEnabled", evaluatorModel.isGetLatestRecordEnabled());
            if(!StringUtils.isEmpty(evaluatorModel.getIdentifier())){
				searchQuery.put("identifier", evaluatorModel.getIdentifier());
			}
			JsonNode searchResponse= indexerService.search(esIndex, searchQuery);

			response.put("resources", searchResponse);
			response.put(MESSAGE_KEY, SUCCESS_MESSAGE);

		} catch (Exception e){
			throw new Exception(e);
		}

		return response;
	}

	@Override
	public Response getTemplate(String templateId, String rootOrg, String org) throws Exception {
		Response response = new Response();
		if (StringUtils.isEmpty(templateId)) {
			throw new BadRequestException("Template Id is required!");
		}
		Optional<ScoringTemplate> scoringTemplateOptional = schemaLoader.getScoringSchema().getScoringTemplates().stream().filter(t -> t.getTemplate_id().equals(templateId)).findFirst();
		if(!scoringTemplateOptional.isPresent())
			throw new BadRequestException("Template is not found on given template id");
		ScoringTemplate scoringTemplate = scoringTemplateOptional.get();
		ObjectMapper templateMapper = new ObjectMapper();
		templateMapper.addMixIn(Object.class, PropertyFilterMixIn.class);
		FilterProvider filters = new SimpleFilterProvider().addFilter("filter properties by name",SimpleBeanPropertyFilter.serializeAllExcept(ignorableFields));
		ObjectWriter writer = templateMapper.writer(filters);
		response.put("result", templateMapper.readValue(writer.writeValueAsString(scoringTemplate), Object.class));
		response.put(MESSAGE_KEY, SUCCESS_MESSAGE);
		response.put("resources", HttpStatus.OK);
		return response;
	}

}
