package ch.protonmail.android.mailcontact.presentation.contactdetails.mapper

import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import ch.protonmail.android.mailcontact.domain.model.ContactDetailAddress
import ch.protonmail.android.mailcontact.domain.model.PartialDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ContactDetailsUiModelMapperExtensionsKtTest {

    @Test
    fun `toFormattedAddress should return full formatted address`() {
        // Given
        val address = ContactDetailAddress(
            street = "123 Main St",
            postalCode = "12345",
            city = "Springfield",
            region = "IL",
            country = "USA",
            addressTypes = emptyList()
        )

        // When
        val result = address.toFormattedAddress()

        // Then
        assertEquals("123 Main St, 12345, Springfield, IL, USA", result)
    }

    @Test
    fun `toFormattedAddress should return address with only street`() {
        // Given
        val address = ContactDetailAddress(
            street = "123 Main St",
            postalCode = null,
            city = null,
            region = null,
            country = null,
            addressTypes = emptyList()
        )

        // When
        val result = address.toFormattedAddress()

        // Then
        assertEquals("123 Main St", result)
    }

    @Test
    fun `toFormattedAddress should return address with missing street`() {
        // Given
        val address = ContactDetailAddress(
            street = null,
            postalCode = "12345",
            city = "Springfield",
            region = null,
            country = null,
            addressTypes = emptyList()
        )

        // When
        val result = address.toFormattedAddress()

        // Then
        assertEquals("12345, Springfield", result)
    }

    @Test
    fun `toFormattedAddress should return address with only country`() {
        // Given
        val address = ContactDetailAddress(
            street = null,
            postalCode = null,
            city = null,
            region = null,
            country = "USA",
            addressTypes = emptyList()
        )

        // When
        val result = address.toFormattedAddress()

        // Then
        assertEquals("USA", result)
    }

    @Test
    fun `toFormattedAddress should use correct comma separation for mixed fields`() {
        // Given
        val address = ContactDetailAddress(
            street = null,
            postalCode = "98765",
            city = null,
            region = "California",
            country = "USA",
            addressTypes = emptyList()
        )

        // When
        val result = address.toFormattedAddress()

        // Then
        assertEquals("98765, California, USA", result)
    }

    @Test
    fun `should format full date with month, day, and year`() {
        // Given
        val date = PartialDate(day = 15, month = 3, year = 2020)

        // When
        val result = date.toFormattedPartialDate()

        // Then
        val expected = "${getMonthName(3)} 15, 2020"
        assertEquals(expected, result)
    }

    @Test
    fun `should format date with month and day only`() {
        // Given
        val date = PartialDate(day = 5, month = 7, year = null)

        // When
        val result = date.toFormattedPartialDate()

        // Then
        val expected = "${getMonthName(7)} 5"
        assertEquals(expected, result)
    }

    @Test
    fun `should format date with month and year only`() {
        // Given
        val date = PartialDate(day = null, month = 12, year = 1999)

        // When
        val result = date.toFormattedPartialDate()

        // Then
        val expected = "${getMonthName(12)} 1999"
        assertEquals(expected, result)
    }

    @Test
    fun `should format date with day and year only`() {
        // Given
        val date = PartialDate(day = 30, month = null, year = 2010)

        // When
        val result = date.toFormattedPartialDate()

        // Then
        val expected = "30, 2010"
        assertEquals(expected, result)
    }

    @Test
    fun `should format date with only month`() {
        // Given
        val date = PartialDate(day = null, month = 1, year = null)

        // When
        val result = date.toFormattedPartialDate()

        // Then
        val expected = getMonthName(1)
        assertEquals(expected, result)
    }

    @Test
    fun `should format date with only day`() {
        // Given
        val date = PartialDate(day = 8, month = null, year = null)

        // When
        val result = date.toFormattedPartialDate()

        // Then
        assertEquals("8", result)
    }

    @Test
    fun `should format date with only year`() {
        // Given
        val date = PartialDate(day = null, month = null, year = 2021)

        // When
        val result = date.toFormattedPartialDate()

        // Then
        assertEquals("2021", result)
    }

    @Test
    fun `should return empty string when all fields are null`() {
        // Given
        val date = PartialDate(day = null, month = null, year = null)

        // When
        val result = date.toFormattedPartialDate()

        // Then
        assertEquals("", result)
    }

    private fun getMonthName(month: Int): String = Month.of(month).getDisplayName(TextStyle.FULL, Locale.US)
}
