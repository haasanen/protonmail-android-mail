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

package ch.protonmail.android.mailmessage.data.remote

import ch.protonmail.android.mailmessage.data.remote.resource.MarkMessageAsReadBody
import ch.protonmail.android.mailmessage.data.remote.resource.MarkMessageAsUnreadBody
import ch.protonmail.android.mailmessage.data.remote.resource.MessageActionBody
import ch.protonmail.android.mailmessage.data.remote.response.MarkReadResponse
import ch.protonmail.android.mailmessage.data.remote.response.MarkUnreadResponse
import ch.protonmail.android.mailmessage.data.remote.response.PutLabelResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Query

interface MessageApi : BaseRetrofitApi {

    @PUT("mail/v4/messages/label")
    suspend fun addLabel(@Body messageActionBody: MessageActionBody): PutLabelResponse

    @PUT("mail/v4/messages/unlabel")
    suspend fun removeLabel(@Body messageActionBody: MessageActionBody): PutLabelResponse

    @PUT("mail/v4/messages/unread")
    suspend fun markAsUnread(@Body markUnreadBody: MarkMessageAsUnreadBody): MarkUnreadResponse

    @PUT("mail/v4/messages/read")
    suspend fun markAsRead(@Body markReadBody: MarkMessageAsReadBody): MarkReadResponse

    @PUT("mail/v4/messages/delete")
    suspend fun deleteMessages(@Body messageActionBody: MessageActionBody): PutLabelResponse

    @DELETE("mail/v4/messages/empty")
    suspend fun emptyLabel(@Query("LabelID") labelId: String): ResponseBody
}
