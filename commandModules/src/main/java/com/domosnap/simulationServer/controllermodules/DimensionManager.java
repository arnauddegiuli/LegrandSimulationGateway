package com.domosnap.simulationServer.controllermodules;

/*
 * #%L
 * DomoSnap Legrand Simulation Gateway Interfaces
 * %%
 * Copyright (C) 2011 - 2020 A. de Giuli
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.domosnap.engine.Log;
import com.domosnap.engine.Log.Session;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.core.dimension.DimensionValue;

public class DimensionManager implements Map<String,List<DimensionValue>> {

	private Hashtable<String, List<DimensionValue>> statusList = new Hashtable<String, List<DimensionValue>>(); // where-dimension, what
	private Log log = new Log(DimensionManager.class.getName());
	
	
	public DimensionManager(InputStream is) {
		read(is);
	}
	
	public void read(InputStream f) {
		Properties p = new Properties();
		try {
			p.load(f);
			for (String key : p.stringPropertyNames()) {
				List<DimensionValue> list = new ArrayList<DimensionValue>();
				DimensionValue d = new DimensionValue() {
					private String s;
					@Override
					public void setValue(String value) {
						s = value;
					}
					
					@Override
					public String getValue() {
						return s;
					}
				};
				d.setValue(p.getProperty(key)); // Actually only manage dimension with one value!!! => need to update to add more than one value
				list.add(d);
				statusList.put(key, list);
			} 			
		} catch (FileNotFoundException e) {
			log.severe(Session.Server, e.getMessage());
		} catch (IOException e) {
			log.severe(Session.Server, e.getMessage());
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
	public List<DimensionValue> get(Object key) {
		return statusList.get(key);
	}

	@Override
	public List<DimensionValue> put(String key, List<DimensionValue> value) {
		return statusList.put(key, value);
	}

	@Override
	public List<DimensionValue> remove(Object key) {
		return statusList.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends List<DimensionValue>> m) {
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
	public Collection<List<DimensionValue>> values() {
		return statusList.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, List<DimensionValue>>> entrySet() {
		return statusList.entrySet();
	}
}
