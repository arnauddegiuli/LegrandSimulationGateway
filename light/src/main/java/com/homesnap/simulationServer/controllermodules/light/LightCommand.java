package com.homesnap.simulationServer.controllermodules.light;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/*
 * #%L
 * HomeSnap Legrand Simulation Gateway LightModule
 * %%
 * Copyright (C) 2011 - 2017 A. de Giuli
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

import com.homesnap.engine.connector.openwebnet.OpenWebNetConstant;
import com.homesnap.engine.connector.openwebnet.light.LightStatusConverter;
import com.homesnap.simulationServer.ControllerStateManagement;
import com.homesnap.simulationServer.controllermodules.StatusManager;

public class LightCommand {
	
	// OSGi Shell Function
	static final String[] functions = { "on", "off", "loadConfiguration"};
	
	public String on(String address) {
		return ControllerStateManagement.executeCommand(MessageFormat.format(OpenWebNetConstant.COMMAND, new Object[] {new LightStatusConverter().getOpenWebWho(), LightStatusConverter.LightStatus.LIGHT_ON.getCode(), address} ));
	}
	
	public String off(String address) {
		return ControllerStateManagement.executeCommand(MessageFormat.format(OpenWebNetConstant.COMMAND, new Object[] {new LightStatusConverter().getOpenWebWho(), LightStatusConverter.LightStatus.LIGHT_OFF.getCode(), address} ));
	}
	
	public String loadConfiguration(String file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			LightSimulator.setStatusManager(new StatusManager(fis));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "Load failed.";
		}
		
		return "Load done (at least file exist...).";
	}
}
