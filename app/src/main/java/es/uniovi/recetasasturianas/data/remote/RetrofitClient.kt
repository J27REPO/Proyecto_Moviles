package es.uniovi.recetasasturianas.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit singleton para la API de recetas.
 * Configura OkHttp con timeouts y logging para depuración.
 */
object RetrofitClient {

    // URL principal (servidor universidad, requiere VPN)
    private const val BASE_URL_UNI = "http://156.35.163.145/json/"

    // URL alternativa (Turismo Asturias, pública)
    private const val BASE_URL_PUBLIC = "https://www.turismoasturiasprofesional.es/open-data/"

    // Usamos la URL de la universidad por defecto (según petición del usuario)
    private const val BASE_URL = BASE_URL_UNI

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente OkHttp estándar. 
     * Nota: AndroidManifest.xml ya tiene android:usesCleartextTraffic="true"
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val recipeApiService: RecipeApiService by lazy {
        retrofit.create(RecipeApiService::class.java)
    }
}
