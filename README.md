![Icon](src/main/resources/assets/mc_server_analytics/icon.png)
# MC Server Analytics
[![License](https://img.shields.io/github/license/danieltebor/mc-server-analytics)]()
[![Tag](https://img.shields.io/github/v/tag/danieltebor/mc-server-analytics)]()<br>
MC Server Analytics is a lightweight Fabric server mod that adds utils for admins & players to analyze their server.

### Commands
- /chunk-info \<dimension\> (shows loaded chunks)
- /cpu (shows cpu thread load, overall load, and temperature)
- /entity-info \<dimension\> (shows number of entities)
- /mcsa-help (describes each command usage)
- /mem (shows server memory usage)
- /mspt (Shows avg milliseconds per tick (MSPT) for 5s, 15s, 1m, 5m, and 15m)
- /perf-sum (shows summary of server telemetry)
- /ping-avg (shows your ping or ping of specified player)
- /ping \<player\> (shows your ping or ping of specified player)
- /tps (shows avg server TPS for 5s, 15s, 1m, 5m, and 15m)
- /world-size (Shows world file size)

### Config
Commands permissions & whether they are enabled can be set in config/mc-server-analytics.properties

### Planned Commands & Features
- displaying cpu usage/memory on a client-side leaderboard

## Installation
### Dependencies
- [Fabric Server](https://fabricmc.net/use/server/)
- [Fabric API](https://github.com/username/repository)

### Manual Installation
Once the Fabric Minecraft Server is properly configured, download the requisite version of MC Server Analytics for your version of Minecraft as well as the requisite version of the Fabric API and move both mods into the mods folder.
