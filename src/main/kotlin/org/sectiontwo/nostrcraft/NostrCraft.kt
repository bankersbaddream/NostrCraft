package org.sectiontwo.nostrcraft

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.Bukkit

class NostrCraft : JavaPlugin(), Listener {
    
    private lateinit var nostrClient: NostrClient
    
    override fun onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig()
        
        // Load configuration
        val privateKey = config.getString("private-key") ?: ""
        val publicKey = config.getString("public-key") ?: ""
        val relay = config.getString("relay") ?: "wss://relay.damus.io"
        
        if (privateKey.isEmpty() || publicKey.isEmpty()) {
            logger.severe("Private key or public key not configured! Plugin disabled.")
            pluginLoader.disablePlugin(this)
            return
        }
        
        // Initialize Nostr client
        nostrClient = NostrClient(relay, privateKey, publicKey)
        
        // Connect to relay
        try {
            nostrClient.connect()
            logger.info("NostrCraft enabled and connected to $relay")
            
            // Publish server start event
            nostrClient.publishNote("Minecraft server started", 
                listOf(listOf("e", "server_start")))
                
        } catch (e: Exception) {
            logger.severe("Failed to connect to Nostr relay: ${e.message}")
        }
        
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, this)
    }
    
    override fun onDisable() {
        if (::nostrClient.isInitialized) {
            // Publish server stop event
            nostrClient.publishNote("Minecraft server stopped", 
                listOf(listOf("e", "server_stop")))
            
            // Give a moment for the message to send
            Thread.sleep(1000)
            
            nostrClient.disconnect()
        }
        logger.info("NostrCraft disabled")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerName = event.player.name
        nostrClient.publishNote("Player $playerName joined the server", 
            listOf(listOf("p", playerName), listOf("e", "join")))
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val playerName = event.player.name
        nostrClient.publishNote("Player $playerName left the server", 
            listOf(listOf("p", playerName), listOf("e", "quit")))
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val playerName = event.player.name
        val blockType = event.blockPlaced.type.toString()
        val location = "${event.blockPlaced.location.x}, ${event.blockPlaced.location.y}, ${event.blockPlaced.location.z}"
        
        nostrClient.publishNote("Player $playerName placed $blockType at $location", 
            listOf(listOf("p", playerName), listOf("e", "block_place"), listOf("b", blockType), listOf("l", location)))
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val playerName = event.player.name
        val blockType = event.block.type.toString()
        val location = "${event.block.location.x}, ${event.block.location.y}, ${event.block.location.z}"
        
        nostrClient.publishNote("Player $playerName broke $blockType at $location", 
            listOf(listOf("p", playerName), listOf("e", "block_break"), listOf("b", blockType), listOf("l", location)))
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hasBlock() && event.clickedBlock != null) {
            val playerName = event.player.name
            val blockType = event.clickedBlock!!.type.toString()
            val location = "${event.clickedBlock!!.location.x}, ${event.clickedBlock!!.location.y}, ${event.clickedBlock!!.location.z}"
            
            nostrClient.publishNote("Player $playerName interacted with $blockType at $location", 
                listOf(listOf("p", playerName), listOf("e", "interact"), listOf("b", blockType), listOf("l", location)))
        }
    }
}
