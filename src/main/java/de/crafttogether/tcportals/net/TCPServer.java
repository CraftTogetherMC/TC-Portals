package de.crafttogether.tcportals.net;

import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.util.Util;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class TCPServer extends Thread {
    private final String host;
    private final int port;
    private boolean listen;
    private ServerSocket serverSocket;
    private ArrayList<TCPClient> clients;

    public TCPServer(String host, int port) {
        this.setName(TCPortals.plugin.getName() + " network thread");
        this.host = host;
        this.port = port;
        start();
    }

    @Override
    public void run() {
        clients = new ArrayList<>();

        try {
            // Create ServerSocket
            serverSocket = new ServerSocket(port, 5, InetAddress.getByName(host));
            listen = true;

            Util.debug("[TCPServer]: Server is listening on port " + port, false);

            // Handle incoming connections
            while (listen && !isInterrupted()) {
                Socket connection = null;

                try {
                    connection = serverSocket.accept();
                } catch (SocketException e) {
                    Util.debug(e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (connection == null)
                    continue;

                TCPClient client = new TCPClient(connection);
                Util.debug("[TCPServer]: " + client.getAddress() + " connected.", false);

                // Should we accept remote connections?
                boolean acceptRemote = TCPortals.plugin.getConfig().getBoolean("Portals.Server.AcceptRemoteConnections");
                if (!acceptRemote && !client.getAddress().equals("127.0.0.1")) {
                    client.send("ERROR:" + TCPClient.Error.NO_REMOTE_CONNECTIONS.name());
                    client.disconnect();
                }

                clients.add(client);
            }
        } catch (BindException e) {
            TCPortals.plugin.getLogger().warning("[TCPServer]: Can't bind to " + port + ".. Port already in use!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        finally {
            close();
            Util.debug("[TCPServer]: Server stopped.", false);
        }
    }

    public void close() {
        if (!listen) return;
        listen = false;

        for (TCPClient client : clients)
            client.disconnect();

        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}