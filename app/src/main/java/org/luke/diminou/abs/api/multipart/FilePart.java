package org.luke.diminou.abs.api.multipart;

import java.io.File;

import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.core5.http.ContentType;

public class FilePart extends Part {

	private final File value;
	
	public FilePart(String key, File value) {
		super(key);
		this.value = value;
	}

	public File getFile() {
		return value;
	}

	@Override
	public ContentBody getValue() {
		return new FileBody(value, ContentType.DEFAULT_BINARY);
	}

}
