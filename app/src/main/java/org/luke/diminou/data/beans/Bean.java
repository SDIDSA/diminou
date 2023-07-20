package org.luke.diminou.data.beans;

import org.apache.commons.text.CaseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.layout.fragment.Fragment;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.app.pages.home.online.Home;
import org.luke.diminou.app.pages.home.online.friends.UserDisplay;
import org.luke.diminou.app.pages.home.online.global.HomeFragment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class Bean {

	private static final HashMap<String, Bean> cache = new HashMap<>();
	private static final HashMap<String, Semaphore> fetchMutex = new HashMap<>();

	private static void acquire(String key) {
		Semaphore mutex = fetchMutex.get(key);
		if(mutex == null) {
			mutex = new Semaphore(1);
			fetchMutex.put(key, mutex);
		}
		mutex.acquireUninterruptibly();
	}

	private static void release(String key) {
		Semaphore mutex = fetchMutex.get(key);
		if(mutex != null) mutex.release();
	}

	public static void clearCache() {
		Page.clearCache(Home.class);
		Fragment.clearCache(HomeFragment.class);
		UserDisplay.clearCache();
		cache.clear();
		fetchMutex.clear();
	}

	public static <T extends Bean> void refresh(Class<T> type, int id) {
		String key = type.getSimpleName() + "_" + id;
		Bean old = cache.get(key);
		Session.getForId(type.getSimpleName(), id, res -> {
			assert old != null;
			old.init(res);
		});
	}

	@SuppressWarnings("unchecked")
	protected static <T extends Bean> void getForId(Class<T> type ,int id, ObjectConsumer<T> onBean) {
		String key = type.getSimpleName() + "_" + id;

		Platform.runBack(() -> {
			acquire(key);
			Bean found = cache.get(key);
			if(found == null) {
				Session.getForId(type.getSimpleName(), id, res -> {
					Constructor<T> constructor = type.getDeclaredConstructor(JSONObject.class);
					constructor.setAccessible(true);
					T loaded = constructor.newInstance(res);
					constructor.setAccessible(false);
					cache.put(key, loaded);
					onBean.accept(loaded);
					release(key);
				});
			}else {
				Platform.runLater(() -> {
					try {
						onBean.accept((T) found);
					} catch (Exception e) {
						ErrorHandler.handle(e, "getting ["+type.getSimpleName()+"] for id=" + id);
					}
				});
				release(key);
			}
		});
	}

	protected static <T extends Bean> T getForIdSync(Class<T> type ,int id) {
		AtomicReference<T> res = new AtomicReference<>(null);

		getForId(type, id, res::set);

		while(res.get() == null) {
			Platform.sleep(10);
		}

		return res.get();
	}

	public static void refresh() {
		cache.values().forEach(bean -> {
			refresh(bean.getClass(), (Integer) bean.get("id"));
		});
	}

	protected void init(JSONObject obj) {
		Iterator<String> keys = obj.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			if (obj.isNull(key)) {
				continue;
			}
			try {
				Object value;
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
		}
	}

	public Object get(String key) {
		String setter = getterName(key);
		try {
			return getClass().getMethod(setter).invoke(this);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				 | SecurityException x) {
			ErrorHandler.handle(x, "getting " + key + " from " + getClass().getName());
		}
		return null;
	}

	private static String getterName(String fieldName) {
		return "get" + toCamelCaseMethod(fieldName);
	}

	private static String setterName(String fieldName) {
		return "set" + toCamelCaseMethod(fieldName);
	}

	private static String toCamelCaseMethod(String name) {
		return CaseUtils.toCamelCase(name, true, '_');
	}
}
