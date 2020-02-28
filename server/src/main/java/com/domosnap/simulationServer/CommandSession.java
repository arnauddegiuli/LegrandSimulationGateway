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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.domosnap.engine.Log;
import com.domosnap.engine.Log.Session;
import com.domosnap.engine.connector.impl.openwebnet.conversion.core.OpenWebNetCommand;
import com.domosnap.engine.connector.impl.openwebnet.conversion.core.parser.ParseException;

public class CommandSession {
	private Socket client; // liaison avec client
	private BufferedReader depuisClient; // réception de requête
	private PrintWriter versClient; // envoi des réponses
	private Log log = new Log(CommandSession.class.getSimpleName());

	public CommandSession(Socket client, BufferedReader depuisClient, PrintWriter versClient) {
		this.client = client;
		this.depuisClient = depuisClient;
		this.versClient = versClient;
	}

	private void write(String msg) {
		versClient.print(msg);
		versClient.flush();
		log.fine(Session.Server, "Send to COMMAND client: " + msg);
	}

	private String read(){
		int indice = 0;
		boolean exit = false;
		char respond[] = new char[1024];
		char c = ' ';
		int ci = 0;
		String responseString = null;

		try{
			do {
				if(client != null && !client.isInputShutdown()) {
					ci = depuisClient.read();
					if (ci == -1) {
						log.finest(Session.Server, "End of read from command client socket.");
						client = null;
						break;
					} else { 
						c = (char) ci;
						if (c == '#' && indice > 1 && '#' == respond[indice-1]) {
							respond[indice] = c;
							exit = true;
							break;
						} else {
							respond[indice] = c;
							indice = indice + 1;
						} 
					}
				} else {
					stop();
					break;
				}
			} while(true); 
		} catch(IOException e) {
			log.severe(Session.Server, "Socket not available");
		}

		if (exit == true){
			responseString = new String(respond,0,indice+1);
		}

		log.fine(Session.Server, "Read from COMMAND client: " + responseString);

		return responseString;
	}

	public void run() {
		boolean continu = true;
		String lue; // la requête
		while (continu) {
			lue = read();
			if (lue == null) {
				continu = false;
			}
			else {
				try {
					OpenWebNetCommand parser = new OpenWebNetCommand(lue);
					if (parser.isStandardCommand() || parser.isDimensionCommand()) {
						write(ControllerStateManagement.executeCommand(lue));
					} else {
						List<String> result = ControllerStateManagement.executeStatus(lue);
						for (String string : result) {
							write(string);
						}
					}	
				} catch (ParseException e) {
					log.finest(Session.Server, "Error during parsing command [" + lue + "]: command is not supported or wrong...");
					e.printStackTrace();
				}
				
			}
		}
		stop();
	}

	public void stop() {
		try {
			log.fine(Session.Server, "End Command Session.");
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			log.severe(Session.Server, "Exception à la fermeture d'une connexion : "
					+ e.getMessage());
		}
	}
}
