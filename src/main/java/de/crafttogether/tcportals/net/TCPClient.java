package de.crafttogether.tcportals.net;

import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.common.utils.CommonUtil;
import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.net.events.ConnectionErrorEvent;
import de.crafttogether.tcportals.net.events.EntityReceivedEvent;
import de.crafttogether.tcportals.net.events.PacketReceivedEvent;
import de.crafttogether.tcportals.net.packets.AuthenticationPacket;
import de.crafttogether.tcportals.net.packets.EntityPacket;
import de.crafttogether.tcportals.net.packets.MessagePacket;
import de.crafttogether.tcportals.net.packets.Packet;
import de.crafttogether.tcportals.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class TCPClient extends Thread {
    public static final Collection<TCPClient> activeClients = new ArrayList<>();

    private Socket connection;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream objOutputStream;
    private ObjectInputStream objInputStream;

    private final UUID trainId;

    public enum Error {
        CONNECTION_REFUSED, INVALID_AUTHENTICATION, NOT_AUTHENTICATED, NO_REMOTE_CONNECTIONS
    }

    public TCPClient(Socket connection) {
        this.trainId = null;
        this.connection = connection;

        try {
            outputStream = connection.getOutputStream();
            inputStream = connection.getInputStream();
            objOutputStream = new ObjectOutputStream(outputStream);
            objInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (connection.isConnected() && objOutputStream != null)
            read();
    }

    public TCPClient(String host, int port, UUID trainId) {
        this.setName(TCPortals.plugin.getName() + " network thread");
        this.trainId = trainId;

        try {
            connection = new Socket(host, port);
            outputStream = connection.getOutputStream();
            inputStream = connection.getInputStream();
            objOutputStream = new ObjectOutputStream(outputStream);
            objInputStream = new ObjectInputStream(inputStream);
        }

        catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                Event event = new ConnectionErrorEvent(Error.CONNECTION_REFUSED, trainId, host, port);
                Bukkit.getServer().getScheduler().runTask(TCPortals.plugin, () -> CommonUtil.callEvent(event));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        if (connection != null && connection.isConnected() && objOutputStream != null) {
            //Util.debug("[TCPClient]: Successfully connected to " + host + ":" + port, false);
            start();
        }
    }

    @Override
    public void run() {
        read();
    }

    public void read() {
        activeClients.add(this);

        try {
            Object inputPacket;
            boolean authenticated = false;

            while (objInputStream != null && (inputPacket = objInputStream.readObject()) != null) {
                //Util.debug("[TCPClient]: Packet received: " + inputPacket.getClass().getTypeName() + " " + inputPacket.getClass().getName());

                // First packet has to be our secretKey
                if (!authenticated && inputPacket instanceof AuthenticationPacket packet) {
                    if (packet.key.equals(TCPortals.plugin.getConfig().getString("Portals.Server.SecretKey")))
                        authenticated = true;

                    else {
                        Util.debug("[TCPClient]: " + getAddress() + " has sent an invalid authentication");
                        send("ERROR:" + Error.INVALID_AUTHENTICATION.name());
                        disconnect();
                    }
                }

                else if (!authenticated && inputPacket instanceof MessagePacket packet) {
                    if (packet.message.startsWith("ERROR:")) {
                        String message = packet.message.replace("ERROR:", "");
                        Error error = null;

                        try {
                            error = Error.valueOf(message);
                        } catch (IllegalArgumentException ignored) {
                            Util.debug("Unkown error occured: " + message);
                        }

                        Event event = new ConnectionErrorEvent(error, trainId, getAddress(), connection.getPort());
                        Bukkit.getServer().getScheduler().runTask(TCPortals.plugin, () -> CommonUtil.callEvent(event));
                    }
                    else
                        Util.debug("Received Message: " + packet.message);

                    disconnect();
                }

                else if (authenticated) {
                    if (inputPacket instanceof EntityPacket packet) {
                        Event event = new EntityReceivedEvent(packet.uuid, packet.type, TCPortals.plugin.getServerName(), CommonTagCompound.readFromStream(inputStream, false));
                        Bukkit.getServer().getScheduler().runTask(TCPortals.plugin, () -> CommonUtil.callEvent(event));
                    }

                    else {
                        Util.debug(inputPacket.getClass().getName());
                        Event event = new PacketReceivedEvent(connection, (Packet) inputPacket);
                        Bukkit.getServer().getScheduler().runTask(TCPortals.plugin, () -> CommonUtil.callEvent(event));
                    }

                    disconnect();
                }

                else {
                    Util.debug("[TCPClient]: " + getAddress() + " is not authenticated.");
                    send("ERROR:" + Error.NOT_AUTHENTICATED.name());
                    disconnect();
                }
            }
        }

        catch (EOFException ignored) { }

        catch (SocketException ex) {
            if ("Socket closed".equals(ex.getMessage())) {
                Util.debug("[TCPClient]: Connection to " + connection.getInetAddress().getHostAddress() + " was closed.", false);
            } else {
                Util.debug("[TCPClient]: " + ex.getMessage());
            }
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

        finally {
            Util.debug("[TCPClient]: Closing connection to " + connection.getInetAddress().getHostAddress(), false);
            disconnect();
        }
    }

    public boolean send(Packet packet) {
        if (connection == null || !connection.isConnected() || connection.isClosed())
            return false;

        try {
            objOutputStream.reset();
            objOutputStream.writeObject(packet);
            objOutputStream.flush();

            //Util.debug("[TCPClient]: Packet was sent: " + packet.getClass().getTypeName() + " " + packet.getClass().getName());
        }
        catch (SocketException e) {
            Util.debug(e.getMessage());
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean send(String message) {
        return send(new MessagePacket(message));
    }

    public void disconnect() {
        try {
            if (objInputStream != null) {
                objInputStream.close();
                objInputStream = null;
            }

            if (objOutputStream != null) {
                objOutputStream.close();
                objInputStream = null;
            }

            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }

            activeClients.remove(this);
        } catch (Exception ex) {
            Util.debug(ex.getMessage());
        }
    }

    public UUID getTrainId() {
        return trainId;
    }

    public String getAddress() {
        return this.connection.getInetAddress().getHostAddress();
    }

    public static void closeAll() {
        int stopped = 0;

        for (TCPClient client : activeClients) {
            client.disconnect();
            stopped++;
        }

        Util.debug("[TCPClient]: Stopped " + stopped + " active clients.", false);
    }

    public void sendAuth(String secretKey) {
        AuthenticationPacket packet = new AuthenticationPacket(TCPortals.plugin.getServerName(), secretKey);
        send(packet);
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}