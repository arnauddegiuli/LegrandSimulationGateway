package com.domosnap.simulationServer;

/*
 * #%L
 * DomoSnap Legrand Simulation Gateway Server
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.domosnap.engine.Log;
import com.domosnap.engine.Log.Session;
import com.domosnap.engine.adapter.impl.openwebnet.connector.OpenWebNetConstant;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.core.parser.CommandParser;
import com.domosnap.engine.adapter.impl.openwebnet.conversion.core.parser.ParseException;
import com.domosnap.simulationServer.controllermodules.ControllerSimulator;

public class ControllerStateManagement {
	
	private static Hashtable<String, ControllerSimulator> controllerCommandList = new Hashtable<String, ControllerSimulator>();
	private static Log log = new Log(ControllerStateManagement.class.getSimpleName());
	
	private static List<MonitorSession> monitorList = new ArrayList<MonitorSession>();
	
	/**
	 * Register a new ControllerCommand. Call by a module (for example light module) to register it to the server.
	 * @param controllerCommand controller to register
	 */
	public synchronized static void registerControllerCommand(ControllerSimulator controllerCommand) {
		synchronized (controllerCommandList) {
			controllerCommandList.put(controllerCommand.getWho(), controllerCommand);	
		}
		
	}
	
	/**
	 * Unregister a controller. Call by a module (for example light module) when user stop the module.
	 * @param controllerCommand controller to unregister
	 */
	public synchronized static void unRegisterControllerCommand(ControllerSimulator controllerCommand) {
		synchronized (controllerCommandList) {
			controllerCommandList.remove(controllerCommand.getWho());
		}
	}

	public synchronized static void registerMonitorSession(MonitorSession monitor) {
		synchronized (monitorList) {
			monitorList.add(monitor);
		}
		
	}
	
	public synchronized static void unRegisterMonitorSession(MonitorSession monitor) {
		synchronized (monitorList) {
			monitorList.remove(monitor);
		}
	}	
	
	/**
	 * Simulate the execution of the command.
	 * @param command command to execute
	 * @return the result of the command
	 */
	public synchronized static String executeCommand(String command) {
		try {
			CommandParser parser = CommandParser.parse(command);
			String who = parser.getWho();
			String result;
			ControllerSimulator cc;
			synchronized (controllerCommandList) {
				cc = controllerCommandList.get(who);
			
				if (cc != null) {
					result = cc.execute(command);
				} else {
					System.out.println("Command not supported [" + command + "]");
					result = OpenWebNetConstant.NACK;
				}
	
				if (!OpenWebNetConstant.NACK.equalsIgnoreCase(result)) {
					synchronized (monitorList) {
						// Monitor session closed is only detected when we try to lunch a command on it
						// So, here we clone the monitor list since in monitor(command) method, if monitor session has been closed,
						// it is removed from the monitorList => cause a concurrent modification not prevented by the lock because we
						// are in the same thread...
						List<MonitorSession> monitorList2 = new ArrayList<MonitorSession>(monitorList);
						for (MonitorSession monitor : monitorList2) {
							monitor.monitor(command);
						}
					}
				}
			}
			return result;
		} catch (ParseException e) {
			log.finest(Session.Server, "Error during parsing command [" + command + "]");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Simulate the execution of the command (status request)
	 * @param command the status request to execute
	 * @return the status
	 */
	public synchronized static List<String> executeStatus(String command) {
		try {
			CommandParser parser = CommandParser.parse(command);
			String who = parser.getWho();
			ControllerSimulator cc;

			synchronized (controllerCommandList) {
				cc = controllerCommandList.get(who);
			
				if (cc != null) {
					List<String> result = cc.status(command);
					if (!result.isEmpty()){				
						synchronized (monitorList) {
							// Monitor session closed is only detected when we try to lunch a command on it
							// So, here we clone the monitor list since in monitor(command) method, if monitor session has been closed,
							// it is removed from the monitorList => cause a concurrent modification not prevented by the lock because we
							// are in the same thread...
							List<MonitorSession> monitorList2 = new ArrayList<MonitorSession>(monitorList);
							for (MonitorSession monitor : monitorList2) {
								for (String c : result) {
									if (!OpenWebNetConstant.NACK.equalsIgnoreCase(c)) {
										monitor.monitor(c);
									}
								}
							}
						}
					}
					return result;
				} else {
					log.info(Session.Server, "Error during parsing command [" + command + "]");
					return new ArrayList<String>();
				}
				
			}
		} catch (ParseException e) {
			log.finest(Session.Server, "Error during parsing command [" + command + "]");
			e.printStackTrace();
			List<String> result = new ArrayList<String>();
			result.add(OpenWebNetConstant.NACK);
			return result;
		}
	}
}
