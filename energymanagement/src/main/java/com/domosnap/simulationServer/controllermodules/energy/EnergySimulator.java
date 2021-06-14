package com.domosnap.simulationServer.controllermodules.energy;
/*
 * #%L
 * DomoSnap Legrand Simulation Gateway Energy Management Module
 * %%
 * Copyright (C) 2011 - 2021 A. de Giuli
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
import java.util.Random;

import com.domosnap.engine.adapter.impl.openwebnet.connector.OpenWebNetConstant;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.core.dimension.DimensionValue;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.core.parser.CommandParser;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.core.parser.ParseException;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.counter.CounterConverter;
import com.domosnap.engine.controller.counter.PowerCounter;
import com.domosnap.simulationServer.controllermodules.ControllerSimulator;
import com.domosnap.simulationServer.controllermodules.DimensionManager;
import com.domosnap.simulationServer.controllermodules.StatusManager;
import com.domosnap.simulationServer.controllermodules.UnknownDeviceException;

public class EnergySimulator implements ControllerSimulator {

	private static StatusManager statusList;
	private static DimensionManager dimensionListManager;

	public EnergySimulator(StatusManager statusList, DimensionManager sm) {
		EnergySimulator.statusList = statusList;
		dimensionListManager = sm;
	}
	
	public static void setStatusManager(DimensionManager dimensionListManager) {
		EnergySimulator.dimensionListManager = dimensionListManager;
	}
	
	@Override
	public String execute(String command) {
		try {
			CommandParser parser = CommandParser.parse(command);

			// No adress group and environment nore general TODO = manange in parser
			// Command direct on a controller
			updateController(parser.getWhere(), parser);
			
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
				throw new UnknownDeviceException(PowerCounter.class, where, null);
			}
			
			// TODO 57
			// TODO 58
			// TODO 59
			// TODO 510
			switch (what) {
//			case "57": {
//				
//				yield type;
//			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + what);
			}

			
//			throw new UnsupportedOperationException("Command not supported [" + where + ":" + what +  "]");
		} else { // dimension
			String dimension = parser.getDimension();
//			List<DimensionValue> dimensionList = parser.getDimensionList();
//
//			String id = where + "-" + dimension;
//			if (!dimensionListManager.containsKey(id)) {
//				throw new UnknownDeviceException(PowerCounter.class, where, null);
//			}
//			
//			if (HeatingZoneConverter.HeatingZoneDimension.MEASURE_TEMPERATURE.getCode().equals(dimension)
//					|| HeatingZoneConverter.HeatingZoneDimension.SET_TEMPERATURE.getCode().equals(dimension)
//					|| HeatingZoneConverter.HeatingZoneDimension.LOCAL_OFFSET.getCode().equals(dimension)
//					) {
//				dimensionListManager.put(id, dimensionList);
//				
//			} else {
				throw new UnsupportedOperationException("Command not supported [" + where + ":" + dimension +  "]");
//			}
		}
	}

	@Override
	public List<String> status(String command) {
		List<String> result = new ArrayList<String>();
		try {
			CommandParser parser = CommandParser.parse(command);
			
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
				throw new UnknownDeviceException(PowerCounter.class, where, null);
			}
			String what = statusList.get(where);
			return what == null ? null : MessageFormat.format(OpenWebNetConstant.COMMAND, new Object[] {getWho(), what, where} );
		} else { //dimension

			String id = where + "-" + dimension;
			if (!dimensionListManager.containsKey(id)) {
				throw new UnknownDeviceException(PowerCounter.class, where, null);
			}
			
			
			// TODO chapter 5.2.15 Start senting instantaneous consumption 1200
			// TODO chapter 5.2.16 Stop senting instantaneous consumption 1200
			// TODO chapter 5.2.17 Request the  daily totalizers on an hourl basis 511
			// TODO 512
			// TODO 513
			// TODO 514
			// TODO chapter 5.2.20 Request energy totalizer per month 52
			// TODO chapter 5.2.24 Request totalizers 72
			// 
			
			
			// TODO chapter 5.2.23
			
			if (CounterConverter.CounterDimension.ACTIVE_POWER.getCode().equals(dimension)) {
				// Chapter 5.2.18 Request active power 113
				return MessageFormat.format(OpenWebNetConstant.DIMENSION_18_STATUS_RESULT, new Object[] {
						getWho(), where, dimension, "" + new Random().nextInt(14000) });
			} else if(CounterConverter.CounterDimension.ENERGY_UNIT_TOTALIZER.getCode().equals(dimension) ||
					CounterConverter.CounterDimension.PARTIAL_TOTALIZER_FOR_CURRENT_MONTH.getCode().equals(dimension) ||
					CounterConverter.CounterDimension.PARTIAL_TOTALIZER_FOR_CURRENT_DAY.getCode().equals(dimension)
					) {
				// chapter 5.2.19 Request energy totalizer 51
				// chapter 5.2.21 Request partial totalizer for current month 53
				// chapter 5.2.22 Request partial totalizer for current day 54
				List<DimensionValue> dimensionList = dimensionListManager.get(where + "-" + dimension);
				
				StringBuilder sb = new StringBuilder();
				for (DimensionValue dv : dimensionList) {
					sb.append(dv.getValue());
					sb.append(OpenWebNetConstant.DIMENSION_SEPARATOR);
				}
				sb.setLength(sb.length() - 1);
				return MessageFormat.format(OpenWebNetConstant.DIMENSION_18_STATUS_RESULT, new Object[] {
						getWho(), where, dimension, sb.toString() });
				
			} else {
				throw new UnsupportedOperationException("Command not supported [" + where + ":" + dimension +  "]");
			}
		}
	}

	@Override
	public String getWho() {
		return CounterConverter.OPEN_WEB_WHO;
	}	
}
