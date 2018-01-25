package com.domosnap.simulationServer.controllermodules;

/*
 * #%L
 * DomoSnap Legrand Simulation Gateway Interfaces
 * %%
 * Copyright (C) 2011 - 2018 A. de Giuli
 * %%
 * This file is part of MyDomo done by A. de Giuli (arnaud.degiuli(at)free.fr).
 * 
 *     MyDomo is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     MyDomo is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with MyDomo.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class StatusManager implements Map<String,String> {

	private Hashtable<String, String> statusList = new Hashtable<String, String>(); // where, what
	
	public StatusManager(InputStream is) {
		read(is);
	}
	
	public void read(InputStream f) {
		Properties p = new Properties();
		try {
			p.load(f);
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
