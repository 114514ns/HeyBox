package cn.pprocket

import cn.pprocket.items.*

interface Client {
    suspend fun login(cookie: String)
    suspend fun getUser(heyId: String): User
    suspend fun getPosts(topic: Topic): List<Post>
    suspend fun getPost(id: String): Post
    suspend fun getGame(id: String): Game
    suspend fun getComments(postId: String, page: Int): List<Comment>
    suspend fun reply(postId: String, text: String, rootId: String? = null,images: List<String> = emptyList())
    suspend fun genQRCode(): String
    suspend fun checkLogin(url:String): Boolean
    suspend fun like(commentId: String)
    suspend fun uploadImage():String

}
