package es.uniovi.recetasasturianas

import android.app.Application

/**
 * Clase Application de la aplicación.
 *
 * Inicializa componentes globales como:
 * - Bases de datos Room
 * - Cliente Retrofit
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // La inicialización de Room y Retrofit se hace de forma lazy en sus respectivas clases
    }
}
