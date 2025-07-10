package me.proton.android.core.payment.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ProductEntitlementTest {

    @Test
    fun `progress entitlement normalized progress`() {
        assertEquals(
            0.5,
            makeProgressEntitlement(min = 0, max = 100, current = 50).normalizedProgress
        )
        assertEquals(
            0.0,
            makeProgressEntitlement(min = 0, max = 100, current = 0).normalizedProgress
        )
        assertEquals(
            1.0,
            makeProgressEntitlement(min = 0, max = 100, current = 100).normalizedProgress
        )
        assertEquals(
            0.5,
            makeProgressEntitlement(min = 50, max = 60, current = 55).normalizedProgress
        )
        assertEquals(
            0.5,
            makeProgressEntitlement(min = -50, max = 50, current = 0).normalizedProgress
        )
        assertEquals(
            0.5,
            makeProgressEntitlement(min = Long.MIN_VALUE, max = Long.MAX_VALUE, current = 0).normalizedProgress
        )
    }

    private fun makeProgressEntitlement(
        min: Long,
        max: Long,
        current: Long
    ) = ProductEntitlement.Progress(
        startText = "",
        iconName = "",
        endText = "",
        min = min,
        max = max,
        current = current
    )
}
