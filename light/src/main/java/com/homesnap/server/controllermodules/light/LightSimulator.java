package com.homesnap.server.controllermodules.light;

import java.io.File;

/*
 * #%L
 * HomeSnap Legrand Simulation Gateway LightModule
 * %%
 * Copyright (C) 2011 - 2016 A. de Giuli
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


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import com.homesnap.engine.connector.openwebnet.OpenWebNetConstant;
import com.homesnap.engine.connector.openwebnet.WhereType;
import com.homesnap.engine.connector.openwebnet.light.LightStatusConverter;
import com.homesnap.engine.connector.openwebnet.parser.CommandParser;
import com.homesnap.engine.connector.openwebnet.parser.ParseException;
import com.homesnap.server.controllermodules.ControllerSimulator;
import com.homesnap.server.controllermodules.StatusManager;

public class LightSimulator implements ControllerSimulator {
	
	private static StatusManager statusList = new StatusManager(new File("light.properties")); // where, what
	
	@Override
	public String execute(String command) {
		try {
			CommandParser parser = CommandParser.parse(command);

			if (WhereType.GENERAL == parser.getWhereType()) {
				// We send command to all correct address
				for (int i = 11; i < 99; i++) {
					if (i % 10 != 0) { // group address (20, 30, ..) are not correct
						updateController(""+i, parser.getWhat());
					}
				}
			} else if (WhereType.GROUP == parser.getWhereType()) {
				// We send command to group address
				// Not supported actually...
			} else if (WhereType.ENVIRONMENT == parser.getWhereType()) {
				String environment = parser.getEnvironment();
				// We send ambiance command to address
				for (int i = 1; i < 9; i++) {
					updateController(environment + i, parser.getWhat());
				}
			} else {
				// Command direct on a controller
				updateController(parser.getWhere(), parser.getWhat());
			}
			
			return OpenWebNetConstant.ACK;

		} catch (ParseException e) {
			System.out.println("Command not supported [" + command + "]");
			return OpenWebNetConstant.NACK;
		} catch (UnsupportedOperationException e) {
			System.out.println("Command not supported [" + command + "]");
			return OpenWebNetConstant.NACK;
		} catch (MissingResourceException e) {
			System.out.println("Device doesn't exist [" + command + "]");
			return OpenWebNetConstant.NACK;
		}
	}
	
	private void updateController(String where, String what) {
		if (statusList.containsKey(where)) {
			throw new MissingResourceException("Device don't exist [" + where + ":" + what +  "]", what, where);
		}
		
		if (LightStatusConverter.LightStatus.LIGHT_OFF.getCode().equals(what)
				|| LightStatusConverter.LightStatus.LIGHT_ON.getCode().equals(what)) {
			statusList.put(where, what);
			
		} else {
			throw new UnsupportedOperationException("Command not supported [" + where + ":" + what +  "]");
		}
		
	}
	
	@Override
	public List<String> status(String command) {
		List<String> result = new ArrayList<String>();
		try {
			CommandParser parser = CommandParser.parse(command);
			String where = parser.getWhere();
			
			
			if (WhereType.GENERAL == parser.getWhereType()) {
				// We send command to all correct address
				for (int i = 11; i < 100; i++) {
					if (i % 10 != 0) { // group address (20, 30, ..) are not correct
						String status = updateStatus(""+i);
						if (status != null) {
							result.add(status);
						}
					}
				}
			} else if (WhereType.GROUP == parser.getWhereType()) {
				// We send command to group address
				// Not supported actually...
			} else if (WhereType.ENVIRONMENT == parser.getWhereType()) {
				String environment = parser.getEnvironment();
				// We send ambiance command to address
				for (int i = 1; i < 10; i++) {
					String status = updateStatus(environment+i);
					if (status != null) {
						result.add(status);
					}
				}
			} else {
				// Command direct on a controller
				String status = updateStatus(where);
				if (status != null) {
					result.add(status);
				}
			}

			result.add(OpenWebNetConstant.ACK);
		} catch (ParseException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
			result.add(OpenWebNetConstant.NACK);
		}
		return result;
	}
	
	private String updateStatus(String where) {
		String what = statusList.get(where);
		return what == null ? null : MessageFormat.format(OpenWebNetConstant.COMMAND, new Object[] {getWho(), what, where} );
	}

	@Override
	public String getWho() {
		return new LightStatusConverter().getOpenWebWho();
	}
}
