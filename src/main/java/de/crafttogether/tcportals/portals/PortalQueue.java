package de.crafttogether.tcportals.portals;

import com.bergerkiller.bukkit.common.entity.type.CommonMinecartFurnace;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions;
import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.net.packets.TrainPacket;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class PortalQueue {

    private final Portal portal;
    private final Queue<QueuedTrain> queue = new LinkedList<>();
    private final Consumer consumer;

    private BukkitTask task;

    @SuppressWarnings("unused")
    interface Consumer {
        boolean operation(QueuedTrain next);
    }

    public PortalQueue(Portal portal, Consumer consumer) {
        this.portal = portal;
        this.consumer = consumer;
    }

    private void processQueue() {
        QueuedTrain item = queue.peek();

        if (item == null)
            cancel();

        else if (consumer.operation(item))
            queue.poll();
    }

    public void addTrain(Portal portal, TrainPacket packet, SpawnableGroup spawnable, SpawnableGroup.SpawnLocationList spawnLocations) {
        queue.offer(new QueuedTrain(portal, packet, spawnable, spawnLocations));
        process();
    }

    public void process() {
        if (task != null && !task.isCancelled())
            return;

        task = Bukkit.getScheduler().runTaskTimer(TCPortals.plugin, this::processQueue, 0L, 1L);
    }

    public List<QueuedTrain> getQueuedTrains() {
        return queue.stream().toList();
    }

    public void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }

    public Portal getPortal() {
        return portal;
    }

    public static class QueuedTrain {
        private final Portal portal;
        private final TrainPacket trainPacket;
        private final SpawnableGroup spawnableGroup;
        private final SpawnableGroup.SpawnLocationList spawnLocations;

        private Map<CommonMinecartFurnace, Integer> fuelMap;
        private MinecartGroup spawnedGroup;
        private CollisionOptions collisionOptions;
        private double waitDistance;

        private QueuedTrain(Portal portal, TrainPacket trainPacket, SpawnableGroup spawnableGroup, SpawnableGroup.SpawnLocationList spawnLocations) {
            this.portal = portal;
            this.trainPacket = trainPacket;
            this.spawnableGroup = spawnableGroup;
            this.spawnLocations = spawnLocations;
        }

        public Portal getPortal() {
            return portal;
        }

        public TrainPacket getTrainPacket() {
            return trainPacket;
        }

        public SpawnableGroup getSpawnableGroup() {
            return spawnableGroup;
        }

        public SpawnableGroup.SpawnLocationList getSpawnLocations() {
            return spawnLocations;
        }

        public MinecartGroup getSpawnedGroup() {
            return spawnedGroup;
        }

        public Map<CommonMinecartFurnace, Integer> getFuelMap() {
            return fuelMap;
        }

        public void setFuelMap(Map<CommonMinecartFurnace, Integer> fuelMap) {
            this.fuelMap = fuelMap;
        }

        public boolean hasFuel() {
            return fuelMap != null;
        }

        public void setSpawnedGroup(MinecartGroup group) {
            spawnedGroup = group;
        }

        public boolean isSpawned() {
            return spawnedGroup != null;
        }

        public void setCollisionOptions(CollisionOptions collisionOptions) {
            this.collisionOptions = collisionOptions;
        }

        public CollisionOptions getCollisionOptions() {
            return this.collisionOptions;
        }

        public void setWaitDistance(double waitDistance) {
            this.waitDistance = waitDistance;
        }

        public double getWaitDistance() {
            return waitDistance;
        }
    }
}
