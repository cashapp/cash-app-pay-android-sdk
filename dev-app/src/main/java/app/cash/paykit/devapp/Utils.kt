package app.cash.paykit.devapp

import android.text.Spanned
import androidx.core.text.HtmlCompat
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> prettyPrintDataClass(data: T, indent: Int = 3): Spanned {
  val htmlFormattedOutput = ppDataClass(data, indent)
  return HtmlCompat.fromHtml(
    htmlFormattedOutput,
    HtmlCompat.FROM_HTML_MODE_COMPACT + HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH,
  )
}

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> ppDataClass(data: T, indent: Int = 3): String {
  val klass = data::class as KClass<T>
  val properties = klass.declaredMemberProperties
    .associate { it.name to it.get(data) }

  val htmlFormattedOutput = with(StringBuilder()) {
    val spaces = "&nbsp;".repeat(indent) // indent
    appendLine("<b>${klass.simpleName}</b>(")
    properties.forEach { prop ->
      appendLine("""<br>$spaces<b>${prop.key}</b>=${prettyPrintValue(prop.value, indent)},""")
    }
    appendLine(")")
    toString()
  }
  return htmlFormattedOutput
}

private fun prettyPrintValue(value: Any?, currentIndent: Int): String {
  if (value == null) return "null"
  return when (value) {
    is String -> "\"$value\""
    is List<*> -> "listOf(" + value.joinToString(", ") { prettyPrintValue(it, currentIndent) } + ")"
    else -> ppDataClass(value, currentIndent + 3)
  }
}
