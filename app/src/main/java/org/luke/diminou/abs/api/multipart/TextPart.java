package org.luke.diminou.abs.api.multipart;

import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.ContentType;

public class TextPart extends Part {

	String value;
	
	public TextPart(String key, String value) {
		super(key);
		this.value = value;
	}

	public String getText() {
		return value;
	}

	@Override
	public ContentBody getValue() {
		return new StringBody(value, ContentType.MULTIPART_FORM_DATA);
	}

}
