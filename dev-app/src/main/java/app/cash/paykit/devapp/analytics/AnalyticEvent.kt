package app.cash.paykit.devapp.analytics

import app.cash.paykit.analytics.core.Deliverable

class AnalyticEvent(
    importantData: String
) : Deliverable {

    override val content = "{'data':'$importantData'}"
    override val metaData = null

    override val type = TYPE

    companion object {
        const val TYPE = "ANALYTIC_EVENT"
    }
}