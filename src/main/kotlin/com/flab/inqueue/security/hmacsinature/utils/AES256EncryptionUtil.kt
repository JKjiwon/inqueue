package com.flab.inqueue.security.hmacsinature.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class AES256EncryptionUtil(
    @Value("\${aes256.secret-key}") secretKey: String
) : EncryptionUtil {

    companion object {
        const val ENCRYPTION_ALGORITHM = "AES"
        const val ENCRYPTION_TRANSFORMATION = "AES/CBC/PKCS5Padding"
        const val AES256_SECRET_KEY_BYTE_SIZE = 32
    }

    private val secretKeySpec: SecretKeySpec
    private val ivParamSpec: IvParameterSpec

    init {
        if (secretKey.toByteArray().size != AES256_SECRET_KEY_BYTE_SIZE) {
            throw IllegalArgumentException("The secret key for aes256 is not $AES256_SECRET_KEY_BYTE_SIZE bytes.")
        }
        secretKeySpec = SecretKeySpec(secretKey.toByteArray(), ENCRYPTION_ALGORITHM)
        ivParamSpec = IvParameterSpec(secretKey.substring(0, 16).toByteArray())
    }

    override fun encrypt(messageToEncrypt: String): String {
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParamSpec)
        val encrypted = cipher.doFinal(messageToEncrypt.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    override fun decrypt(messageToDecrypt: String): String {
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParamSpec);

        val decodedBytes = Base64.getDecoder().decode(messageToDecrypt)
        val decrypted = cipher.doFinal(decodedBytes)
        return String(decrypted)
    }
}