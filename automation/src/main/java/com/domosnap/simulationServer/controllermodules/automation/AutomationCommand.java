package com.domosnap.simulationServer.controllermodules.automation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/*
 * #%L
 * DomoSnap Legrand Simulation Gateway AutomationModule
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

import java.text.MessageFormat;

import com.domosnap.engine.connector.impl.openwebnet.connector.OpenWebNetConstant;
import com.domosnap.engine.connector.impl.openwebnet.conversion.shutter.ShutterStatusConverter;
import com.domosnap.simulationServer.ControllerStateManagement;
import com.domosnap.simulationServer.controllermodules.StatusManager;

public class AutomationCommand {
	
	// OSGi Shell Function
	static final String[] functions = { "up", "down", "stop", "loadConfiguration" };
	
	public String up(String address) {
		return ControllerStateManagement.executeCommand(MessageFormat.format(OpenWebNetConstant.COMMAND, new Object[] {new ShutterStatusConverter().getOpenWebWho() , ShutterStatusConverter.AutomationStatus.AUTOMATION_UP.getCode(), address} ));
	}
	
	public String down(String address) {
		return ControllerStateManagement.executeCommand(MessageFormat.format(OpenWebNetConstant.COMMAND, new Object[] {new ShutterStatusConverter().getOpenWebWho(), ShutterStatusConverter.AutomationStatus.AUTOMATION_DOWN.getCode(), address} ));
	}
	
	public String stop(String address) {
		return ControllerStateManagement.executeCommand(MessageFormat.format(OpenWebNetConstant.COMMAND, new Object[] {new ShutterStatusConverter().getOpenWebWho(), ShutterStatusConverter.AutomationStatus.AUTOMATION_STOP.getCode(), address} ));	
	}
	
	public String loadConfiguration(String file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			AutomationSimulator.setStatusManager(new StatusManager(fis));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "Load failed.";
		}
		
		return "Load done (at least file exist...).";
	}
}
