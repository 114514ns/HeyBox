package cn.pprocket.items

import cn.pprocket.HeyClient.domain
import cn.pprocket.HeyClient.get
import cn.pprocket.HeyClient.parseComment
import cn.pprocket.utils.ParamsBuilder
import kotlinx.serialization.json.*

class Comment {
    var content: String = ""
    var userId: String = ""
    var commentId: String = ""
    var userName: String = ""
    var userAvatar: String = ""
    var hasMore = false
    var replyName: String = "null"
    var replyId: String = ""
    var createdAt: String = ""
    var images: List<String> = emptyList()
    var likes = 0
    var postId: String = ""
    var subComments: MutableList<Comment> = mutableListOf()
    var extraPost: Post = Post()
    var liked = false
    var postTitle: String = ""

    suspend fun fillSubComments(): List<Comment>? {
        if (!hasMore) return subComments
        val params = mapOf(
            "root_comment_id" to commentId!!,
            "lastval" to subComments!![subComments!!.size - 1].commentId!!
        )
        val url =
            "${domain}/bbs/app/comment/sub/comments?" + ParamsBuilder(params).build("/bbs/app/comment/sub/comments/")
        val string = get(url)
        val array = Json.decodeFromString<JsonObject>(string)["comments"]!!.jsonArray
        array.forEach {
            val comment = parseComment(it.jsonObject)
            subComments!!.add(comment)
        }
        hasMore = Json.decodeFromString<JsonObject>(string)["has_more"]!!.jsonPrimitive.boolean
        this.subComments.distinctBy { it.commentId }
        subComments!!.forEach { ele: Comment -> ele.postId = postId }

        return this.subComments
    }

}
