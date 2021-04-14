package com.domosnap.simulationServer;

/*
 * #%L
 * DomoSnap Legrand Simulation Gateway Server
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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.domosnap.engine.Log;
import com.domosnap.engine.Log.Session;

public class MonitorSession {
	private Socket client; // liaison avec client
	private PrintWriter versClient; // envoi des réponses
	private Log log = new Log(MonitorSession.class.getSimpleName());
	
	public MonitorSession(Socket client, PrintWriter versClient) {
		this.client = client;
		this.versClient = versClient;
	}

	private void write(String msg) throws IOException {
		versClient.print(msg);
		if (versClient.checkError()) {
			log.severe(Session.Server, "ERROR WHEN MONITOR SERVER WRITE: " + msg);
			throw new IOException();
		} else {
			log.fine(Session.Server, "Send to MONITOR client: " + msg);
		}
	}

	public void monitor(String command) {
		try {
			write(command);
		} catch (IOException e) {
			log.severe(Session.Server, "Error with IO : " + e.getMessage());
			stop();
		}
	}
	
	public void stop() {
		try {
			log.fine(Session.Server, "End Monitor Session.");
			client.close();
		} catch (IOException e) {
			log.severe(Session.Server, "Error when closing connection : "
					+ e.getMessage());
		} finally {
			ControllerStateManagement.unRegisterMonitorSession(this);
		}
	}
}
