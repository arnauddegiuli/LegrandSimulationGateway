package com.domosnap.simulationServer.controllermodules.light;

/*
 * #%L
 * DomoSnap Legrand Simulation Gateway LightModule
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


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import com.domosnap.engine.adapter.impl.openwebnet.connector.OpenWebNetConstant;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.core.WhereType;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.core.parser.CommandParser;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.core.parser.ParseException;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.light.LightStatusConverter;
import com.domosnap.engine.controller.light.Light;
import com.domosnap.simulationServer.controllermodules.ControllerSimulator;
import com.domosnap.simulationServer.controllermodules.StatusManager;
import com.domosnap.simulationServer.controllermodules.UnknownDeviceException;

public class LightSimulator implements ControllerSimulator {
	
	private static StatusManager statusList;

	public LightSimulator(StatusManager sm) {
		statusList = sm;
	}
	
	public static void setStatusManager(StatusManager statusList) {
		LightSimulator.statusList = statusList;
	}
	
	@Override
	public String execute(String command) {
		try {
			CommandParser parser = CommandParser.parse(command);

			if (WhereType.GENERAL == parser.getWhereType()) {
				// We send command to all correct address
				for (int i = 11; i < 99; i++) {
					try {
						if (i % 10 != 0) { // group address (20, 30, ..) are not correct
							updateController(""+i, parser.getWhat());
						}
					} catch (UnknownDeviceException e) {
						System.out.println(e.getMessage());
					}
				}
			} else if (WhereType.GROUP == parser.getWhereType()) {
				// We send command to group address
				// TODO Not supported actually...
			} else if (WhereType.ENVIRONMENT == parser.getWhereType()) {
				String environment = parser.getEnvironment();
				// We send ambiance command to address
				for (int i = 1; i < 9; i++) {
					try {
						updateController(environment + i, parser.getWhat());
					} catch (UnknownDeviceException e) {
						System.out.println(e.getMessage());
					}
				}
			} else {
				try {
					// Command direct on a controller
					updateController(parser.getWhere(), parser.getWhat());
				} catch (UnknownDeviceException e) {
					System.out.println(e.getMessage());
					return OpenWebNetConstant.NACK;
				}
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
	
	private void updateController(String where, String what) throws UnknownDeviceException {
		if (!statusList.containsKey(where)) {
			throw new UnknownDeviceException(Light.class, where, null);
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
						try {
							String status = updateStatus(""+i);
							if (status != null) {
								result.add(status);
							}
						} catch (UnknownDeviceException e) {
							System.out.println(e.getMessage());
						}

					}
				}
			} else if (WhereType.GROUP == parser.getWhereType()) {
				// We send command to group address
				// TODO Not supported actually...
			} else if (WhereType.ENVIRONMENT == parser.getWhereType()) {
				String environment = parser.getEnvironment();
				// We send ambiance command to address
				for (int i = 1; i < 10; i++) {
					try {
						String status = updateStatus(environment+i);
						if (status != null) {
							result.add(status);
						}
					} catch (UnknownDeviceException e) {
						System.out.println(e.getMessage());
					}
				}
			} else {
				// Command direct on a controller
				String status;
				try {
					status = updateStatus(where);
					if (status != null) {
						result.add(status);
					}
				} catch (UnknownDeviceException e) {
					System.out.println(e.getMessage());
					result.add(OpenWebNetConstant.NACK);
					return result;
				}

			}

			result.add(OpenWebNetConstant.ACK);
		} catch (ParseException e) {
			System.out.println("Unexpected error during parsing command ["+ command +"] (probably unsupported command or feature from the command)");
			result.add(OpenWebNetConstant.NACK);
		}
		return result;
	}
	
	private String updateStatus(String where) throws UnknownDeviceException {
		if (!statusList.containsKey(where)) {
			throw new UnknownDeviceException(Light.class, where, null);
		}
		String what = statusList.get(where);
		return what == null ? null : MessageFormat.format(OpenWebNetConstant.COMMAND, new Object[] {getWho(), what, where} );
	}

	@Override
	public String getWho() {
		return new LightStatusConverter().getOpenWebWho();
	}
}
