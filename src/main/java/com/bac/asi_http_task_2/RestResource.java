package com.bac.asi_http_task_2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/*
 * Class to generate a response to the GET request. 
 */
public class RestResource extends ServerResource {

	private final String sep = "/";

	private final String gitUrlFormat = "https://api.github.com/users/%s/repos?client_id=%s&client_secret=%s";

	private final String gitHubVersionParam = "application/vnd.github.v3+json";

	private final String userAgent = "srbaird";

	private final int limit = 5;

	private final String clientId = "<redacted>";
	private final String clientSecret = "<redacted>";

	private static final Logger log = Logger.getLogger(RestResource.class.getName());

	@Get
	public Representation represent() {

		log.info("Received GET request.");
		StringRepresentation resp = new StringRepresentation(getRepos(encodeRepo(parseReference())));
		resp.setMediaType(MediaType.APPLICATION_JSON);
		return resp;
	}

	/*
	 * Takes the first value on the request path as the parameter for the repo query
	 */
	private String parseReference() {

		String paramValue = getReference().getPath();
		if (paramValue.startsWith(sep)) {

			String content = paramValue.substring(1);
			String name = content.contains(sep) ? content.substring(0, content.indexOf(sep)) : content;
			return name;

		} else {
			return "";
		}
	}

	private String encodeRepo(String repoName) {

		log.info("Encode Repo for " + repoName);
		return String.format(gitUrlFormat, repoName, clientId, clientSecret);
	}

	private String getRepos(String url) {

		URLConnection connection = null;
		try {
			connection = new URL(url).openConnection();
			connection.setRequestProperty("Accept", gitHubVersionParam);
			connection.setRequestProperty("User-Agent", userAgent);
		} catch (IOException e) {

			return Json.createObjectBuilder().add("error", e.getMessage()).build().toString();
		}

		NavigableMap<Integer, String> sizeMap = new TreeMap<>();
		try (InputStream is = connection.getInputStream(); JsonReader rdr = Json.createReader(is)) {

			// Read response as a structure initially to trap Authentication
			// failures
			JsonStructure repoObject = rdr.read();

			if (repoObject.getValueType() == JsonValue.ValueType.OBJECT) {

				JsonObject repos = (JsonObject) repoObject;
				for (Entry<String, JsonValue> entry : repos.entrySet()) {

					log.info("Entry: " + entry.getKey() + ", Value: " + entry.getKey());
					log.info("Value: " + repos.getString(entry.getKey()));
				}
				return repos.toString();
			} else {
				JsonArray repos = (JsonArray) repoObject;

				for (JsonValue repo : repos) {

					JsonObject reportAsObject = (JsonObject) repo;
					sizeMap.put(reportAsObject.getInt("size"), reportAsObject.getString("name"));
				}
			}

		} catch (IOException | JsonException e) {

			return Json.createObjectBuilder().add("error", e.getMessage()).build().toString();
		}

		int count = 0;
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (Entry<Integer, String> entry : sizeMap.descendingMap().entrySet()) {
			if (++count > limit) {
				break;
			}
			arrayBuilder
					.add(Json.createObjectBuilder().add("name", entry.getValue()).add("size", entry.getKey()).build());

		}
		return arrayBuilder.build().toString();
	}
}
