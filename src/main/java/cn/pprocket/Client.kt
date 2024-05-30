package cn.pprocket

import cn.pprocket.items.Game
import cn.pprocket.items.Post
import cn.pprocket.items.User

interface Client {
    fun login(cookie:String)
    fun getUSer(heyId : String): User
    fun getPosts() : List<Post>
    fun getPost(id: String) : Post
    fun getGame(id: String) : Game
}