package es.uniovi.recetasasturianas.util

import androidx.test.espresso.IdlingResource
import java.util.concurrent.atomic.AtomicInteger

/**
 * IdlingResource para contar operaciones de carga de datos.
 * Útil para sincronizar tests con la carga de la API.
 */
class DataLoadingIdlingResource(
    private val resourceName: String = "DataLoading"
) : IdlingResource {

    private val counter = AtomicInteger(0)
    private var callback: IdlingResource.ResourceCallback? = null

    /**
     * Incrementa el contador de operaciones activas.
     */
    fun increment() {
        counter.incrementAndGet()
    }

    /**
     * Decrementa el contador de operaciones activas.
     * Notifica al callback si pasa a idle.
     */
    fun decrement() {
        val previous = counter.decrementAndGet()
        if (previous == 0) {
            callback?.onTransitionToIdle()
        }
    }

    override fun getName(): String = resourceName

    override fun isIdleNow(): Boolean = counter.get() == 0

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.callback = callback
        if (isIdleNow) {
            callback.onTransitionToIdle()
        }
    }
}
