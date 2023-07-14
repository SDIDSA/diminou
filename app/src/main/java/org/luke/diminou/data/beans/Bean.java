package org.luke.diminou.data.beans;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.apache.commons.text.CaseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.utils.ErrorHandler;

public class Bean {
	protected void init(JSONObject obj) {
		Iterator<String> keys = obj.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			if (obj.isNull(key)) {
				continue;
			}
			try {
				Object value = null;
				value = obj.get(key);
				set(key, value);
			} catch (JSONException e) {
				ErrorHandler.handle(e, "init bean " + getClass().getName());
			}
		}
	}

	public void set(String key, Object value) {
		String setter = setterName(key);
		Log.i(key, value.toString());
		try {
			getClass().getMethod(setter, value.getClass()).invoke(this, value);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException x) {
			ErrorHandler.handle(x, "parse " + key + " of type " + value.getClass().getSimpleName());
			x.printStackTrace();
		}
	}

	private static String setterName(String fieldName) {
		return "set" + toCamelCaseMethod(fieldName);
	}

	private static String toCamelCaseMethod(String name) {
		return CaseUtils.toCamelCase(name, true, '_');
	}
}
