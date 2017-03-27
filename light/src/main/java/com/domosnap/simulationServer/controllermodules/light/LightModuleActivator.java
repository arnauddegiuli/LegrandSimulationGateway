package com.domosnap.simulationServer.controllermodules.light;

/*
 * #%L
 * DomoSnap Legrand Simulation Gateway LightModule
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


import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.domosnap.simulationServer.ControllerStateManagement;
import com.domosnap.simulationServer.controllermodules.StatusManager;

public class LightModuleActivator implements BundleActivator {

	private LightSimulator lightModule;
	
	@Override
	public void start(BundleContext context) throws Exception {
		
		lightModule = new LightSimulator(new StatusManager(context.getBundle().getResource("light.properties").openConnection().getInputStream()));
		
		Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put("osgi.command.scope", "light");
		dict.put("osgi.command.function", LightCommand.functions);

		context.registerService(LightCommand.class.getName(), new LightCommand(), dict);
		
		ControllerStateManagement.registerControllerCommand(lightModule);
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		ControllerStateManagement.unRegisterControllerCommand(lightModule);
	}
	
	

}
