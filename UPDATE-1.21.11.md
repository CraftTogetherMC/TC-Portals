# TC-Portals update to 1.21.11

## What was changed
- Java target raised from 17 to 21.
- Spigot API dependency raised from `1.20.1-R0.1-SNAPSHOT` to `1.21.11-R0.1-SNAPSHOT`.
- BKCommonLib dependency raised from `1.20.4-v4-SNAPSHOT` to `1.21.11-v1-SNAPSHOT`.
- TrainCarts dependency raised from `1.20.4-v2-SNAPSHOT` to `1.21.11-v3-SNAPSHOT`.
- `plugin.yml` `api-version` raised from `1.17` to `1.21`.
- README updated for the new baseline.

## Analysis
The plugin does not appear to use direct NMS imports or CraftBukkit internals. Most of the version-sensitive surface area comes from:
- Bukkit/Spigot API
- BKCommonLib
- TrainCarts

Because of that, the main migration step is dependency alignment. The codebase itself looks mostly version-agnostic, but the following classes are the most likely to need follow-up testing on a real 1.21.11 server:
- `PortalHandler`
- `TCHelper`
- `PlayerJoinListener`
- all `SignAction*` classes
