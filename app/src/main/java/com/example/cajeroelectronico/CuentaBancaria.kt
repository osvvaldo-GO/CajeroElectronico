package com.example.cajeroelectronico

/**
 * Clase de dominio que representa una cuenta bancaria.
 * Encapsula el saldo y las operaciones de consignación y retiro.
 */
data class Transaccion(
    val tipo: String,       // "CONSIGNACIÓN" o "RETIRO"
    val monto: Double,
    val saldoResultante: Double
)

class CuentaBancaria(saldoInicial: Double = 0.0) {

    private var _saldo: Double = saldoInicial
    private val _historial: MutableList<Transaccion> = mutableListOf()

    /** Saldo actual de la cuenta (solo lectura desde fuera). */
    val saldo: Double
        get() = _saldo

    /** Historial de transacciones realizadas en esta sesión. */
    val historial: List<Transaccion>
        get() = _historial.toList()

    /**
     * Realiza una consignación (depósito) en la cuenta.
     * @param monto Valor a consignar. Debe ser mayor que 0.
     * @return Result.success con el nuevo saldo, o Result.failure con el mensaje de error.
     */
    fun consignar(monto: Double): Result<Double> {
        if (monto <= 0) {
            return Result.failure(IllegalArgumentException("El monto a consignar debe ser mayor a cero."))
        }
        _saldo += monto
        _historial.add(Transaccion("CONSIGNACIÓN", monto, _saldo))
        return Result.success(_saldo)
    }

    /**
     * Realiza un retiro de la cuenta.
     * @param monto Valor a retirar. Debe ser mayor que 0 y no superar el saldo disponible.
     * @return Result.success con el nuevo saldo, o Result.failure con el mensaje de error.
     */
    fun retirar(monto: Double): Result<Double> {
        if (monto <= 0) {
            return Result.failure(IllegalArgumentException("El monto a retirar debe ser mayor a cero."))
        }
        if (monto > _saldo) {
            return Result.failure(
                IllegalStateException("Saldo insuficiente. Saldo disponible: $${"%.2f".format(_saldo)}")
            )
        }
        _saldo -= monto
        _historial.add(Transaccion("RETIRO", monto, _saldo))
        return Result.success(_saldo)
    }
}
