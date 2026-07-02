package com.luke.cocoonlock

import android.content.Context
import java.security.MessageDigest

object PinStore {
    private const val PREFS = "cocoonlock_prefs"
    private const val KEY_HASH = "pin_hash"

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun savePin(context: Context, pin: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_HASH, hash(pin)).apply()
    }

    fun hasPin(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .contains(KEY_HASH)
    }

    fun verify(context: Context, pin: String): Boolean {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_HASH, null) ?: return false
        return stored == hash(pin)
    }
}
