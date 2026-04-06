package es.uniovi.recetasasturianas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

/**
 * Splash Screen Activity.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Navegar directamente a MainActivity
        splashScreen.setKeepOnScreenCondition { false }
        
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
