package org.luke.diminou.abs.api.multipart;

import android.util.Log;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.api.ApiCall;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;


public class MultiPartApiCall extends ApiCall {
	private final Part[] parts;

	public MultiPartApiCall(String path, Part... parts) {
		super(path);
		this.parts = parts;
	}
	
	public void execute(ObjectConsumer<JSONObject> onResult, String token) throws IOException, ParseException, JSONException {
		HttpPost httpPost = new HttpPost(path);
		httpPost.addHeader("Accept", "application/multiform");
		
		if(token != null) {
			httpPost.addHeader("token", token);
		}

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.LEGACY);
		for (Part part : this.parts) {
			if(part instanceof TextPart) {
				builder.addTextBody(part.getKey(), ((TextPart) part).getText());
			}else if(part instanceof FilePart) {
				builder.addBinaryBody(part.getKey(), ((FilePart) part).getFile());
			}
		}

		httpPost.setEntity(builder.build());

		CloseableHttpResponse response = client.execute(httpPost);

		JSONObject res = new JSONObject(EntityUtils.toString(response.getEntity()));

		Platform.runLater(() -> {
			try {
				onResult.accept(res);
			} catch (Exception x) {
				ErrorHandler.handle(x, "handle response for API call to " + path);
			}
		});
	}
}
