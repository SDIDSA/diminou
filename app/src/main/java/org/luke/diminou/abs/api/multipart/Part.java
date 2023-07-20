package org.luke.diminou.abs.api.multipart;

import org.apache.hc.client5.http.entity.mime.ContentBody;

public abstract class Part {
	private final String key;
	
	protected Part(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	public abstract ContentBody getValue();
}
