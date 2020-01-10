# mineweb_bridge

A bungee/spigot hybrid plugin that inject and expose a http server within the game instance

## Debug mode

You need to set the `DEBUG` env variable to `true`, so just start your minecraft server with `DEBUG=true java -jar minecraft.jar`
The HTTP server will then don't try to decipher request and cipher response, so you'll just need to make a request by serializing the json inside signed :

```curl
curl -XPOST http://localhost:25565/ask -d '{ "signed": "[ { \"name\":\"GET_PLAYER_COUNT\", \"args\": [] }]", "iv": "415454a12a1" }' 
```
You will received response serialized, signed will be ```{ "signed": "[{\"name\":\"GET_PLAYER_COUNT\",\"response\":0}]", "iv": "415454a12a1" }```

## Methods available 

For bukkit check here : https://github.com/vmarchaud/mineweb_bridge/blob/master/src/main/java/fr/vmarchaud/mineweb/bukkit/BukkitCore.java#L191
For bungee check here https://github.com/vmarchaud/mineweb_bridge/blob/master/src/main/java/fr/vmarchaud/mineweb/bungee/BungeeCore.java#L139
