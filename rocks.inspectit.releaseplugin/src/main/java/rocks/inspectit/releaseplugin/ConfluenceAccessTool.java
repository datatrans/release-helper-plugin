package rocks.inspectit.releaseplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Type encapsulating the REST-API based access to a confluence Server. Allows
 * searching for pages and creating new pages at the moment.
 * 
 * @author Jonas Kunz
 *
 */

public class ConfluenceAccessTool {

	/**
	 * the HTTP connection supporting JSON in and output.
	 */
	private JsonHTTPClientWrapper client;

	/**
	 * Initializes a new connection with the given connection information.
	 * @param url the url of confluence
	 * @param user the username
	 * @param password the password
	 * @param proxy the proxy to use for building the connection
	 */
	public ConfluenceAccessTool(String url, String user, String password, String proxy) {
		super();
		client = new JsonHTTPClientWrapper(url, user, password, proxy);
	}



	/**
	 * closes the connection.
	 */
	public void destroy() {
		client.destroy();
	}


	/**
	 * 
	 * Queries all pages wioth the given title in the given space.
	 * 
	 * @param title
	 *            the title of the pages to look for
	 * @param space
	 *            the key of the space
	 * @return a list of all IDs of the pages having the given title in the
	 *         given space
	 */
	public List<Long> getPageIDByTitle(String title, String space) {
		try {
			
			Map<String, String> params = new HashMap<>();
			params.put("spaceKey", space);
			params.put("title", title);
			
			JsonElement je = client.getJson("/rest/api/content", params);
			
			JsonArray resultsArray = je.getAsJsonObject().get("results").getAsJsonArray();

			List<Long> results = new ArrayList<Long>();

			for (int i = 0; i < resultsArray.size(); i++) {
				results.add(resultsArray.get(i).getAsJsonObject().get("id")
						.getAsLong());
			}

			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * Creates a new page in the given space.
	 * 
	 * @param title
	 *            the title of the new page
	 * @param htmlContent
	 *            the html content of the new page
	 * @param space
	 *            the key of the space where the new page shall be placed
	 * @param parentPageID
	 *            the id of the parent page, under which the new page should be
	 *            inserted as child. If parentPageID is null, the new page will
	 *            be placed at the spaces root.
	 */
	public void createPage(String title, String htmlContent, String space, Long parentPageID) {

		JsonObject page = new JsonObject();
		page.addProperty("type", "page");
		page.addProperty("title", title);
		if (parentPageID != null) {
			JsonObject parentPage = new JsonObject();
			parentPage.addProperty("type", "page");
			parentPage.addProperty("id", parentPageID);
			
			JsonArray ancestors = new JsonArray();
			ancestors.add(parentPage);
			page.add("ancestors", ancestors);
			
		}
		JsonObject spaceObj = new JsonObject();
		spaceObj.addProperty("key", space);
		page.add("space", spaceObj);
		
		JsonObject body = new JsonObject();
		JsonObject storage = new JsonObject();
		storage.addProperty("value", htmlContent);
		storage.addProperty("representation", "storage");
		body.add("storage", storage);
		
		page.add("body", body);
		
		client.postJson("/rest/api/content", page);

	}

}
