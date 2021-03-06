/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.webhook.citibot;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.client.services.MessageListener;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.message.MessageMLVersion;
import org.symphonyoss.integration.service.StreamService;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.parser.WebHookParser;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.pod.model.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.symphonyoss.integration.core.properties.IntegrationBridgeImplProperties.USER_POSTED_MESSAGE;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.ws.rs.core.MediaType;

/**
 * Implementation of a WebHook to integrate Rick to Symphony.
 *
 */
@Component
public class CitiWebHookIntegration extends WebHookIntegration implements MessageListener {

	@Autowired
	private CitiBotParserFactory parserFactory;

	private SymphonyClient symClient;

	@Autowired
	private StreamService streamService;

	private String integrationUser;
	
	private String cvCookies;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final String integrationUser) {
		super.onCreate(integrationUser);
		String keystorePassword = System.getProperty("keystore.password");
		InputStream inputStream = ClassLoader.class.getResourceAsStream("/cvCookie");
		try {
			cvCookies = IOUtils.toString(inputStream);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		this.integrationUser = integrationUser;
		try {
			symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC,
					"innovate.citibot@symphony.com", // bot email
					"/Users/kl68884/projects/symphony/App-Integrations-Universal/certs/innovate.citibot.p12", // bot
																												// cert
					keystorePassword, // bot cert/keystore pass
					"/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/lib/security/cacerts/", // truststore
																													// file
					"changeit"); // truststore password
			this.symClient.getMessageService().addMessageListener(this);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void onMessage(SymMessage message) {
		String searchKey = "search";
		String msgText = message.getMessageText();
		System.out.println(msgText);
		String streamId = message.getStreamId();
		Stream stream = new Stream();
		stream.setId(streamId);
		Message ackMessage = new Message();
		ackMessage.setMessage("<messageML>ACK, One sec.</messageML>");
		ackMessage.setData("{}");
		ackMessage.setVersion(MessageMLVersion.V2);
		
		try {
			postMessage(integrationUser, streamId, ackMessage);
		} catch (RemoteApiException e1) {
			e1.printStackTrace();
		}

		if (StringUtils.containsIgnoreCase(msgText, searchKey) && StringUtils.containsIgnoreCase(msgText, "citibot")
				|| StringUtils.containsIgnoreCase(msgText, "/search")) {
			String searchContent = StringUtils.substringAfterLast(msgText, searchKey).replaceAll("#", "").trim();
			System.out.println(searchContent);
			try {
				String responseStr = CitiWebHookIntegration.this.searchCVContent(searchContent);

				WebHookPayload payload = new WebHookPayload(null, null, responseStr);
				WebHookParser parser = parserFactory.getParser(payload);
				Message outputMsg = parser.parse(payload);
				ObjectMapper mapper = new ObjectMapper();
				JsonNode newData = mapper.readTree(outputMsg.getData());
				JsonNode searchMessageNode = ((ObjectNode) newData.get("citiSearchMessage")).put("data", responseStr);
				((ObjectNode) newData).put("citiSearchMessage", searchMessageNode);
				outputMsg.setData(newData.toString());

				Message sentMsg = postMessage(integrationUser, streamId, outputMsg);
				System.out.println(sentMsg.getData());

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (StringUtils.containsIgnoreCase(msgText, "/chart")) {
			String searchContent = StringUtils.substringAfterLast(msgText, "chart").replaceAll("#", "").trim();
			System.out.println(searchContent);
			stream = new Stream();
			stream.setId(streamId);
			Message chartMessage = new Message();
			chartMessage.setMessage("<messageML><div class=\"entity\" data-entity-id=\"citiChartMessage\">\n"
					+ "    </div></messageML>");
			chartMessage.setData(String.format(
					"{\"citiChartMessage\":{\"type\":\"com.symphony.integration.zapier.event.v2.chartMessage\",\"version\":\"1.0\", \"data\": { \"ticker\": \"%s\"}}}",
					searchContent));
			chartMessage.setVersion(MessageMLVersion.V2);
			Message sentMsg;
			try {
				sentMsg = postMessage(integrationUser, streamId, chartMessage);
				System.out.println(sentMsg.getData());
			} catch (RemoteApiException e) {
				e.printStackTrace();
			}

		} else if (StringUtils.containsIgnoreCase(msgText, "/list")) {
			stream = new Stream();
			stream.setId(streamId);
			Message listMessage = new Message();
			listMessage.setMessage("<messageML>"+
					"<ul>" +
					 "<li>1. /search</li>" +
					 "<li>2. /chart</li>" +
					 "</ul>" +
					 "</messageML>");
			listMessage.setData("{}");
			listMessage.setVersion(MessageMLVersion.V2);
			Message sentMsg;
			try {
				sentMsg = postMessage(integrationUser, streamId, listMessage);
				System.out.println(sentMsg.getData());
			} catch (RemoteApiException e) {
				e.printStackTrace();
			}
		}
	}

	public Message postMessage(String integrationUser, String stream, Message message) throws RemoteApiException {
		Message messageResponse = streamService.postMessage(integrationUser, stream, message);
		// LOGGER.info(logMessage.getMessage(USER_POSTED_MESSAGE, integrationUser,
		// stream));

		return messageResponse;
	}

	public String searchCVContent(String searchContent) throws Exception {
		@SuppressWarnings("deprecation")
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String searchContentB64 = Base64.getEncoder().encodeToString(searchContent.getBytes("utf-8"));
		String urlOverHttps = String.format(
				"https://uat.citivelocity.com/hubsearch2/json?q=%s&qencode_schema=base64&start=1&end=5&pubType=research,commentary&platformID=1&model=nonbetamodel-04282017&enrich-facet=Company,Author,Region,EMRegions",
				searchContentB64);

		HttpGet getMethod = new HttpGet(urlOverHttps);

		getMethod.setHeader("Cookie", cvCookies);
		HttpResponse response = httpClient.execute(getMethod);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		System.out.println(responseString);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(responseString);
		JsonNode results = actualObj.get("Results");
		if (results.isArray()) {
			for (final JsonNode objNode : results) {
				System.out.println(objNode.get("docTitle"));
			}
		}
		return responseString;
	}

	/**
	 * Parser method for the incoming Zapier payloads.
	 * 
	 * @param input
	 *            Incoming Zapier payload.
	 * @return The messageML resulting from the incoming payload parser.
	 * @throws WebHookParseException
	 *             when any exception occurs when parsing the payload.
	 */
	@Override
	public Message parse(WebHookPayload input) throws WebHookParseException {
		WebHookParser parser = parserFactory.getParser(input);
		return parser.parse(input);
	}

	/**
	 * @see WebHookIntegration#getSupportedContentTypes()
	 */
	@Override
	public List<MediaType> getSupportedContentTypes() {
		List<MediaType> supportedContentTypes = new ArrayList<>();
		supportedContentTypes.add(MediaType.WILDCARD_TYPE);
		return supportedContentTypes;
	}
}
