package org.sectiontwo.nostrcraft

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
// No need to import NostrClient as it's in the same package now

public class NostrCraft extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
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
