package es.uniovi.recetasasturianas.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type
import java.lang.reflect.ParameterizedType

/**
 * DTO para la respuesta JSON completa de la API.
 * Estructura: { "articles": { "article": [...] } }
 */
@JsonClass(generateAdapter = true)
data class RecipeResponse(
    @Json(name = "articles")
    val articles: ArticlesWrapper
)

@JsonClass(generateAdapter = true)
data class ArticlesWrapper(
    @Json(name = "article")
    val article: List<ArticleDto>
)

/**
 * DTO para cada artículo/receta del JSON.
 * Los campos "content" pueden venir como objeto o array.
 * Usamos Moshi con adapters personalizados para manejar estas inconsistencias.
 */
@JsonClass(generateAdapter = true)
data class ArticleDto(
    @Json(name = "Nombre")
    val nombre: ContentWrapper? = null,

    @Json(name = "Resumen")
    val resumen: ContentWrapper? = null,

    @Json(name = "Imagen")
    val imagen: ContentWrapper? = null,

    @Json(name = "Visualizador")
    val visualizador: VisualizadorWrapper? = null,

    @Json(name = "Informacion")
    val informacion: InformacionWrapper? = null,

    @Json(name = "Contacto")
    val contacto: ContactoWrapper? = null,

    @Json(name = "Observaciones")
    val observaciones: ObservacionesWrapper? = null
)

/**
 * Wrapper genérico para campos con "content".
 */
data class ContentWrapper(
    val content: String? = null
)

@JsonClass(generateAdapter = true)
data class VisualizadorWrapper(
    @Json(name = "Slide")
    val slide: SlideWrapper? = null
)

@JsonClass(generateAdapter = true)
data class SlideWrapper(
    @Json(name = "value")
    val value: String? = null,
    @Json(name = "SlideUrl")
    val slideUrl: ContentWrapper? = null
)

@JsonClass(generateAdapter = true)
data class InformacionWrapper(
    @Json(name = "Preparacion")
    val preparacion: ContentWrapper? = null,

    @Json(name = "Donde")
    val donde: ContentWrapper? = null,

    @Json(name = "JornadasGastronomicas")
    val jornadasGastronomicas: ContentWrapper? = null
)

@JsonClass(generateAdapter = true)
data class ContactoWrapper(
    @Json(name = "Tiempo")
    val tiempo: ContentWrapper? = null,

    @Json(name = "Ingredientes")
    val ingredientes: ContentWrapper? = null
)

@JsonClass(generateAdapter = true)
data class ObservacionesWrapper(
    @Json(name = "TrucosYConsejos")
    val trucosYConsejos: ContentWrapper? = null,

    @Json(name = "Observacion")
    val observacion: ContentWrapper? = null
)

/**
 * Fábrica de adapters Moshi que maneja campos que pueden ser
 * un objeto JSON o un array de objetos JSON (flexibilidad).
 *
 * Funciona extrayendo el primer elemento si el token es BEGIN_ARRAY
 * y delegando la lectura real al adapter estándar de Moshi.
 */
class FlexibleAdapterFactory : JsonAdapter.Factory {

    private val flexibleTypes = setOf(
        ContentWrapper::class.java,
        VisualizadorWrapper::class.java,
        InformacionWrapper::class.java,
        ContactoWrapper::class.java,
        ObservacionesWrapper::class.java
    )

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val rawType = if (type is ParameterizedType) type.rawType else type
        if (rawType !in flexibleTypes) return null

        val delegate = moshi.nextAdapter<Any>(this, type, annotations)

        return object : JsonAdapter<Any>() {
            override fun fromJson(reader: JsonReader): Any? {
                return when (reader.peek()) {
                    JsonReader.Token.BEGIN_OBJECT -> delegate.fromJson(reader)
                    JsonReader.Token.BEGIN_ARRAY -> {
                        reader.beginArray()
                        val result = if (reader.hasNext()) delegate.fromJson(reader) else null
                        while (reader.hasNext()) reader.skipValue()
                        reader.endArray()
                        result
                    }
                    else -> {
                        reader.skipValue()
                        null
                    }
                }
            }

            override fun toJson(writer: JsonWriter, value: Any?) {
                delegate.toJson(writer, value)
            }
        }
    }
}
