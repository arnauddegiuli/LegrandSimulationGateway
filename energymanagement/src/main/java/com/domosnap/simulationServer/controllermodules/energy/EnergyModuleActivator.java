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


import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.domosnap.simulationServer.ControllerStateManagement;
import com.domosnap.simulationServer.controllermodules.DimensionManager;
import com.domosnap.simulationServer.controllermodules.StatusManager;

public class EnergyModuleActivator implements BundleActivator {

	private EnergySimulator energyModule;
	
	@Override
	public void start(BundleContext context) throws Exception {
		energyModule = new EnergySimulator(new StatusManager(context.getBundle().getResource("energystatus.properties").openConnection().getInputStream()), new DimensionManager(context.getBundle().getResource("energydimension.properties").openConnection().getInputStream()));
		Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put("osgi.command.scope", "energy");
		dict.put("osgi.command.function", EnergyCommand.functions);

		context.registerService(EnergyCommand.class.getName(), new EnergyCommand(), dict);
		
		ControllerStateManagement.registerControllerCommand(energyModule);
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		ControllerStateManagement.registerControllerCommand(energyModule);
	}
}
