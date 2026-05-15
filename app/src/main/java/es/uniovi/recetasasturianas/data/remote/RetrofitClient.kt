package es.uniovi.recetasasturianas.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import es.uniovi.recetasasturianas.data.remote.dto.FlexibleAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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

    // Usamos la URL de la universidad por defecto
    private const val BASE_URL = BASE_URL_UNI

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(FlexibleAdapterFactory())
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val recipeApiService: RecipeApiService by lazy {
        retrofit.create(RecipeApiService::class.java)
    }
}
