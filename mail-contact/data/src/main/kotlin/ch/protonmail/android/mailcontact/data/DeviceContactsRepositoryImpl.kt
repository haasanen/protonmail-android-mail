/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.mailcontact.data

import android.content.Context
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import ch.protonmail.android.mailcontact.domain.model.DeviceContact
import ch.protonmail.android.mailcontact.domain.model.DeviceContactsWithSignature
import ch.protonmail.android.mailcontact.domain.repository.DeviceContactsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class DeviceContactsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : DeviceContactsRepository {

    private val cacheMutex = Mutex()
    private var allContactsCache: DeviceContactsWithSignature? = null

    override suspend fun getDeviceContacts(
        query: String
    ): Either<DeviceContactsRepository.DeviceContactsErrors, List<DeviceContact>> {
        @Suppress("TooGenericExceptionCaught")
        return withContext(ioDispatcher) {
            try {
                queryContacts(query).right()
            } catch (e: SecurityException) {
                Timber.e(e, "Failed to query contacts due to permission issue")
                DeviceContactsRepository.DeviceContactsErrors.PermissionDenied.left()
            } catch (e: Exception) {
                Timber.e(e, "Failed to query contacts")
                DeviceContactsRepository.DeviceContactsErrors.Unknown.left()
            }
        }
    }

    private fun queryContacts(query: String): List<DeviceContact> {
        // If the user searches for "_" or "%", they should be treated as literals.
        val escapedQuery = query
            .replace("_", "\\_")
            .replace("%", "\\%")

        val selectionArgs = arrayOf("%$escapedQuery%", "%$escapedQuery%", "%$escapedQuery%")

        return context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            ANDROID_PROJECTION,
            ANDROID_SELECTION,
            selectionArgs,
            ANDROID_ORDER_BY
        )?.use { cursor ->
            val contacts = mutableListOf<DeviceContact>()

            val displayNameIndex = cursor.getColumnIndex(
                ContactsContract.CommonDataKinds.Email.DISPLAY_NAME_PRIMARY
            ).takeIf { it >= 0 }

            val emailIndex = cursor.getColumnIndex(
                ContactsContract.CommonDataKinds.Email.ADDRESS
            ).takeIf { it >= 0 }

            for (position in 0 until cursor.count) {
                if (!cursor.moveToPosition(position)) continue
                val emailAddress = emailIndex?.let { cursor.getStringOrNull(it) } ?: continue

                // Fallback to email address if for some reason the display name can't be obtained
                val displayName = displayNameIndex?.let { cursor.getStringOrNull(it) } ?: emailAddress
                contacts.add(DeviceContact(name = displayName, email = emailAddress))
            }

            contacts
        } ?: emptyList()
    }

    /**
     * Returns all contacts. If [useCacheIfAvailable] is true and a cache exists,
     * the cached value is returned. Otherwise, it fetches via getDeviceContacts("")
     * and updates the cache on success.
     */
    override suspend fun getAllContacts(
        useCacheIfAvailable: Boolean
    ): Either<DeviceContactsRepository.DeviceContactsErrors, DeviceContactsWithSignature> {

        if (useCacheIfAvailable) {
            cacheMutex.withLock {
                allContactsCache?.let { return it.right() }
            }
        }

        return getDeviceContacts(query = "")
            .map { freshContacts ->
                val value = DeviceContactsWithSignature(
                    contacts = freshContacts,
                    signature = freshContacts.signature()
                )
                cacheMutex.withLock { allContactsCache = value }
                value
            }
    }


    companion object {

        private const val ANDROID_ORDER_BY = ContactsContract.CommonDataKinds.Email.DISPLAY_NAME_PRIMARY + " ASC"

        @Suppress("MaxLineLength")
        private val ANDROID_SELECTION = """
            |${ContactsContract.CommonDataKinds.Email.DISPLAY_NAME_PRIMARY} LIKE ? ESCAPE '\'
            |OR ${ContactsContract.CommonDataKinds.Email.ADDRESS} LIKE ? ESCAPE '\'
            |OR ${ContactsContract.CommonDataKinds.Email.DATA} LIKE ? ESCAPE '\'
        """.trimMargin()

        private val ANDROID_PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Email.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.DATA
        )
    }

}

@Suppress("MagicNumber")
fun List<DeviceContact>.signature(): Long =
    this.fold(0L) { acc, c -> acc + c.name.hashCode() + 31L * c.email.hashCode() }
