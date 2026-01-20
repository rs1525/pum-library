package com.akustom15.pum.model

/**
 * Represents a Kustom Widget (KWGT) item
 *
 * @param id Unique identifier
 * @param name Display name (e.g., "LNX 001")
 * @param description Brief description (e.g., "Widget Clock size 2x2")
 * @param fileName Name of the .kwgt file in assets
 * @param previewUrl URL or path to preview image
 * @param widgetSize Size description (e.g., "2x2", "4x2")
 */
data class WidgetItem(
        val id: String,
        val name: String,
        val description: String,
        val fileName: String,
        val previewUrl: String? = null,
        val widgetSize: String? = null
)
