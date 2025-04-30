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

package ch.protonmail.android.mailsession.data.keychain

import ch.protonmail.android.mailsession.domain.coroutines.KeyChainScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import timber.log.Timber
import uniffi.proton_mail_uniffi.OsKeyChain
import uniffi.proton_mail_uniffi.OsKeyChainEntryKind
import javax.inject.Inject

class AndroidKeyChain @Inject constructor(
    private val keyChainLocalDataSource: KeyChainLocalDataSource,
    private val keyStoreCrypto: KeyStoreCrypto,
    @KeyChainScope private val coroutineScope: CoroutineScope
) : OsKeyChain {

    override fun delete(kind: OsKeyChainEntryKind) {
        coroutineScope.launch {
            keyChainLocalDataSource.remove(kind)
        }
    }

    override fun load(kind: OsKeyChainEntryKind): String? = runBlocking(coroutineScope.coroutineContext) {
        keyChainLocalDataSource.get(kind)
            .onLeft { Timber.e("android-keychain: failed to read secret from data source") }
            .map {
                keyStoreCrypto.decrypt(it)
            }
    }.getOrNull()

    override fun store(kind: OsKeyChainEntryKind, key: String) {
        coroutineScope.launch {
            val encryptedString = keyStoreCrypto.encrypt(key)
            keyChainLocalDataSource.save(kind, encryptedString)
        }
    }

}
