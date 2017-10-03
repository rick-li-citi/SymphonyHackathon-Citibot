package org.symphonyoss.integration.webhook.citibot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.message.MessageMLVersion;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.parser.WebHookParser;
import org.symphonyoss.integration.webhook.parser.WebHookParserFactory;

@Component
public class CitiBotParserFactory implements WebHookParserFactory {
	private WebHookParser parser;
	
	@Autowired
	public CitiBotParserFactory(CitiBotSearchResultParser parser) {
		this.parser = parser;
	}

	@Override
	public boolean accept(MessageMLVersion version) {
		return MessageMLVersion.V2.equals(version);
	}

	@Override
	public void onConfigChange(IntegrationSettings settings) {
		// do nothing
	}

	@Override
	public WebHookParser getParser(WebHookPayload payload) {
		return this.parser;
	}

}
