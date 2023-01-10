package de.crafttogether.tcportals.portals;

public class ReceivedTrain {
    private final Portal portal;
    private double travelledBlocks;

    public ReceivedTrain(Portal portal) {
        this.portal = portal;
        this.travelledBlocks = 0;
    }

    public Portal getPortal() {
        return portal;
    }

    public double getTravelledBlocks() {
        return travelledBlocks;
    }

    public void move(double distance) {
        travelledBlocks += distance;
    }
}
