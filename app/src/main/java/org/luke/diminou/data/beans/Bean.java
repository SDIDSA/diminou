package org.luke.diminou.data.beans;

import org.apache.commons.text.CaseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class Bean {

	private static final HashMap<String, Bean> cache = new HashMap<>();
	private static final HashMap<String, Semaphore> fetchMutex = new HashMap<>();

	@SuppressWarnings("unchecked")
	protected static <T extends Bean> void getForId(Class<T> type ,int id, ObjectConsumer<T> onBean) {
		String key = type.getSimpleName() + "_" + id;

		Semaphore mutex = fetchMutex.get(key);
		if(mutex == null) {
			mutex = new Semaphore(1);
			fetchMutex.put(key, mutex);
		}
		mutex.acquireUninterruptibly();
		Bean found = cache.get(key);
		if(found == null) {
			Semaphore finalMutex = mutex;
			Session.getForId(type.getSimpleName(), id, res -> {
				Constructor<T> constructor = type.getDeclaredConstructor(JSONObject.class);
				Bean loaded = constructor.newInstance(res);
				cache.put(key, loaded);
				onBean.accept((T) loaded);
				finalMutex.release();
			});
		}else {
			try {
				onBean.accept((T) found);
				mutex.release();
			} catch (Exception e) {
				ErrorHandler.handle(e, "getting ["+type.getSimpleName()+"] for id=" + id);
			}
		}
	}
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
