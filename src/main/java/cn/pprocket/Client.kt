package cn.pprocket

import cn.pprocket.items.Comment
import cn.pprocket.items.Game
import cn.pprocket.items.Post
import cn.pprocket.items.Topic
import cn.pprocket.items.User

interface Client {
    fun login(cookie: String)
    fun getUser(heyId: String): User
    fun getPosts(topic: Topic): List<Post>
    fun getPost(id: String): Post
    fun getGame(id: String): Game
    fun getComments(postId: String, page: Int): List<Comment>
    fun reply(postId: String, text: String, rootId: String? = null)
    fun genQRCode(): String
    fun checkLogin(url:String): Boolean
    fun like(commentId: String)

}
