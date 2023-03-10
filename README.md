![](https://img.shields.io/github/license/CraftTogetherMC/TC-Portals?style=flat-square)
![](https://img.shields.io/github/last-commit/CraftTogetherMC/TC-Portals?style=flat-square)
![](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.craft-together-mc.de%2Fjob%2FTC%2520Portals&style=flat-square)
![](https://shields-io-visitor-counter.herokuapp.com/badge?page=CraftTogetherMC.TC-Portals&style=flat-square)
![](https://img.shields.io/spiget/download-size/107442?style=flat-square)
![](https://img.shields.io/spiget/downloads/107442?style=flat-square)
  
[Development Builds](https://ci.craft-together-mc.de/job/TC%20Portals/) / 
[Modrinth](https://modrinth.com/plugin/tc-portals) / 
[SpigotMC](https://www.spigotmc.org/resources/tc-portals.107442/)
  
# TC-Portals
#### Requires: [TrainCarts](https://github.com/bergerhealer/TrainCarts), [CTCommons](https://github.com/CraftTogetherMC/CTCommons) and a MySQL-Database
**Java-Version:** 17 or above  
**Minecraft-Version:** 1.17.1 or above  

TC-Portals is a plugin for Minecraft servers using [SpigotMC](https://www.spigotmc.org), [PaperMC](https://papermc.io) or forks of these projects.  
It serves as an add-on for the [TrainCarts](https://github.com/bergerhealer/TrainCarts) plugin which adds ActionSigns, supporting cross-server teleporting of trains in a BungeeCord network.   
It is very lightweight as database queries and network connections run asynchronously where possible.

This plugin was developed for the [CraftTogetherMC](https://github.com/CraftTogetherMC) Minecraft server, see also: [TC-Destinations](https://github.com/CraftTogetherMC/TC-Destinations)!

### A big thank you and lots of love go out to [TeamBergerhealer](https://github.com/bergerhealer)
Also, a lot of appreciation goes to the people behind [Cloud](https://github.com/Incendo/cloud) and [Adventure](https://github.com/KyoriPowered/adventure)!   
  
[![](https://i.imgur.com/SzkHTE8.png)](https://www.youtube.com/watch?v=8XCvmY8EPtk)  

## ActionSigns

The signs behave like ordinary traincarts signs.  
You can de/activate them with redstone, alter trigger-directions, etc.  

A portal exit behaves like a [spawn sign](https://wiki.traincarts.net/p/TrainCarts/Signs/Spawner), so you must set a direction for it like you do with the spawn sign.  
The spawn direction can be set by specifying trigger directions.  
For example: `[train:left]` or `[train:right]`. If none is set, the train is spawned based on what side is powered by redstone.  
  
If you use `[cart]` instead of `[train]`, a train will be spawned at the exit cart by cart.  

### Signs
|                                      |                                                                                                                                                                                                                                                 |
|:-------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ![](https://i.imgur.com/EPwpDDO.png) | **Bidirectional Portal**<br/><br/>This sign represents a portal entrance and exit at the same time.<br/>A pair of these signs that have the same channel name can be passed from either side.<br/>The other sign acts as a portal exit.         |
| ![](https://i.imgur.com/Aevpead.png) | **Directional Portal** *(Entrance)*<br/><br/>This sign represents a portal entrance.<br/>A portal of this type requires a `portal-out`-sign that has the same channel name.<br/>Any number of `portal-in`-signs can be created for one channel. |
| ![](https://i.imgur.com/VJEQ2fP.png) | **Directional Portal** *(Exit)*<br/><br/>This sign represents a portal exit.<br/>Signs of this type can only exist once per channel name.                                                                                                       |

### Flags
There are some flags you can use on the fourth line of a portal-sign to control whether items and mobs are transported.  
You can use them at the entrance as well as at the exit or both.
- `!mobs` means mobs are killed
- `!items` means items are killed
- `-mobs` means mobs are ejected
- `-items` means items are dropped

## Pathfinding across servers (BungeeCord)
With a little extra work, it is possible to reach destinations on another server.

For example, if you want to drive from server1 to a destination on server2,
you need to create a [destination](https://wiki.traincarts.net/p/TrainCarts/Signs/Destination) on server1 that leads to the portal, which leads to server2.  
Let's name it server2 as well.  
  
Now you create a [route](https://wiki.traincarts.net/p/TrainCarts/PathFinding#Route_Manager) for the train, which first contain the destination: `server2` and then afterwards,  
the destination you want to reach.
  
Thats it!
  
If you use [TC-Destinations](https://github.com/CraftTogetherMC/TC-Destinations) to manage your destinations, it will do the routing part for you.

## Libraries used
- [CTCommons](https://github.com/CraftTogetherMC/CTCommons) (CraftTogether's plugin library)
- [BKCommonLib](https://github.com/bergerhealer) (Extensive plugin library)
- [Adventure](https://docs.adventure.kyori.net) (Very neat UI-Framework for Bukkit/Bungeecord and more)
- [MiniMessage](https://docs.adventure.kyori.net/minimessage) (Text format to represent chat components)
- [HikariCP](https://github.com/brettwooldridge/HikariCP) (High-performance, JDBC connection pool)
- [MySQL Connector/J](https://github.com/mysql/mysql-connector-j) (JDBC-Driver)

### MySQL Table-structure:

``` sql
CREATE TABLE `tc_portals` (
    `id` int(11) NOT NULL,
    `name` varchar(16) NOT NULL,
    `type` varchar(16) NOT NULL,
    `host` varchar(128) DEFAULT NULL,
    `port` int(11) DEFAULT NULL,
    `server` varchar(128) DEFAULT NULL,
    `world` varchar(128) DEFAULT NULL,
    `x` double DEFAULT NULL,
    `y` double DEFAULT NULL,
    `z` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `tc_portals`
    ADD PRIMARY KEY (`id`),
    ADD KEY `name` (`name`) USING BTREE;

ALTER TABLE `tc_portals`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

COMMIT;
```
