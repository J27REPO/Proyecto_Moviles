package es.uniovi.recetasasturianas.data.remote.dto

import es.uniovi.recetasasturianas.data.model.Recipe
import org.json.JSONObject

/**
 * Funciones de mapeo para convertir DTOs de la API a entidades Room.
 */

fun ArticleDto.toEntity(index: Int, cachedAt: Long): Recipe {
    return Recipe(
        id = index,
        name = nombre?.content ?: "Sin nombre",
        restaurant = extractRestaurantName(resumen?.content),
        preparationHtml = informacion?.preparacion?.content ?: "",
        ingredientsHtml = contacto?.ingredientes?.content ?: "",
        imageUrl = extractImageUrl(imagen?.content, visualizador?.slide),
        restaurantUrl = extractRestaurantUrl(informacion?.donde?.content),
        timeMinutes = parseTimeToMinutes(contacto?.tiempo?.content),
        tipsHtml = observaciones?.trucosYConsejos?.content,
        notesHtml = observaciones?.observacion?.content,
        cachedAt = cachedAt
    )
}

fun extractRestaurantName(resumen: String?): String {
    if (resumen.isNullOrBlank()) return "Desconocido"
    return resumen.removePrefix("Por ").trim()
}

fun parseTimeToMinutes(raw: String?): Int? {
    if (raw.isNullOrBlank()) return null

    var total = 0

    val hoursRegex = Regex("""(\d+)\s*hora?s?""", RegexOption.IGNORE_CASE)
    hoursRegex.find(raw)?.let { match ->
        total += (match.groupValues[1].toIntOrNull() ?: 0) * 60
    }

    val minutesRegex = Regex("""(\d+)\s*minuto?s?""", RegexOption.IGNORE_CASE)
    minutesRegex.find(raw)?.let { match ->
        total += match.groupValues[1].toIntOrNull() ?: 0
    }

    if (total == 0) {
        val numberRegex = Regex("""(\d+)""")
        val numbers = numberRegex.findAll(raw).toList()
        if (numbers.isNotEmpty()) {
            total = numbers[0].groupValues[1].toIntOrNull() ?: 0
        }
    }

    return if (total > 0) total else null
}

fun extractImageUrl(imagenContent: String?, slide: SlideWrapper?): String? {
    imagenContent?.let { json ->
        buildImageUrlFromJson(json)?.let { return it }
    }

    slide?.value?.let { json ->
        buildImageUrlFromJson(json)?.let { return it }
    }
    
    slide?.slideUrl?.content?.let { url ->
        if (url.isNotBlank()) {
            val fullUrl = if (url.startsWith("http")) url else "https://www.turismoasturias.es$url"
            return if (fullUrl.contains("?")) fullUrl else "$fullUrl?version=1.0"
        }
    }

    return null
}

private fun buildImageUrlFromJson(jsonString: String): String? {
    if (jsonString.isNullOrBlank() || !jsonString.trim().startsWith("{")) return null
    
    return try {
        val json = JSONObject(jsonString)
        val targetJson = if (json.has("image")) json.getJSONObject("image") else json
        
        val groupId = targetJson.optString("groupId")
        val uuid = targetJson.optString("uuid")
        val title = targetJson.optString("title")

        if (groupId.isNotEmpty() && uuid.isNotEmpty() && title.isNotEmpty()) {
            val encodedTitle = title.replace(" ", "%20")
            "https://www.turismoasturias.es/documents/$groupId/0/$encodedTitle/$uuid?version=1.0"
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

fun extractRestaurantUrl(whereHtml: String?): String? {
    if (whereHtml.isNullOrBlank()) return null

    val hrefRegex = Regex("""href="([^"]+)"""")
    val match = hrefRegex.find(whereHtml) ?: return null

    val href = match.groupValues[1]
    return if (href.startsWith("http")) {
        href
    } else {
        "https://www.turismoasturias.es$href"
    }
}
