package org.luke.diminou.abs.api;

import java.io.IOException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;

public abstract class ApiCall {
	protected static CloseableHttpClient client = HttpClients.createDefault();
	protected String path;

	protected ApiCall(String path) {
		this.path = path;
	}

	public abstract void execute(ObjectConsumer<JSONObject> onResult, String token) throws IOException, ParseException, JSONException;
}
