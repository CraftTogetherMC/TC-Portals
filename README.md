# TC-Destinations
#### Requires: [TrainCarts](https://github.com/bergerhealer/TrainCarts), [CTCommons](https://github.com/CraftTogetherMC/CTCommons) and a MySQL-Database


TC-Portals is a plugin for minecraft servers using [SpigotMC](https://www.spigotmc.org), [PaperMC](https://papermc.io) or forks of these projects.  
It serves as an add-on for the [TrainCarts](https://github.com/bergerhealer/TrainCarts) plugin which adds ActionSigns, supporting cross-server teleporting of trains in a BungeeCord network.

This plugin was developed for the [CraftTogetherMC](https://github.com/CraftTogetherMC) minecraft-server, see also: [TC-Destinations](https://github.com/CraftTogetherMC/TC-Destinations)!

### A big thank you and lots of love go out to [TeamBergerhealer](https://github.com/bergerhealer)
Also, a lot of appreciation goes to the People behind [Cloud](https://github.com/Incendo/cloud) and [Adventure](https://github.com/KyoriPowered/adventure)!  
  
#### Dev-Builds: [See here](https://ci.craft-together-mc.de/job/TC%20Destinations/)  
  
[![](https://i.imgur.com/SzkHTE8.png)](https://www.youtube.com/watch?v=8XCvmY8EPtk)  

|                                      |                                                                                                                                                                                                                                                 |
|:-------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ![](https://i.imgur.com/LnmPAAy.png) | **Bidirectional Portal**<br/><br/>This sign represents a portal entrance and exit at the same time.<br/>A pair of these signs that have the same channel name can be passed from either side.<br/>The other sign acts as a portal exit.         |
| ![](https://i.imgur.com/ybuisvC.png) | **Directional Portal** *(Entrance)*<br/><br/>This sign represents a portal entrance.<br/>A portal of this type requires a `portal-out`-sign that has the same channel name.<br/>Any number of `portal-in`-signs can be created for one channel. |
| ![](https://i.imgur.com/3UlGw1q.png) | **Directional Portal** *(Exit)*<br/><br/>This sign represents a portal exit.<br/>Signs of this type can only exist once per channel name.                                                                                                       |

### Flags
There are some flags you can use on the fourth line of each portal-sign to control whether itens and mobs are transported.
- `!mobs` mobs are killed
- `!items` items are killed
- `-mobs` mobs are ejected
- `-items` items are dropped

## Pathfinding across servers
With a little extra work, it is possible to reach destinations on another server.

For example, if you want to drive from server1 to a destination on server2,  
you need a destination on server1 that leads to the portal, which leads to server2.  
Let's call it server2 as well.  
  
Now you create a [route](https://wiki.traincarts.net/p/TrainCarts/PathFinding#Route_Manager) for the train, which first contains server1  
and then the destination you want to reach.  
  
Thats it!
  
If you use [TC-Destinations](https://github.com/CraftTogetherMC/TC-Destinations) to manage your destinations, it will do the routing part for you.

### Libraries used
- [CTCommons](https://github.com/CraftTogetherMC/CTCommons) (CraftTogether's plugin library)
- [BKCommonLib](https://github.com/bergerhealer) (Extensive plugin library)
- [Adventure](https://github.com/KyoriPowered/adventure) (UI Framework)

### MySQL Table-structure:

``` sql
CREATE TABLE `cb_destinations` (
  `id` int(11) NOT NULL,
  `name` varchar(24) NOT NULL,
  `type` varchar(24) NOT NULL,
  `server` varchar(24) NOT NULL,
  `world` varchar(24) NOT NULL,
  `loc_x` double NOT NULL,
  `loc_y` double NOT NULL,
  `loc_z` double NOT NULL,
  `owner` varchar(36) NOT NULL,
  `participants` longtext DEFAULT NULL,
  `public` tinyint(1) NOT NULL,
  `tp_x` double DEFAULT NULL,
  `tp_y` double DEFAULT NULL,
  `tp_z` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `cb_destinations`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `cb_destinations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

CREATE TABLE `cb_portals` (
    `id` int(11) NOT NULL,
    `name` varchar(16) NOT NULL,
    `type` varchar(16) NOT NULL,
    `target_host` varchar(128) DEFAULT NULL,
    `target_port` int(11) DEFAULT NULL,
    `target_server` varchar(128) DEFAULT NULL,
    `target_world` varchar(128) DEFAULT NULL,
    `target_x` double DEFAULT NULL,
    `target_y` double DEFAULT NULL,
    `target_z` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `cb_portals`
    ADD PRIMARY KEY (`id`),
    ADD KEY `name` (`name`) USING BTREE;

ALTER TABLE `cb_portals`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

COMMIT;
```
