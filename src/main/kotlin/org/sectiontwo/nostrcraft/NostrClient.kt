package org.sectiontwo.nostrcraft

import org.java_websocket.client.*
import org.java_websocket.handshake.*
import java.net.URI
import java.util.*
import kotlinx.serialization.json.*
import java.security.*
import java.security.spec.*
import java.util.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bukkit.Bukkit

class NostrClient(
    private val relay: String,
    private val privateKeyHex: String,
    private val publicKeyHex: String
) {
    private lateinit var ws: WebSocketClient
    
    companion object {
        init {
            Security.addProvider(BouncyCastleProvider())
        }
    }
    
    fun connect() {
        ws = object : WebSocketClient(URI(relay)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Bukkit.getLogger().info("Connected to Nostr relay")
            }
            
            override fun onMessage(message: String?) {
                // Optional: handle relay responses
            }
            
            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Bukkit.getLogger().info("Nostr relay closed: $reason")
            }
            
            override fun onError(ex: Exception?) {
                Bukkit.getLogger().severe("Nostr relay error: ${ex?.message}")
            }
        }
        ws.connect()
    }
    
    fun disconnect() {
        if (::ws.isInitialized) {
            ws.close()
        }
    }
    
    fun publishNote(content: String, tags: List<List<String>> = emptyList()) {
        val now = System.currentTimeMillis() / 1000
        
        // Create unsigned event for ID calculation
        val unsignedEvent = buildJsonObject {
            put("pubkey", publicKeyHex)
            put("created_at", JsonPrimitive(now))
            put("kind", JsonPrimitive(1))
            putJsonArray("tags") { 
                tags.forEach { tag ->
                    addJsonArray { 
                        tag.forEach { str -> add(JsonPrimitive(str)) } 
                    } 
                } 
            }
            put("content", JsonPrimitive(content))
        }
        
        // Calculate the event ID (sha256 of the serialized event)
        val serializedEvent = Json.encodeToString(JsonObject.serializer(), unsignedEvent)
        val id = serializedEvent.sha256()
        
        // Sign the ID
        val sig = sign(id, privateKeyHex)
        
        // Create the complete event
        val eventMessage = buildJsonObject {
            put("id", JsonPrimitive(id))
            put("pubkey", JsonPrimitive(publicKeyHex))
            put("created_at", JsonPrimitive(now))
            put("kind", JsonPrimitive(1))
            putJsonArray("tags") { 
                tags.forEach { tag ->
                    addJsonArray { 
                        tag.forEach { str -> add(JsonPrimitive(str)) } 
                    } 
                } 
            }
            put("content", JsonPrimitive(content))
            put("sig", JsonPrimitive(sig))
        }
        
        // Send to relay
        ws.send("[\"EVENT\",${Json.encodeToString(JsonObject.serializer(), eventMessage)}]")
    }
    
    private fun sign(eventId: String, privateKeyHex: String): String {
        try {
            // Convert hex private key to bytes
            val privateKeyBytes = hexToBytes(privateKeyHex)
            
            // Create private key
            val kf = KeyFactory.getInstance("EC", "BC")
            val pkSpec = PKCS8EncodedKeySpec(privateKeyBytes)
            val privateKey = kf.generatePrivate(pkSpec)
            
            // Sign the event ID
            val signature = Signature.getInstance("SHA256withECDSA", "BC")
            signature.initSign(privateKey)
            signature.update(hexToBytes(eventId))
            
            // Return signature as hex string
            return bytesToHex(signature.sign())
        } catch (e: Exception) {
            Bukkit.getLogger().severe("Error signing Nostr event: ${e.message}")
            throw e
        }
    }
    
    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
    
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        bytes.forEach { b ->
            val i = b.toInt() and 0xFF
            result.append(hexChars[i shr 4])
            result.append(hexChars[i and 0x0F])
        }
        return result.toString()
    }
}

fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(this.toByteArray(Charsets.UTF_8))
    return bytesToHex(hash)
}

private fun bytesToHex(bytes: ByteArray): String {
    val hexChars = "0123456789abcdef"
    val result = StringBuilder(bytes.size * 2)
    bytes.forEach { b ->
        val i = b.toInt() and 0xFF
        result.append(hexChars[i shr 4])
        result.append(hexChars[i and 0x0F])
    }
    return result.toString()
}
