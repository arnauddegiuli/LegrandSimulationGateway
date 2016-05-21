package com.homesnap.server.controllermodules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class StatusManager implements Map<String,String> {

	private static Hashtable<String, String> statusList = new Hashtable<String, String>(); // where, what
	
	public StatusManager(File file) {
		read(file);
	}
	
	public void read(File f) {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(f));
			for (String key : p.stringPropertyNames()) {
				statusList.put(key, p.getProperty(key));
			} 			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int size() {
		return statusList.size();
	}

	@Override
	public boolean isEmpty() {
		return statusList.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return statusList.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return statusList.containsValue(value);
	}

	@Override
	public String get(Object key) {
		return statusList.get(key);
	}

	@Override
	public String put(String key, String value) {
		return statusList.put(key, value);
	}

	@Override
	public String remove(Object key) {
		return statusList.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		statusList.putAll(m);
	}

	@Override
	public void clear() {
		statusList.clear();
	}

	@Override
	public Set<String> keySet() {
		return statusList.keySet();
	}

	@Override
	public Collection<String> values() {
		return statusList.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return statusList.entrySet();
	}
}
