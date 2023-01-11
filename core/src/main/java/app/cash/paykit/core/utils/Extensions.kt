package app.cash.paykit.core.utils

/**
 * Executes a lambda if the predicate [R] is null.
 *
 * Eg.: ```gotData?.(display).orElse { logError() }```
 */
internal inline fun <R> R?.orElse(block: () -> R): R {
  return this ?: block()
}
