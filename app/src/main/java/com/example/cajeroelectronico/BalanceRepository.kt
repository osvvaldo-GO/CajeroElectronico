package com.example.cajeroelectronico

import android.content.Context
import android.content.SharedPreferences

/**
 * Repositorio encargado de persistir y recuperar el saldo
 * de la cuenta bancaria usando SharedPreferences.
 */
class BalanceRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    /**
     * Guarda el saldo actual en el almacenamiento local del dispositivo.
     * @param saldo Valor del saldo a persistir.
     */
    fun guardarSaldo(saldo: Double) {
        prefs.edit()
            .putLong(KEY_SALDO, java.lang.Double.doubleToRawLongBits(saldo))
            .apply()
    }

    /**
     * Recupera el saldo guardado previamente.
     * Si no existe un valor guardado, retorna el saldo por defecto (0.0).
     * @return Saldo recuperado del almacenamiento local.
     */
    fun obtenerSaldo(): Double {
        val bits = prefs.getLong(KEY_SALDO, java.lang.Double.doubleToRawLongBits(SALDO_INICIAL))
        return java.lang.Double.longBitsToDouble(bits)
    }

    /**
     * Verifica si ya existe un saldo guardado (primera vez que se abre la app).
     */
    fun tieneSaldoGuardado(): Boolean {
        return prefs.contains(KEY_SALDO)
    }

    companion object {
        private const val PREFS_NAME = "cajero_prefs"
        private const val KEY_SALDO  = "saldo_cuenta"
        private const val SALDO_INICIAL = 0.0
    }
}
