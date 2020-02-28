package com.domosnap.simulationServer.controllermodules.heating;
/*
 * #%L
 * DomoSnap Legrand Simulation Gateway HeatingModule
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

import com.domosnap.engine.connector.impl.openwebnet.connector.OpenWebNetConstant;
import com.domosnap.engine.connector.impl.openwebnet.conversion.core.WhereType;
import com.domosnap.engine.connector.impl.openwebnet.conversion.core.dimension.DimensionValue;
import com.domosnap.engine.connector.impl.openwebnet.conversion.core.parser.CommandParser;
import com.domosnap.engine.connector.impl.openwebnet.conversion.core.parser.ParseException;
import com.domosnap.engine.connector.impl.openwebnet.conversion.heating.HeatingZoneConverter;
import com.domosnap.engine.controller.who.Who;
import com.domosnap.simulationServer.controllermodules.ControllerSimulator;
import com.domosnap.simulationServer.controllermodules.DimensionManager;
import com.domosnap.simulationServer.controllermodules.StatusManager;
import com.domosnap.simulationServer.controllermodules.UnknownDeviceException;

public class HeatingSimulator implements ControllerSimulator {

	
//	private static Hashtable<String, List<DimensionValue>> dimensionCache = new Hashtable<String, List<DimensionValue>>(); // where-dimension, dimensionList
	
	private static StatusManager statusList;
	private static DimensionManager dimensionListManager;

	public HeatingSimulator(StatusManager statusList, DimensionManager sm) {
		HeatingSimulator.statusList = statusList;
		dimensionListManager = sm;
	}
	
	public static void setStatusManager(DimensionManager dimensionListManager) {
		HeatingSimulator.dimensionListManager = dimensionListManager;
	}
	
	@Override
	public String execute(String command) {
		try {
			CommandParser parser = CommandParser.parse(command);

			if (WhereType.GENERAL == parser.getWhereType()) {
				// We send command to all correct address
				for (int i = 1; i < 99; i++) {
					try {
						if (i % 10 != 0) { // group address (20, 30, ..) are not correct
							updateController(""+i, parser);
						}
					} catch (UnknownDeviceException e) {
						System.out.println(e.getMessage());
					}
				}
			} else if (WhereType.GROUP == parser.getWhereType()) {
				// We send command to group address
				// TODO Not supported actually...
			} else /*if (WhereType.ENVIRONMENT == parser.getWhereType()) {// TODO actually heating adress 2 is a true adress and no an environnement adress... should be fix in parser... 
				String environment = parser.getEnvironment();
				// We send ambiance command to address
				for (int i = 1; i < 9; i++) {
					try {
						updateController(environment + i, parser);
					} catch (UnknownDeviceException e) {
						System.out.println(e.getMessage());
					}
				}
			} else */{
				// Command direct on a controller
				updateController(parser.getWhere(), parser);
			}
			
			return OpenWebNetConstant.ACK;

		} catch (ParseException e) {
			System.out.println("Command not supported [" + command + "]");
			return OpenWebNetConstant.NACK;
		} catch (UnknownDeviceException e) {
			System.out.println(e.getMessage());
			return OpenWebNetConstant.NACK;
		} catch (UnsupportedOperationException e) {
			System.out.println("Command not supported [" + command + "]");
			return OpenWebNetConstant.NACK;
		} catch (MissingResourceException e) {
			System.out.println("Device doesn't exist [" + command + "]");
			return OpenWebNetConstant.NACK;
		}
	}
	
	private void updateController(String where, CommandParser parser) throws UnknownDeviceException {
		
		String what = parser.getWhat();
		if (what != null) { // status
			if (!statusList.containsKey(where)) {
				throw new UnknownDeviceException(Who.HEATING_ADJUSTMENT, where, null);
			}
			
			if (HeatingZoneConverter.HeatingZoneStatus.HEATING_MODE.getCode().equals(what)
					|| HeatingZoneConverter.HeatingZoneStatus.HEATING_OFF.getCode().equals(what)
					|| HeatingZoneConverter.HeatingZoneStatus.THERMAL_PROTECTION.getCode().equals(what)
					) {
				statusList.put(where, what);
				
			} else {
				throw new UnsupportedOperationException("Command not supported [" + where + ":" + what +  "]");
			}
		} else { // dimension
			String dimension = parser.getDimension();
			List<DimensionValue> dimensionList = parser.getDimensionList();

			String id = where + "-" + dimension;
			if (!dimensionListManager.containsKey(id)) {
				throw new UnknownDeviceException(Who.HEATING_ADJUSTMENT, where, null);
			}
			
			if (HeatingZoneConverter.HeatingZoneDimension.MEASURE_TEMPERATURE.getCode().equals(dimension)
					|| HeatingZoneConverter.HeatingZoneDimension.SET_TEMPERATURE.getCode().equals(dimension)
					|| HeatingZoneConverter.HeatingZoneDimension.LOCAL_OFFSET.getCode().equals(dimension)
					) {
				dimensionListManager.put(id, dimensionList);
				
			} else {
				throw new UnsupportedOperationException("Command not supported [" + where + ":" + dimension +  "]");
			}
		}
	}

	@Override
	public List<String> status(String command) {
		List<String> result = new ArrayList<String>();
		try {
			CommandParser parser = CommandParser.parse(command);
			
			if (WhereType.GENERAL == parser.getWhereType()) {
				// We send command to all correct address
				for (int i = 1; i < 100; i++) {
					if (i % 10 != 0) { // group address (20, 30, ..) are not correct
						try {
							String status = updateStatus(""+i, parser);
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
			} else /* if (WhereType.ENVIRONMENT == parser.getWhereType()) { // TODO actually heating adress 2 is a true adress and no an environnement adress... should be fix in parser...
				String environment = parser.getEnvironment();
				// We send ambiance command to address
				for (int i = 1; i < 10; i++) {
					try {
						String status = updateStatus(environment+i, parser);
						if (status != null) {
							result.add(status);
						}
					} catch (UnknownDeviceException e) {
						System.out.println(e.getMessage());
					}
				}
			} else */ {
				// Command direct on a controller
				String status;
				try {
					status = updateStatus(parser.getWhere(), parser);
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
	
	private String updateStatus(String where, CommandParser parser) throws UnknownDeviceException {
		
		String dimension = parser.getDimension();
		if (dimension == null) { // status
			if (!statusList.containsKey(where)) {
				throw new UnknownDeviceException(Who.HEATING_ADJUSTMENT, where, null);
			}
			String what = statusList.get(where);
			return what == null ? null : MessageFormat.format(OpenWebNetConstant.COMMAND, new Object[] {getWho(), what, where} );
		} else { //dimension

			String id = where + "-" + dimension;
			if (!dimensionListManager.containsKey(id)) {
				throw new UnknownDeviceException(Who.HEATING_ADJUSTMENT, where, null);
			}
			
			if (HeatingZoneConverter.HeatingZoneDimension.MEASURE_TEMPERATURE.getCode().equals(dimension)
					|| HeatingZoneConverter.HeatingZoneDimension.SET_TEMPERATURE.getCode().equals(dimension)
					|| HeatingZoneConverter.HeatingZoneDimension.LOCAL_OFFSET.getCode().equals(dimension)
					) {
				List<DimensionValue> dimensionList = dimensionListManager.get(where + "-" + dimension);
				
				StringBuilder sb = new StringBuilder();
				for (DimensionValue dv : dimensionList) {
					sb.append(dv.getValue());
					sb.append(OpenWebNetConstant.DIMENSION_SEPARATOR);
				}
				sb.setLength(sb.length() - 1);
				return MessageFormat.format(OpenWebNetConstant.DIMENSION_COMMAND, new Object[] {
						getWho(), where, dimension, sb.toString() });
			} else {
				throw new UnsupportedOperationException("Command not supported [" + where + ":" + dimension +  "]");
			}
			
			
		}
	}

	@Override
	public String getWho() {
		return HeatingZoneConverter.OPEN_WEB_WHO;
	}	
}
