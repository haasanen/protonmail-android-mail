/*
 * Copyright (C) 2025 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.payment.presentation.model

import me.proton.android.core.payment.domain.model.ProductEntitlement
import me.proton.android.core.payment.domain.model.ProductHeader

data class Subscription(
    val header: ProductHeader,
    val entitlements: List<ProductEntitlement>,
    val additionalText: List<String>
) {

    companion object {

        val test = Subscription(
            header = ProductHeader(
                title = "Proton Unlimited",
                description = "Comprehensive privacy and security with all Proton services combined.",
                priceText = "CHF129.48",
                cycleText = "Per year",
                starred = false
            ),
            entitlements = listOf(
                ProductEntitlement.Progress(
                    startText = "Mail storage",
                    iconName = null,
                    endText = "100 MB of 500 MB",
                    min = 0,
                    max = 500,
                    current = 100
                ),
                ProductEntitlement.Progress(
                    startText = "Drive storage",
                    iconName = null,
                    endText = "260 MB of 500 MB",
                    min = 0,
                    max = 500,
                    current = 260
                ),
                ProductEntitlement.Progress(
                    startText = "Pass storage",
                    iconName = null,
                    endText = "480 MB of 500 MB",
                    min = 0,
                    max = 500,
                    current = 480
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "1 of 1 user",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "1 of 1 address",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "500 GB storage",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "And the free features of all other Proton products",
                    hint = ""
                )
            ),
            additionalText = listOf(
                "Your plan will automatically renew on 15 March 2025.",
                "Your subscription can't be managed inside mobile app."
            )
        )

        val test_pass2022 = Subscription(
            header = ProductHeader(
                title = "Pass Plus",
                description = "For next-level password management and identity protection.",
                priceText = "CHF0.99",
                cycleText = "Per month",
                starred = false
            ),
            entitlements = listOf(
                ProductEntitlement.Description(
                    iconName = null,
                    text = "Unlimited logins and notes",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "Unlimited devices",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "20 vaults",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "Share each vault with up to 10 people",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "Unlimited email aliases",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "Integrated 2FA authenticator",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "Custom fields",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "Priority support",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "And the free features of all other Proton products",
                    hint = ""
                )
            ),
            additionalText = listOf(
                "Your plan will automatically renew on 15 March 2025.",
                "Your subscription can't be managed inside mobile app."
            )
        )

        val test_free = Subscription(
            header = ProductHeader(
                title = "Proton Free",
                description = "Current plan",
                priceText = "",
                cycleText = "",
                starred = false
            ),
            entitlements = listOf(
                ProductEntitlement.Progress(
                    startText = "Mail storage",
                    iconName = null,
                    endText = "100 MB of 500 MB",
                    min = 0,
                    max = 500,
                    current = 100
                ),
                ProductEntitlement.Progress(
                    startText = "Drive storage",
                    iconName = null,
                    endText = "260 MB of 500 MB",
                    min = 0,
                    max = 500,
                    current = 260
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "1 of 1 user",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "1 of 1 address",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "1 of 3 calendar",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "Free VPN on single device",
                    hint = ""
                ),
                ProductEntitlement.Description(
                    iconName = null,
                    text = "And the free features of all other Proton products",
                    hint = ""
                )
            ),
            additionalText = emptyList()
        )
    }
}
