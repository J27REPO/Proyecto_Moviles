package es.uniovi.recetasasturianas.data.remote.dto

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests para las funciones de mapeo en Mappers.kt.
 */
class MappersTest {

    // ==================== extractRestaurantName ====================

    @Test
    fun extractRestaurantName_withPrefix_removesPrefix() {
        val result = extractRestaurantName("Por Casa Fermín")
        assertEquals("Casa Fermín", result)
    }

    @Test
    fun extractRestaurantName_withoutPrefix_returnsOriginal() {
        val result = extractRestaurantName("Casa Fermín")
        assertEquals("Casa Fermín", result)
    }

    @Test
    fun extractRestaurantName_null_returnsUnknown() {
        val result = extractRestaurantName(null)
        assertEquals("Desconocido", result)
    }

    @Test
    fun extractRestaurantName_empty_returnsUnknown() {
        val result = extractRestaurantName("")
        assertEquals("Desconocido", result)
    }

    // ==================== parseTimeToMinutes ====================

    @Test
    fun parseTimeToMinutes_onlyMinutes() {
        val result = parseTimeToMinutes("45 minutos")
        assertEquals(45, result)
    }

    @Test
    fun parseTimeToMinutes_onlyHours() {
        val result = parseTimeToMinutes("2 horas")
        assertEquals(120, result)
    }

    @Test
    fun parseTimeToMinutes_hoursAndMinutes() {
        val result = parseTimeToMinutes("1 hora 30 minutos")
        assertEquals(90, result)
    }

    @Test
    fun parseTimeToMinutes_pluralHours() {
        val result = parseTimeToMinutes("3 horas")
        assertEquals(180, result)
    }

    @Test
    fun parseTimeToMinutes_null_returnsNull() {
        val result = parseTimeToMinutes(null)
        assertNull(result)
    }

    @Test
    fun parseTimeToMinutes_empty_returnsNull() {
        val result = parseTimeToMinutes("")
        assertNull(result)
    }

    @Test
    fun parseTimeToMinutes_onlyNumber() {
        val result = parseTimeToMinutes("45")
        assertEquals(45, result)
    }

    // ==================== extractRestaurantUrl ====================

    @Test
    fun extractRestaurantUrl_relativeUrl_prependsDomain() {
        val html = """<a href="/ruta/restaurante/casa-fermin">Casa Fermín</a>"""
        val result = extractRestaurantUrl(html)
        assertEquals("https://www.turismoasturias.es/ruta/restaurante/casa-fermin", result)
    }

    @Test
    fun extractRestaurantUrl_absoluteUrl_returnsAsIs() {
        val html = """<a href="https://example.com/restaurante">Restaurante</a>"""
        val result = extractRestaurantUrl(html)
        assertEquals("https://example.com/restaurante", result)
    }

    @Test
    fun extractRestaurantUrl_null_returnsNull() {
        val result = extractRestaurantUrl(null)
        assertNull(result)
    }

    @Test
    fun extractRestaurantUrl_noHref_returnsNull() {
        val html = "<p>Sin enlace</p>"
        val result = extractRestaurantUrl(html)
        assertNull(result)
    }

    // ==================== extractImageUrl ====================

    @Test
    fun extractImageUrl_validJson_returnsUrl() {
        val json = """{"classPK":94845,"groupId":"39908","title":"foto.jpg","uuid":"a744d57d-73a3-09f5-bb64-837d690337a4"}"""
        val result = extractImageUrl(json, null)
        assertEquals(
            "https://www.turismoasturias.es/documents/39908/a744d57d-73a3-09f5-bb64-837d690337a4/foto.jpg",
            result
        )
    }

    @Test
    fun extractImageUrl_nestedInImageField_returnsUrl() {
        val json = """{"image": {"classPK":94845,"groupId":"39908","title":"foto.jpg","uuid":"a744d57d-73a3-09f5-bb64-837d690337a4"}}"""
        val result = extractImageUrl(json, null)
        assertEquals(
            "https://www.turismoasturias.es/documents/39908/a744d57d-73a3-09f5-bb64-837d690337a4/foto.jpg",
            result
        )
    }

    @Test
    fun extractImageUrl_withSpacesInTitle_encodesUrl() {
        val json = """{"groupId":"39908","title":"foto con espacios.jpg","uuid":"uuid-123"}"""
        val result = extractImageUrl(json, null)
        assertEquals(
            "https://www.turismoasturias.es/documents/39908/uuid-123/foto%20con%20espacios.jpg",
            result
        )
    }

    @Test
    fun extractImageUrl_invalidJson_returnsNull() {
        val result = extractImageUrl("not a json", null)
        assertNull(result)
    }

    @Test
    fun extractImageUrl_null_returnsNull() {
        val result = extractImageUrl(null, null)
        assertNull(result)
    }
}
