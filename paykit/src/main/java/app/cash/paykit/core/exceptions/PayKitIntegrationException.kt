package app.cash.paykit.core.exceptions

/**
 * This exception gets throw when an illegal operation is performed against the Cash App PayKit SDK.
 */
class PayKitIntegrationException(val description: String) : Exception(description)
