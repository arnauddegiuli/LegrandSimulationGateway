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

import com.domosnap.engine.controller.who.Who;

public class UnknownDeviceException extends Exception {

	private static final long serialVersionUID = 1L;
	private Who who;
	private String where;
	private String what;
	
	public UnknownDeviceException(Who who, String where, String what) {
		this.where = where;
		this.what = what;
		this.who = who;
	}
	
	public String getMessage() {
		return "Device " + who + " doesn't exist [" + where + ":" + what +  "]";
	}
	
}
