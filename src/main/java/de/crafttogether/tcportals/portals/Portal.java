package de.crafttogether.tcportals.portals;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.tc.SignActionHeader;
import de.crafttogether.tcportals.util.CTLocation;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class Portal {
    private Integer id;
    private String name;
    private PortalType type;
    private String targetHost;
    private Integer targetPort;
    private CTLocation targetLocation;

    public enum PortalType {
        IN, OUT, BIDIRECTIONAL
    }

    public Portal(String name, PortalType type, Integer id, String targetHost, Integer targetPort, CTLocation targetLocation) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.targetLocation = targetLocation;
    }

    public Integer getId() { return id; }
    public String getName() {
        return this.name;
    }
    public PortalType getType() { return this.type; }
    public String getTargetHost() { return targetHost; }
    public Integer getTargetPort() { return targetPort; }
    public CTLocation getTargetLocation() { return targetLocation; }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(PortalType type) {
        this.type = type;
    }
    public void setTargetHost(String targetHost) { this.targetHost = targetHost; }
    public void setTargetPort(Integer targetPort) { this.targetPort = targetPort; }
    public void setTargetLocation(CTLocation targetLocation) { this.targetLocation = targetLocation; }

    public static boolean isValid(Sign sign) {
        SignActionHeader actionSign = SignActionHeader.parseFromSign(sign);
        return actionSign.isValid() && (
                ("portal").equalsIgnoreCase(sign.getLine(1)) ||
                ("portal-in").equalsIgnoreCase(sign.getLine(1)) ||
                ("portal-out").equalsIgnoreCase(sign.getLine(1))
        );
    }

    public Sign getSign() {
        Location location = this.getTargetLocation().getBukkitLocation();
        if (location == null || location.getWorld() == null) return null;

        Block block = location.getWorld().getBlockAt(location);
        Sign sign = BlockUtil.getSign(block);
        if (isValid(sign)) return sign;

        return null;
    }

    public String toString() {
        return "id=" + id + ", name=" + name + ", targetHost=" + targetHost + ", targetPort=" + targetPort + ", location=[" + (targetLocation == null ? null : targetLocation.toString()) + "]";
    }
}