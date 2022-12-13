package com.squareup.cash.paykit.devapp

import android.text.Spanned
import androidx.core.text.HtmlCompat
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> prettyPrintDataClass(data: T, indent: Int = 2): Spanned {
  val klass = data::class as KClass<T>
  val properties = klass.declaredMemberProperties
    .associate { it.name to it.get(data) }
  val orderedProperties = "([A-Za-z0-9_]+)=".toRegex()
    .findAll(data.toString()).map { it.groupValues[1] }

  val htmlFormattedOutput = with(StringBuilder()) {
    val spaces = "&nbsp;".repeat(indent) // indent
    appendLine("<b>${klass.simpleName}</b>(")
    orderedProperties.forEach { propName ->
      val value = prettyPrintValue(properties[propName])
      appendLine("""<br>$spaces<b>$propName</b>=$value,""")
    }
    appendLine(")")
    toString()
  }
  return HtmlCompat.fromHtml(
    htmlFormattedOutput,
    HtmlCompat.FROM_HTML_MODE_COMPACT + HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
  )
}

private fun prettyPrintValue(value: Any?): String {
  if (value == null) return "null"
  return when (value) {
    is String -> "\"$value\""
    is List<*> -> "listOf(" + value.joinToString(", ") { prettyPrintValue(it) } + ")"
    else -> "$value"
  }
}