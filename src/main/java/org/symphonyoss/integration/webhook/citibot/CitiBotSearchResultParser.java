package org.symphonyoss.integration.webhook.citibot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.parser.WebHookParser;
import org.symphonyoss.integration.webhook.parser.metadata.MetadataParser;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class CitiBotSearchResultParser extends MetadataParser implements WebHookParser {
	private static final String TEMPLATE_FILE = "templateSearchMessage.xml";

	private static final String METADATA_FILE = "metadataSearchMessage.xml";

	@Override
	public List<String> getEvents() {
		return Arrays.asList("search_results");
	}

	@Override
	public Message parse(WebHookPayload payload) throws WebHookParseException {
		try {
			JsonNode rootNode = JsonUtils.readTree(payload.getBody());
			return parse(rootNode);
		} catch (IOException e) {
			throw new CitibotParserException("Something went wrong while trying to validate a message from Citibot", e);
		}
	}

	/**
	 * Replace the '\n' to <br/>
	 * tag on the message content.
	 * 
	 * @param input
	 *            JSON input payload
	 */
	@Override
	protected void preProcessInputData(JsonNode input) {
		
	}

	@Override
	protected String getTemplateFile() {
		return TEMPLATE_FILE;
	}

	@Override
	protected String getMetadataFile() {
		return METADATA_FILE;
	}

}

