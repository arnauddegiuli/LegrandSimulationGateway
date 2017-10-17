package com.domosnap.simulationServer;

/*
 * #%L
 * DomoSnap Legrand Simulation Gateway Server
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.domosnap.engine.Log;
import com.domosnap.engine.Log.Session;
import com.domosnap.engine.connector.impl.openwebnet.connector.OpenWebNetConstant;

public class Server {

	Log log = new Log(Server.class.getSimpleName());
	private int port = 1234;
	private Integer password = 12345;
	private static final int nonce = 603356072;
	private final int poolSize = 50;

	private final ServerSocket serverSocket;
	private final ExecutorService pool;

	public Server() throws IOException {
		log.debug = true;
		log.error = true;
		log.info = true;
		log.finest = true;
		serverSocket = new ServerSocket(port);
		pool = Executors.newFixedThreadPool(poolSize);
	}

	public void stop() {
		pool.shutdown();
	}

	public void start() {
		try {
			System.out.println("Waiting connection for Monitor/Command on port [" + port + "]...");
			for (;;) {
				pool.execute(new Handler(serverSocket.accept()));
			}
		} catch (IOException e) {
			System.out.println("Error during Socket creation : "
					+ e.getMessage());
			System.exit(1);
			pool.shutdown();
		}
	}

	public static void main(String args[]) {

		try {
			Server s = new Server();

			// définition du port
			try {
				s.port = Integer.parseInt(args[0]);
			} catch (Exception e) {
				s.port = 1234; // valeur par défaut
			}

			s.start();
		} catch (IOException e) {
			System.out.println("Error during Server creation : "
					+ e.getMessage());
			System.exit(1);
		}

	}

	class Handler implements Runnable {
    	private final Socket socket;
    	
    	public Handler(Socket socket) {
			this.socket = socket;
		}

        public void run() {
    		// installation
				
			BufferedReader depuisClient; // réception de requête
			PrintWriter versClient; 	 // envoi des réponses

			try {
				// Read from client
				depuisClient = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				// Write to client
				versClient = new PrintWriter(new OutputStreamWriter(
						socket.getOutputStream()), true);
				// Welcome ack
				write(Session.Server, OpenWebNetConstant.ACK, versClient);
				String sessionType = read(socket, depuisClient);
				if (OpenWebNetConstant.MONITOR_SESSION.equalsIgnoreCase(sessionType)) {
					if (password != null) {
						write(Session.Monitor, "*#" + nonce + "##", versClient);
						String result = read(socket, depuisClient);
						if (!"*#25280520##".equals(result)) {
							log.fine(Session.Server, "Password error..."); 
							write(Session.Monitor, OpenWebNetConstant.NACK, versClient);
							return;
						}
					}
					
					log.fine(Session.Server, "Start Monitor Session..."); 
					write(Session.Monitor, OpenWebNetConstant.ACK, versClient);
					ControllerStateManagement.registerMonitorSession(
							new MonitorSession(socket, versClient)
					);

				} else if (OpenWebNetConstant.COMMAND_SESSION
						.equalsIgnoreCase(sessionType)) {
					if (password != null) {
						write(Session.Command, "*#" + nonce + "##", versClient);
						String result = read(socket, depuisClient);
						if (!"*#25280520##".equals(result)) {
							log.fine(Session.Server, "Password error..."); 
							write(Session.Command, OpenWebNetConstant.NACK, versClient);
							return;
						}
					}
					log.fine(Session.Server, "Start Command Session...");
					write(Session.Command, OpenWebNetConstant.ACK, versClient);
					new CommandSession(socket, depuisClient,
							versClient).run();

				} else {
					write(Session.Server, OpenWebNetConstant.NACK, versClient);
				}
			} catch (IOException e) {
				try {
					socket.close();
				} catch (IOException ee) {
				}
			}
    	}
    	
    	private void write(Session session, String msg, PrintWriter versClient) {
    		versClient.print(msg);
    		versClient.flush();
    		log.fine(Session.Server, "Send to " + session.name().toUpperCase() + " client: " + msg);
    	}

    	private String read(Socket client, BufferedReader depuisClient) {
    		int indice = 0;
    		boolean exit = false;
    		char respond[] = new char[1024];
    		char c = ' ';
    		int ci = 0;
    		String responseString = null;

    		try {
    			do {
    				if (client != null && !client.isInputShutdown()) {
    					ci = depuisClient.read();
    					if (ci == -1) {
    						// System.out.println("End of read from socket.");
    						// client = null;
    						// break;
    					} else {
    						c = (char) ci;
    						if (c == '#' && indice > 1
    								&& '#' == respond[indice - 1]) {
    							respond[indice] = c;
    							exit = true;
    							break;
    						} else {
    							respond[indice] = c;
    							indice = indice + 1;
    						}
    					}
    				}
    			} while (true);
    		} catch (IOException e) {
    			System.out.println("Socket not available");
    		}

    		if (exit == true) {
    			responseString = new String(respond, 0, indice + 1);
    		}

    		log.fine(Session.Server, "FROM SERVER CLIENT: " + responseString);

    		return responseString;
    	}

    }
}
