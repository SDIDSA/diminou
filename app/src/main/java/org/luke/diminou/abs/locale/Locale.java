package org.luke.diminou.abs.locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.Assets;
import org.luke.diminou.abs.utils.ErrorHandler;

import java.util.HashMap;
import java.util.Iterator;

public class Locale {
	private final static HashMap<String, Locale> cache = new HashMap<>();
	private final HashMap<String, String> values;

	private final String fontFamily;
	private final String name;

	public Locale(App owner, String name, String fontFamily) {
		this.fontFamily = fontFamily;
		this.name = name;
		cache.put(name.toLowerCase(), this);
		values = new HashMap<>();
		String file = Assets.readAsset(owner, "locales/".concat(name).concat(".json"));
		try {
			assert file != null;
			JSONObject obj = new JSONObject(file);

			Iterator<String> keys = obj.keys();
			while(keys.hasNext()) {
				String key = keys.next();
				values.put(key, obj.getString(key));
			}
		}catch(JSONException x) {
			ErrorHandler.handle(x, "reading locale".concat(name));
		}
	}

	public String get(String key) {
		if(key.length() == 0) return key;
		String found = values.get(key);

		if (found == null) {
			found = key;
			//ErrorHandler.handle(new RuntimeException("Missing Key From Locale " + name), "getting value of key [" + key + "] for locale " + name);
		}

		return found;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public static Locale forName(String name) {
		return cache.get(name.toLowerCase());
	}
}
