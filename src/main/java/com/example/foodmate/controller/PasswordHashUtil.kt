package com.example.foodmate.controller

import java.security.MessageDigest

object PasswordHashUtil {

    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray())
        return bytesToHexString(hashedBytes)
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val stringBuilder = StringBuilder()
        for (byte in bytes) {
            val hex = String.format("%02X", byte)
            stringBuilder.append(hex)
        }
        return stringBuilder.toString()
    }
}

