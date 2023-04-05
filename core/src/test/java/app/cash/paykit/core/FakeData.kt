/*
 * Copyright (C) 2023 Cash App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.paykit.core

import app.cash.paykit.core.models.sdk.CashAppPayCurrency.USD
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction.OneTimeAction

object FakeData {
  const val CLIENT_ID = "fake_client_id"
  const val BRAND_ID = "fake_brand_id"
  const val REDIRECT_URI = "fake_redirect_uri"
  const val FAKE_AMOUNT = 500

  val oneTimePayment = OneTimeAction(USD, FAKE_AMOUNT, BRAND_ID)

  val validCreateCustomerJSONresponse = """{
         "request":{
            "id":"GRR_dvm2v6b6wkdrwhcaqefx6tnp",
            "status":"PENDING",
            "actions":[
               {
                  "type":"ONE_TIME_PAYMENT",
                  "amount":500,
                  "currency":"USD",
                  "scope_id":"BRAND_9kx6p0mkuo97jnl025q9ni94t"
               }
            ],
            "origin":{
               "type":"DIRECT"
            },
            "auth_flow_triggers":{
               "qr_code_image_url":"https://sandbox.api.cash.app/qr/sandbox/v1/GRR_dvm2v6b6wkdrwhcaqefx6tnp-n3nf7z?rounded=0&logoColor=0000ff&format=png",
               "qr_code_svg_url":"https://sandbox.api.cash.app/qr/sandbox/v1/GRR_dvm2v6b6wkdrwhcaqefx6tnp-n3nf7z?rounded=0&logoColor=0000ff&format=svg",
               "mobile_url":"https://sandbox.api.cash.app/sandbox/v1/GRR_dvm2v6b6wkdrwhcaqefx6tnp-n3nf7z?method=mobile_url&type=cap",
               "refreshes_at":"2023-02-08T21:01:09.077Z"
            },
            "created_at":"2023-02-08T21:00:39.105Z",
            "updated_at":"2023-02-08T21:00:39.105Z",
            "expires_at":"2023-02-08T22:00:39.077Z",
            "requester_profile":{
               "name":"SDK Hacking: The Brand",
               "logo_url":"defaultlogo.jpg"
            },
            "channel":"IN_APP"
         }
      }"""
}
