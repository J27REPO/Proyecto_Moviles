package es.uniovi.recetasasturianas.data.remote.dto

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

/**
 * DTO para la respuesta JSON completa de la API.
 * Estructura: { "articles": { "article": [...] } }
 */
data class RecipeResponse(
    @SerializedName("articles")
    val articles: ArticlesWrapper
)

data class ArticlesWrapper(
    @SerializedName("article")
    val article: List<ArticleDto>
)

/**
 * DTO para cada artículo/receta del JSON.
 * Los campos tienen estructura anidada con "content" dentro.
 * Usamos @JsonAdapter para manejar inconsistencias (objeto vs array).
 */
data class ArticleDto(
    @SerializedName("Nombre")
    @JsonAdapter(FlexibleContentDeserializer::class)
    val nombre: ContentWrapper? = null,

    @SerializedName("Resumen")
    @JsonAdapter(FlexibleContentDeserializer::class)
    val resumen: ContentWrapper? = null,

    @SerializedName("Imagen")
    @JsonAdapter(FlexibleContentDeserializer::class)
    val imagen: ContentWrapper? = null,

    @SerializedName("Visualizador")
    @JsonAdapter(FlexibleVisualizadorDeserializer::class)
    val visualizador: VisualizadorWrapper? = null,

    @SerializedName("Informacion")
    @JsonAdapter(FlexibleInformacionDeserializer::class)
    val informacion: InformacionWrapper? = null,

    @SerializedName("Contacto")
    @JsonAdapter(FlexibleContactoDeserializer::class)
    val contacto: ContactoWrapper? = null,

    @SerializedName("Observaciones")
    @JsonAdapter(FlexibleObservacionesDeserializer::class)
    val observaciones: ObservacionesWrapper? = null
)

/**
 * Wrapper genérico para campos con "content".
 */
data class ContentWrapper(
    @SerializedName("content")
    val content: String? = null
)

/**
 * Función de ayuda para obtener el primer objeto de un JsonElement que puede ser objeto o array.
 */
private fun JsonElement.getFirstObject(): JsonObject? {
    return when {
        this.isJsonObject -> this.asJsonObject
        this.isJsonArray -> {
            val array = this.asJsonArray
            if (array.size() > 0 && array[0].isJsonObject) array[0].asJsonObject else null
        }
        else -> null
    }
}

/**
 * Deserializador manual y flexible para ContentWrapper.
 */
class FlexibleContentDeserializer : JsonDeserializer<ContentWrapper> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ContentWrapper? {
        val obj = json.getFirstObject() ?: return null
        val contentElement = obj.get("content") ?: return null
        
        val content = when {
            contentElement.isJsonPrimitive -> contentElement.asString
            contentElement.isJsonObject -> contentElement.toString()
            else -> null
        }
        
        return ContentWrapper(content)
    }
}

data class VisualizadorWrapper(
    @SerializedName("Slide")
    val slide: SlideWrapper? = null
)

class FlexibleVisualizadorDeserializer : JsonDeserializer<VisualizadorWrapper> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): VisualizadorWrapper? {
        val obj = json.getFirstObject() ?: return null
        val slideElement = obj.get("Slide") ?: return null
        val slideObj = slideElement.getFirstObject() ?: return VisualizadorWrapper(null)
        
        val value = slideObj.get("value")?.let { if (it.isJsonPrimitive) it.asString else null }
        val slideUrlElement = slideObj.get("SlideUrl")?.getFirstObject()
        val slideUrl = slideUrlElement?.get("content")?.let { if (it.isJsonPrimitive) it.asString else null }
        
        return VisualizadorWrapper(SlideWrapper(value, slideUrl))
    }
}

data class SlideWrapper(
    @SerializedName("value")
    val value: String? = null,
    @SerializedName("SlideUrl")
    val slideUrl: String? = null
)

data class InformacionWrapper(
    @SerializedName("Preparacion")
    val preparacion: ContentWrapper? = null,

    @SerializedName("Donde")
    val donde: ContentWrapper? = null,

    @SerializedName("JornadasGastronomicas")
    val jornadasGastronomicas: ContentWrapper? = null
)

class FlexibleInformacionDeserializer : JsonDeserializer<InformacionWrapper> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): InformacionWrapper? {
        val obj = json.getFirstObject() ?: return null
        
        fun parseContent(fieldName: String): ContentWrapper? {
            return obj.get(fieldName)?.getFirstObject()?.let {
                ContentWrapper(it.get("content")?.let { c -> if (c.isJsonPrimitive) c.asString else null })
            }
        }

        return InformacionWrapper(
            preparacion = parseContent("Preparacion"),
            donde = parseContent("Donde"),
            jornadasGastronomicas = parseContent("JornadasGastronomicas")
        )
    }
}

data class ContactoWrapper(
    @SerializedName("Tiempo")
    val tiempo: ContentWrapper? = null,

    @SerializedName("Ingredientes")
    val ingredientes: ContentWrapper? = null
)

class FlexibleContactoDeserializer : JsonDeserializer<ContactoWrapper> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ContactoWrapper? {
        val obj = json.getFirstObject() ?: return null

        fun parseContent(fieldName: String): ContentWrapper? {
            return obj.get(fieldName)?.getFirstObject()?.let {
                ContentWrapper(it.get("content")?.let { c -> if (c.isJsonPrimitive) c.asString else null })
            }
        }

        return ContactoWrapper(
            tiempo = parseContent("Tiempo"),
            ingredientes = parseContent("Ingredientes")
        )
    }
}

data class ObservacionesWrapper(
    @SerializedName("TrucosYConsejos")
    val trucosYConsejos: ContentWrapper? = null,

    @SerializedName("Observacion")
    val observacion: ContentWrapper? = null
)

class FlexibleObservacionesDeserializer : JsonDeserializer<ObservacionesWrapper> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ObservacionesWrapper? {
        val obj = json.getFirstObject() ?: return null

        fun parseContent(fieldName: String): ContentWrapper? {
            return obj.get(fieldName)?.getFirstObject()?.let {
                ContentWrapper(it.get("content")?.let { c -> if (c.isJsonPrimitive) c.asString else null })
            }
        }

        return ObservacionesWrapper(
            trucosYConsejos = parseContent("TrucosYConsejos"),
            observacion = parseContent("Observacion")
        )
    }
}
