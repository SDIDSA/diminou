package org.luke.diminou.abs.api.json;

import androidx.annotation.NonNull;

public class Param {
	private final String key;
	private final Object value;

	public Param(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public String stringValue() {
		return String.valueOf(value);
	}

	@NonNull
	@Override
	public String toString() {
		return key + "=" + value;
	}
}
