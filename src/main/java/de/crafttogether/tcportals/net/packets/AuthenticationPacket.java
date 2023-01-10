package de.crafttogether.tcportals.net.packets;

public class AuthenticationPacket implements Packet {
    public final String server;
    public final String key;

    public AuthenticationPacket(String server, String key) {
        this.server = server;
        this.key = key;
    }
}
