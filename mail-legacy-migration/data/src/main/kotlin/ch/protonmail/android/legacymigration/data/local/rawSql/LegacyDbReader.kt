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

package ch.protonmail.android.legacymigration.data.local.rawSql

import androidx.sqlite.db.SupportSQLiteDatabase
import ch.protonmail.android.legacymigration.domain.model.LegacySessionInfo
import ch.protonmail.android.legacymigration.domain.model.LegacyUserAddressInfo
import ch.protonmail.android.legacymigration.domain.model.LegacyUserInfo
import jakarta.inject.Inject
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.AddressId

@Suppress("MultilineRawStringIndentation", "MagicNumber")
class LegacyDbReader @Inject constructor(
    private val db: SupportSQLiteDatabase,
    private val keyStoreCrypto: KeyStoreCrypto
) {

    fun readAuthenticatedSessions(): List<LegacySessionInfo> {
        val cursor = db.query(
            """
        SELECT s.sessionId, s.userId, s.refreshToken, d.twoPassModeEnabled
        FROM SessionEntity AS s
        LEFT JOIN SessionDetailsEntity AS d ON s.sessionId = d.sessionId
        WHERE s.userId IS NOT NULL AND s.userId != ''
            """.trimIndent()
        )

        val result = mutableListOf<LegacySessionInfo>()

        cursor.use {
            while (it.moveToNext()) {
                val sessionId = SessionId(it.getString(0))
                val userId = UserId(it.getString(1))
                val refreshTokenEncrypted = it.getString(2)
                val twoPass = it.getInt(3) != 0

                val refreshToken = refreshTokenEncrypted?.let { encryptedString ->
                    runCatching {
                        keyStoreCrypto.decrypt(encryptedString)
                    }.getOrNull()
                } ?: continue

                result += LegacySessionInfo(
                    userId = userId,
                    sessionId = sessionId,
                    refreshToken = refreshToken,
                    twoPassModeEnabled = twoPass
                )
            }
        }

        return result
    }

    fun readLegacySessionInfo(sessionId: SessionId): LegacySessionInfo? {
        val cursor = db.query(
            """
            SELECT s.userId, s.refreshToken, d.twoPassModeEnabled
            FROM SessionEntity AS s
            LEFT JOIN SessionDetailsEntity AS d ON s.sessionId = d.sessionId
            WHERE s.sessionId = ?
            """.trimIndent(),
            arrayOf(sessionId.id)
        )

        return cursor.use {
            if (it.moveToFirst()) {
                val userId = UserId(it.getString(0))
                val refreshTokenEncrypted = it.getString(1)
                val twoPass = it.getInt(2) != 0

                val refreshToken = refreshTokenEncrypted?.let { encryptedString ->
                    keyStoreCrypto.decrypt(encryptedString)
                } ?: return@use null

                LegacySessionInfo(
                    userId = userId,
                    sessionId = sessionId,
                    refreshToken = refreshToken,
                    twoPassModeEnabled = twoPass
                )
            } else null
        }
    }

    fun readLatestPrimaryUserId(): UserId? {
        val cursor = db.query(
            "SELECT userId FROM AccountMetadataEntity WHERE product = 'mail' ORDER BY primaryAtUtc DESC LIMIT 1"
        )
        return cursor.use {
            if (it.moveToFirst()) UserId(it.getString(0)) else null
        }
    }

    fun readLegacyUserInfo(userId: UserId): LegacyUserInfo? {
        val cursor = db.query(
            "SELECT passphrase, email, name, displayName FROM UserEntity WHERE userId = ?",
            arrayOf(userId.id)
        )
        return cursor.use {
            if (it.moveToFirst()) {
                val encryptedPassphrase = it.getBlob(0)
                val passPhrase = encryptedPassphrase?.let { bytes ->
                    runCatching {
                        String(keyStoreCrypto.decrypt(EncryptedByteArray(bytes)).array)
                    }.getOrNull()
                } ?: return@use null

                LegacyUserInfo(
                    userId = userId,
                    passPhrase = passPhrase,
                    email = it.getString(1),
                    name = it.getString(2),
                    displayName = it.getString(3)
                )
            } else null
        }
    }

    @Suppress("MaxLineLength")
    fun readPrimaryAddress(userId: UserId): LegacyUserAddressInfo? {
        val cursor = db.query(
            "SELECT addressId, email, displayName FROM AddressEntity WHERE userId = ? AND enabled = 1 ORDER BY `order` ASC LIMIT 1",
            arrayOf(userId.id)
        )
        return cursor.use {
            if (it.moveToFirst()) {
                LegacyUserAddressInfo(
                    userId = userId,
                    addressId = AddressId(it.getString(0)),
                    email = it.getString(1),
                    displayName = it.getString(2)
                )
            } else null
        }
    }
}
