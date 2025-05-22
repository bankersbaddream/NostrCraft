package org.sectiontwo.nostrcraft

import org.java_websocket.client.*
import org.java_websocket.handshake.*
import java.net.URI
import kotlinx.serialization.json.*
import java.security.*
import java.math.BigInteger
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.util.encoders.Hex
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
                Bukkit.getLogger().info("Connected to Nostr relay: $relay")
            }
            
            override fun onMessage(message: String?) {
                // Handle relay responses if needed
                message?.let {
                    Bukkit.getLogger().fine("Relay response: $it")
                }
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
        if (::ws.isInitialized && !ws.isClosed) {
            ws.close()
        }
    }
    
    fun publishNote(content: String, tags: List<List<String>> = emptyList()) {
        if (!::ws.isInitialized || ws.isClosed) {
            Bukkit.getLogger().warning("WebSocket not connected, cannot publish note")
            return
        }
        
        try {
            val now = System.currentTimeMillis() / 1000
            
            // Create the event array for ID calculation (NIP-01 format)
            val eventArray = JsonArray(listOf(
                JsonPrimitive(0), // reserved
                JsonPrimitive(publicKeyHex), // pubkey
                JsonPrimitive(now), // created_at
                JsonPrimitive(1), // kind
                buildJsonArray { 
                    tags.forEach { tag ->
                        addJsonArray { 
                            tag.forEach { str -> add(JsonPrimitive(str)) } 
                        } 
                    } 
                }, // tags
                JsonPrimitive(content) // content
            ))
            
            // Calculate the event ID (sha256 of the serialized event array)
            val serializedEvent = Json.encodeToString(JsonArray.serializer(), eventArray)
            val id = serializedEvent.sha256()
            
            // Sign the ID
            val sig = signEventId(id)
            
            // Create the complete event
            val eventMessage = buildJsonObject {
                put("id", id)
                put("pubkey", publicKeyHex)
                put("created_at", now)
                put("kind", 1)
                putJsonArray("tags") { 
                    tags.forEach { tag ->
                        addJsonArray { 
                            tag.forEach { str -> add(JsonPrimitive(str)) } 
                        } 
                    } 
                }
                put("content", content)
                put("sig", sig)
            }
            
            // Send to relay
            val message = buildJsonArray {
                add("EVENT")
                add(eventMessage)
            }
            
            ws.send(Json.encodeToString(JsonArray.serializer(), message))
            Bukkit.getLogger().fine("Published note: $content")
            
        } catch (e: Exception) {
            Bukkit.getLogger().severe("Error publishing note: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun signEventId(eventId: String): String {
        try {
            // Convert hex strings to bytes
            val privateKeyBytes = Hex.decode(privateKeyHex)
            val eventIdBytes = Hex.decode(eventId)
            
            // Get secp256k1 curve parameters
            val curve = ECNamedCurveTable.getParameterSpec("secp256k1")
            val domain = ECDomainParameters(curve.curve, curve.g, curve.n, curve.h)
            
            // Create private key parameter
            val privateKeyInt = BigInteger(1, privateKeyBytes)
            val privateKeyParams = ECPrivateKeyParameters(privateKeyInt, domain)
            
            // Sign using ECDSA
            val signer = ECDSASigner()
            signer.init(true, privateKeyParams)
            val signature = signer.generateSignature(eventIdBytes)
            
            // Format signature as hex (r + s)
            val r = signature[0].toByteArray()
            val s = signature[1].toByteArray()
            
            // Ensure r and s are 32 bytes each
            val rPadded = ByteArray(32)
            val sPadded = ByteArray(32)
            
            System.arraycopy(r, maxOf(0, r.size - 32), rPadded, maxOf(0, 32 - r.size), minOf(32, r.size))
            System.arraycopy(s, maxOf(0, s.size - 32), sPadded, maxOf(0, 32 - s.size), minOf(32, s.size))
            
            return Hex.toHexString(rPadded + sPadded)
            
        } catch (e: Exception) {
            Bukkit.getLogger().severe("Error signing Nostr event: ${e.message}")
            throw e
        }
    }
}

fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(this.toByteArray(Charsets.UTF_8))
    return Hex.toHexString(hash)
}
