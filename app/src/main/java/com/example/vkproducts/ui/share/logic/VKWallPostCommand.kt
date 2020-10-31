package com.example.vkproducts.ui.share.logic

import android.net.Uri
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKHttpPostCall
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VKWallPostCommand(
    private val message: String? = null,
    private val photos: List<Uri> = listOf(),
    private val ownerId: Int = 0,
    private val friendsOnly: Boolean = false,
    private val fromGroup: Boolean = false
) : ApiCommand<Int>() {
    override fun onExecute(manager: VKApiManager): Int {
        val callBuilder = VKMethodCall.Builder()
            .method("wall.post")
            .args("friends_only", if (friendsOnly) 1 else 0)
            .args("from_group", if (fromGroup) 1 else 0)
            .version("5.103")
        message?.let {
            callBuilder.args("message", it)
        }

        if (ownerId != 0) {
            callBuilder.args("owner_id", ownerId)
        }

        println("here photos : $photos")

        if (photos.isNotEmpty()) {
            val uploadInfo = getServerUploadInfo(manager)
            val attachments = photos.map { uploadPhoto(it, uploadInfo, manager) }

            println("atachments : ${attachments.joinToString(",")}")
            callBuilder.args("attachments", attachments.joinToString(","))
        }

        return manager.execute(callBuilder.build(), ResponseApiParser())
    }

    private fun getServerUploadInfo(manager: VKApiManager): VKServerUploadInfo {
        val uploadInfoCall = VKMethodCall.Builder()
            .method("photos.getWallUploadServer")
            .version("5.103")
            .build()
        return manager.execute(uploadInfoCall, ServerUploadInfoParser())
    }

    private fun uploadPhoto(uri: Uri, serverUploadInfo: VKServerUploadInfo, manager: VKApiManager): String {
        println("uploadPhoto here")
        println("url : ${serverUploadInfo.uploadUrl} : ${uri}")
        val fileUploadCall = VKHttpPostCall.Builder()
            .url(serverUploadInfo.uploadUrl)
            .args("photo", uri, "image.jpg")
            .timeout(TimeUnit.SECONDS.toMillis(15))
            .retryCount(1)
            .build()


        val fileUploadInfo = manager.execute(fileUploadCall, null, FileUploadInfoParser())
        println("Here 1")

        val saveCall = VKMethodCall.Builder()
            .method("photos.saveWallPhoto")
            .args("server", fileUploadInfo.server)
            .args("photo", fileUploadInfo.photo)
            .args("hash", fileUploadInfo.hash)
            .version("5.103")
            .build()

        println("here 2")

        val saveInfo = manager.execute(saveCall, SaveInfoParser())

        return saveInfo.getAttachment()
    }

    private class ResponseApiParser : VKApiResponseParser<Int> {
        override fun parse(response: String): Int {
            try {
                return JSONObject(response).getJSONObject("response").getInt("post_id")
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }

    private class ServerUploadInfoParser : VKApiResponseParser<VKServerUploadInfo> {
        override fun parse(response: String): VKServerUploadInfo {
            try {
                val joResponse = JSONObject(response).getJSONObject("response")
                return VKServerUploadInfo(
                    uploadUrl = joResponse.getString("upload_url"),
                    albumId = joResponse.getInt("album_id"),
                    userId = joResponse.getInt("user_id")
                )
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }

    private class FileUploadInfoParser : VKApiResponseParser<VKFileUploadInfo> {
        override fun parse(response: String): VKFileUploadInfo {
            println("here parse")
            try {
                val joResponse = JSONObject(response)
                return VKFileUploadInfo(
                    server = joResponse.getString("server"),
                    photo = joResponse.getString("photo"),
                    hash = joResponse.getString("hash")
                )
            } catch (ex: JSONException) {
                println("exception : ${ex.message}")
                throw VKApiIllegalResponseException(ex)
            }
        }
    }

    private class SaveInfoParser : VKApiResponseParser<VKSaveInfo> {
        override fun parse(response: String): VKSaveInfo {
            try {
                val joResponse = JSONObject(response).getJSONArray("response").getJSONObject(0)
                return VKSaveInfo(
                    id = joResponse.getInt("id"),
                    albumId = joResponse.getInt("album_id"),
                    ownerId = joResponse.getInt("owner_id")
                )
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}