package com.squareup.cash.paykit.models.response


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthFlowTriggers(
    @Json(name = "mobile_url")
    val mobileUrl: String,
    @Json(name = "qr_code_image_url")
    val qrCodeImageUrl: String,
    @Json(name = "qr_code_svg_url")
    val qrCodeSvgUrl: String,
    @Json(name = "refreshes_at")
    val refreshesAt: String
)