package com.example.zhangwu.webdav

import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * WebDAV 客户端：基于 OkHttp 实现，支持坚果云
 * 支持：创建目录(MKCOL)、上传(PUT)、下载(GET)、删除(DELETE)、检查存在(PROPFIND)
 */
class WebDavClient(private val config: WebDavConfig) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val authHeader: String by lazy {
        Credentials.basic(config.username, config.password)
    }

    /** 拼接远程完整 URL：serverUrl + remotePath + fileName */
    private fun fullUrl(fileName: String = ""): String {
        val base = config.serverUrl.trimEnd('/')
        val path = config.remotePath.trimEnd('/')
        return if (fileName.isEmpty()) "$base$path/" else "$base$path/$fileName"
    }

    /** 创建远程目录（如已存在则视为成功） */
    fun mkcol(): WebDavResult<Unit> = run {
        // 先确保根路径，再创建子目录
        val rootUrl = config.serverUrl.trimEnd('/')
        val remotePath = config.remotePath.trim('/')
        val pathParts = remotePath.split("/").filter { it.isNotEmpty() }

        var currentPath = rootUrl
        for (part in pathParts) {
            currentPath = "$currentPath/$part"
            val req = Request.Builder()
                .url("$currentPath/")
                .header("Authorization", authHeader)
                .method("MKCOL", null)
                .build()
            client.newCall(req).execute().use { resp ->
                // 201 创建成功，405 已存在，都算成功
                if (resp.code !in setOf(201, 405)) {
                    return WebDavResult.Error("MKCOL 失败: HTTP ${resp.code}")
                }
            }
        }
        WebDavResult.Success(Unit)
    }

    /** 上传文件到远程 */
    fun upload(fileName: String, content: ByteArray): WebDavResult<Unit> = run {
        // 先确保目录存在
        when (val r = mkcol()) {
            is WebDavResult.Error -> return r
            is WebDavResult.Success -> {}
        }
        val req = Request.Builder()
            .url(fullUrl(fileName))
            .header("Authorization", authHeader)
            .header("Content-Type", "application/octet-stream")
            .put(content.toRequestBody("application/octet-stream".toMediaType()))
            .build()
        client.newCall(req).execute().use { resp ->
            if (resp.code in setOf(200, 201, 204)) {
                WebDavResult.Success(Unit)
            } else {
                WebDavResult.Error("上传失败: HTTP ${resp.code}")
            }
        }
    }

    /** 下载远程文件 */
    fun download(fileName: String): WebDavResult<ByteArray> = run {
        val req = Request.Builder()
            .url(fullUrl(fileName))
            .header("Authorization", authHeader)
            .get()
            .build()
        client.newCall(req).execute().use { resp ->
            if (resp.code == 200) {
                WebDavResult.Success(resp.body?.bytes() ?: ByteArray(0))
            } else {
                WebDavResult.Error("下载失败: HTTP ${resp.code}")
            }
        }
    }

    /** 删除远程文件 */
    fun delete(fileName: String): WebDavResult<Unit> = run {
        val req = Request.Builder()
            .url(fullUrl(fileName))
            .header("Authorization", authHeader)
            .delete()
            .build()
        client.newCall(req).execute().use { resp ->
            if (resp.code in setOf(200, 204, 404)) {
                WebDavResult.Success(Unit)
            } else {
                WebDavResult.Error("删除失败: HTTP ${resp.code}")
            }
        }
    }

    /** 检查远程文件是否存在 */
    fun exists(fileName: String): WebDavResult<Boolean> = run {
        val req = Request.Builder()
            .url(fullUrl(fileName))
            .header("Authorization", authHeader)
            .header("Depth", "0")
            .method("PROPFIND", "".toRequestBody())
            .build()
        client.newCall(req).execute().use { resp ->
            if (resp.code == 207) WebDavResult.Success(true)
            else if (resp.code == 404) WebDavResult.Success(false)
            else WebDavResult.Error("检查存在失败: HTTP ${resp.code}")
        }
    }

    /** 测试连接：尝试 PROPFIND 根目录 */
    fun testConnection(): WebDavResult<Unit> = run {
        val req = Request.Builder()
            .url(config.serverUrl.trimEnd('/') + "/")
            .header("Authorization", authHeader)
            .header("Depth", "0")
            .method("PROPFIND", "".toRequestBody())
            .build()
        client.newCall(req).execute().use { resp ->
            if (resp.code == 207) WebDavResult.Success(Unit)
            else if (resp.code == 401) WebDavResult.Error("账号或密码错误 (HTTP 401)")
            else WebDavResult.Error("连接失败: HTTP ${resp.code}")
        }
    }
}

/** WebDAV 操作结果 */
sealed class WebDavResult<out T> {
    data class Success<T>(val data: T) : WebDavResult<T>()
    data class Error(val message: String) : WebDavResult<Nothing>()
}
