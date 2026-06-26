package iss.nus.edu.sg.weather.data.remote

import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

/**
 * Generates JWT tokens for QWeather API using Ed25519.
 * Uses net.i2p.crypto:eddsa directly (no JCA provider registration needed).
 */
object JwtManager {

    private var cachedToken: String? = null
    private var tokenExpiry: Long = 0

    @Synchronized
    fun getToken(kid: String, sub: String, privateKeyBase64: String): String {
        val now = System.currentTimeMillis() / 1000
        if (cachedToken != null && now < tokenExpiry - 300) {
            return cachedToken!!
        }
        cachedToken = generateJwt(kid, sub, privateKeyBase64)
        tokenExpiry = now + 900
        return cachedToken!!
    }

    private fun generateJwt(kid: String, sub: String, privateKeyBase64: String): String {
        val privateKey = loadPrivateKey(privateKeyBase64)
        val urlEncoder = Base64.getUrlEncoder().withoutPadding()

        // Header + Payload
        val headerB64 = urlEncoder.encodeToString("{\"alg\":\"EdDSA\",\"kid\":\"$kid\"}".toByteArray())
        val iat = System.currentTimeMillis() / 1000 - 30
        val exp = iat + 900
        val payloadB64 = urlEncoder.encodeToString("{\"sub\":\"$sub\",\"iat\":$iat,\"exp\":$exp}".toByteArray())
        val signingInput = "$headerB64.$payloadB64"

        // Sign with EdDSAEngine directly (bypasses JCA)
        val engine = EdDSAEngine(MessageDigest.getInstance("SHA-512"))
        engine.initSign(privateKey)
        val sigB64 = urlEncoder.encodeToString(engine.signOneShot(signingInput.toByteArray()))

        return "$signingInput.$sigB64"
    }

    private fun loadPrivateKey(base64Key: String): PrivateKey {
        val cleaned = base64Key.trim()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        return EdDSAPrivateKey(PKCS8EncodedKeySpec(Base64.getDecoder().decode(cleaned)))
    }
}
