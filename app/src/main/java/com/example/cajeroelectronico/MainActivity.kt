package com.example.cajeroelectronico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.cajeroelectronico.databinding.ActivityMainBinding

/**
 * Actividad principal del Cajero Electrónico.
 *
 * Responsabilidades:
 *  - Mostrar el saldo actual recuperado desde SharedPreferences.
 *  - Permitir al usuario realizar consignaciones y retiros.
 *  - Validar que el monto ingresado sea válido y que no se retire
 *    más del saldo disponible.
 *  - Persistir el saldo actualizado en SharedPreferences al salir
 *    o después de cada operación.
 *  - Mostrar el historial de transacciones de la sesión actual.
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding generado a partir de activity_main.xml
    private lateinit var binding: ActivityMainBinding

    // Capa de persistencia: guarda y recupera el saldo con SharedPreferences
    private lateinit var repository: BalanceRepository

    // Objeto de dominio que encapsula la lógica de la cuenta
    private lateinit var cuenta: CuentaBancaria

    // -------------------------------------------------------------------------
    // Ciclo de vida
    // -------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inicializarDatos()
        configurarBotones()
        actualizarSaldoUI()
    }

    /**
     * Persiste el saldo cada vez que la aplicación pasa a segundo plano,
     * garantizando que el saldo esté disponible al regresar.
     */
    override fun onPause() {
        super.onPause()
        repository.guardarSaldo(cuenta.saldo)
    }

    // -------------------------------------------------------------------------
    // Inicialización
    // -------------------------------------------------------------------------

    /**
     * Crea el repositorio y la cuenta bancaria cargando el saldo
     * previamente guardado (o 0.0 si es la primera vez).
     */
    private fun inicializarDatos() {
        repository = BalanceRepository(this)
        val saldoGuardado = repository.obtenerSaldo()
        cuenta = CuentaBancaria(saldoGuardado)
    }

    /**
     * Asigna los listeners a los botones de consignar, retirar y limpiar.
     */
    private fun configurarBotones() {
        binding.btnConsignar.setOnClickListener {
            procesarOperacion(esConsignacion = true)
        }

        binding.btnRetirar.setOnClickListener {
            procesarOperacion(esConsignacion = false)
        }

        binding.btnLimpiarHistorial.setOnClickListener {
            limpiarHistorialUI()
        }
    }

    // -------------------------------------------------------------------------
    // Lógica de operaciones
    // -------------------------------------------------------------------------

    /**
     * Lee el monto del campo de texto, valida el formato y delega
     * la operación a la instancia de [CuentaBancaria].
     *
     * @param esConsignacion true → consignar, false → retirar.
     */
    private fun procesarOperacion(esConsignacion: Boolean) {
        ocultarMensaje()

        val textoMonto = binding.etMonto.text.toString().trim()

        // Validar que el campo no esté vacío
        if (textoMonto.isEmpty()) {
            mostrarMensaje(
                getString(R.string.msg_campo_vacio),
                esError = true
            )
            return
        }

        // Validar que el texto sea un número válido
        val monto = textoMonto.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            mostrarMensaje(
                getString(R.string.msg_monto_invalido),
                esError = true
            )
            return
        }

        // Ejecutar la operación en el objeto de dominio
        val resultado = if (esConsignacion) cuenta.consignar(monto) else cuenta.retirar(monto)

        resultado
            .onSuccess { nuevoSaldo ->
                // Persistir inmediatamente el nuevo saldo
                repository.guardarSaldo(nuevoSaldo)

                val mensaje = if (esConsignacion) {
                    getString(R.string.msg_consignacion_exitosa, formatearMonto(nuevoSaldo))
                } else {
                    getString(R.string.msg_retiro_exitoso, formatearMonto(nuevoSaldo))
                }

                mostrarMensaje(mensaje, esError = false)
                actualizarSaldoUI()
                agregarItemHistorial(cuenta.historial.last())
                limpiarCampoMonto()
            }
            .onFailure { error ->
                mostrarMensaje(
                    "⚠️ ${error.message}",
                    esError = true
                )
            }
    }

    // -------------------------------------------------------------------------
    // Actualización de la UI
    // -------------------------------------------------------------------------

    /**
     * Actualiza el TextView del saldo con el valor actual de la cuenta.
     */
    private fun actualizarSaldoUI() {
        binding.tvSaldo.text = formatearMonto(cuenta.saldo)
        binding.tvSaldo.contentDescription =
            getString(R.string.desc_saldo_actual) + " " + formatearMonto(cuenta.saldo)
    }

    /**
     * Muestra un mensaje de éxito o error debajo de los botones.
     *
     * @param mensaje Texto a mostrar.
     * @param esError Si es true usa colores de error; si es false, colores de éxito.
     */
    private fun mostrarMensaje(mensaje: String, esError: Boolean) {
        binding.tvMensaje.apply {
            text = mensaje
            visibility = View.VISIBLE

            if (esError) {
                setTextColor(ContextCompat.getColor(context, R.color.rojo_retiro))
                background = ContextCompat.getDrawable(context, R.drawable.bg_mensaje_error)
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.verde_exito))
                background = ContextCompat.getDrawable(context, R.drawable.bg_mensaje)
            }
        }
    }

    private fun ocultarMensaje() {
        binding.tvMensaje.visibility = View.GONE
    }

    /**
     * Infla una vista de item_transaccion.xml y la añade al contenedor
     * de historial, mostrando tipo, monto y saldo resultante.
     */
    private fun agregarItemHistorial(transaccion: Transaccion) {
        // Ocultar el texto "sin transacciones"
        binding.tvHistorialVacio.visibility = View.GONE

        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.item_transaccion, binding.llHistorial, false)

        val tvIcono        = itemView.findViewById<TextView>(R.id.tvIcono)
        val tvTipo         = itemView.findViewById<TextView>(R.id.tvTipoTransaccion)
        val tvSaldoResult  = itemView.findViewById<TextView>(R.id.tvSaldoResultante)
        val tvMonto        = itemView.findViewById<TextView>(R.id.tvMontoTransaccion)

        val esConsignacion = transaccion.tipo == "CONSIGNACIÓN"

        tvIcono.text = if (esConsignacion) "+" else "-"
        tvTipo.text  = transaccion.tipo
        tvSaldoResult.text = "Saldo: ${formatearMonto(transaccion.saldoResultante)}"

        if (esConsignacion) {
            tvMonto.text = "+ ${formatearMonto(transaccion.monto)}"
            tvMonto.setTextColor(ContextCompat.getColor(this, R.color.verde_exito))
        } else {
            tvMonto.text = "- ${formatearMonto(transaccion.monto)}"
            tvMonto.setTextColor(ContextCompat.getColor(this, R.color.rojo_retiro))
        }

        // Insertar al inicio para ver la transacción más reciente primero
        binding.llHistorial.addView(itemView, 0)
    }

    private fun limpiarHistorialUI() {
        binding.llHistorial.removeAllViews()
        binding.tvHistorialVacio.visibility = View.VISIBLE
        ocultarMensaje()
    }

    private fun limpiarCampoMonto() {
        binding.etMonto.text?.clear()
        binding.tilMonto.error = null
    }

    // -------------------------------------------------------------------------
    // Utilidades
    // -------------------------------------------------------------------------

    /**
     * Formatea un valor Double como moneda con separador de miles y 2 decimales.
     * Ejemplo: 1500000.5 → "$1,500,000.50"
     */
    private fun formatearMonto(valor: Double): String {
        return "$%,.2f".format(valor)
    }
}
