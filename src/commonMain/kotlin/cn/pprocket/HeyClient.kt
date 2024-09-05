package cn.pprocket

import cn.pprocket.items.*
import cn.pprocket.utils.Encrypt
import cn.pprocket.utils.ParamsBuilder
import cn.pprocket.utils.app.AppParamsBuilder
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration


object HeyClient : Client {
    var cookie: String = ""
    var scriptContent = ""
    var user = User()
    var ktorClient = Platform.getKtorClient()
    override suspend fun login(cookie: String) {
        this.cookie = cookie
    }

    override suspend fun getUser(heyId: String): User {
        val user = User()
        val params = mutableMapOf(
            "userid" to heyId,
        )
        val str =
            get("https://api.xiaoheihe.cn/bbs/app/profile/user/profile?${ParamsBuilder(params).build("/bbs/app/profile/user/profile/")}")
        val obj = Json.decodeFromString<JsonObject>(str)["account_detail"]!!.jsonObject
        user.userName = obj.get("username")!!.jsonPrimitive.content
        user.avatar = obj.get("avatar")!!.jsonPrimitive.content
        user.level = obj["level_info"]!!.jsonObject["level"]!!.jsonPrimitive.int
        val bbs = obj["bbs_info"]!!.jsonObject
        user.posts = bbs.get("post_link_num")!!.jsonPrimitive.int
        user.location = obj.get("ip_location")!!.jsonPrimitive.content
        user.followings = bbs.get("follow_num")!!.jsonPrimitive.int
        user.signature = obj.get("signature")!!.jsonPrimitive.content
        user.followers = bbs.get("fan_num")!!.jsonPrimitive.int
        user.userId = obj.get("userid")!!.jsonPrimitive.content

        return user
    }

    suspend fun User.fetchComments(page: Int): List<Comment> {
        val params = mapOf(
            "userid" to this.userId,
            "limit" to "20",
            "offset" to ((page - 1) * 20).toString(),
        )
        val url =
            "https://api.xiaoheihe.cn/bbs/web/profile/post/comments?${ParamsBuilder(params).build("/bbs/web/profile/post/comments/")}"
        val res = get(url)
        val comments = mutableListOf<Comment>()
        Json.decodeFromString<JsonObject>(res)["result"]!!.jsonArray.forEach {
            var obj = it.jsonObject
            val comment = Comment()
            comment.commentId = obj.get("comment_id")!!.jsonPrimitive.content
            comment.createdAt = parseTime(obj.get("create_at")!!.jsonPrimitive.content.replace(".0", "").toLong() * 1000)
            comment.content = obj.get("text")!!.jsonPrimitive.content
            comment.replyId = try {
                obj.get("root_comment_id")!!.jsonPrimitive.content
            } catch (_: Exception) {
                ""
            }
            var link = obj.get("link")!!.jsonObject
            val post = Post()
            post.postId = link.get("id")!!.jsonPrimitive.content
            post.title = link.get("title")!!.jsonPrimitive.content
            post.images = mutableListOf<String>()
            link["thumbs"]!!.jsonArray.forEach {
                post.images.add(it.jsonPrimitive.content)
            }
            comment.userAvatar = this.avatar
            comment.userName = this.userName
            comment.extraPost = post
            comment.postId = post.postId
            comment.postTitle = post.title
            comments.add(comment)
        }
        return comments
    }

    suspend fun User.fetchPosts(page: Int): List<Post> {
        val params = mapOf(
            "userid" to this.userId.toString(),
            "limit" to "20",
            "offset" to ((page - 1) * 20).toString(),
            "post_type" to "1",
        )
        val url =
            "https://api.xiaoheihe.cn/bbs/web/profile/post/links?${ParamsBuilder(params).build("/bbs/web/profile/post/links/")}"
        val res = get(url)
        val posts = mutableListOf<Post>()
        Json.decodeFromString<JsonObject>(res)["post_links"]!!.jsonArray.forEach {
            val obj = it.jsonObject
            val post = parsePost(obj)
            posts.add(post)
        }
        return posts
    }
    suspend fun Post.fillContent() : String {
        val params = mapOf(
            "link_id" to this.postId,
            "page" to "1",
            "limit" to "10"
        )
        val url = "https://api.xiaoheihe.cn/bbs/app/link/tree?${ParamsBuilder(params).build("/bbs/app/link/tree/")}"
        val res = get(url)
        val obj = Json.decodeFromString<JsonObject>(res).jsonObject.get("link")!!.jsonObject
        val builder = StringBuilder()
        Json.decodeFromString<JsonArray>(obj.get("text")!!.jsonPrimitive.content).jsonArray.forEach {
            val obj = it.jsonObject
            if (obj["type"]!!.jsonPrimitive.content == "text") {
                builder.append(obj.get("text")!!.jsonPrimitive.content).append("\n")
            }
        }
        return builder.toString()
    }
    suspend fun Post.renderHTML() :List<Tag> {
        val tags = mutableListOf<Tag>()
        val params = mapOf(
            "link_id" to this.postId,
            "return_json" to "1",
            "index" to "1"
        )
        val url = "https://api.xiaoheihe.cn/bbs/app/link/web/view?${ParamsBuilder(params).build("/bbs/app/link/web/view/")}"
        val str = get(url)
        val obj = Json.decodeFromString<JsonObject>(str).jsonObject.get("link")!!.jsonObject
        val raw = obj["content"]!!.jsonArray[0].jsonObject["text"]!!.jsonPrimitive.content
        val elements = Ksoup.parse(raw).getElementsByTag("body")[0].children()
        elements.forEach {
            var ele = it
            val tag = Tag()
            if (ele.childNodes().size != 0) {
                if (!ele.getElementsByTag("h3").isEmpty() ||
                    !ele.getElementsByTag("h2").isEmpty()
                ) {
                    tag.tagType = "title"
                    tag.tagValue = ele.text()
                } else if (!ele.getElementsByTag("img").isEmpty()) {
                    val img  = ele.getElementsByTag("img")[0]
                    if (img.hasAttr("data-original")) {
                        tag.tagType = "image"
                        tag.tagValue = img.attr("data-original")
                    } else {
                        tag.tagType = "gameCard"
                        tag.tagValue = img.attr("data-gameid")
                    }
                } else if (ele.childNodes()[0] is com.fleeksoft.ksoup.nodes.TextNode) {
                    tag.tagType = "text"
                    tag.tagValue = ele.text()
                } /*else if (ele instanceof TextNode) {
                    tag.setTagType("text");
                    tag.setTagValue(ele.text());
                } */ else if (ele.getElementsByTag("b").size != 0 && ele.childNodes().size > 1) {
                } else if (!ele.getElementsByTag("a").isEmpty()) {
                    tag.tagType = "link"
                    tag.tagValue = ele.attr("href")
                } else if (!ele.getElementsByTag("blockquote").isEmpty()) {
                    tag.tagType = "ref"
                    tag.tagValue = ele.text()
                }
                tags.add(tag)
            }
        }
        return tags
    }

    override suspend fun getPosts(topic: Topic): List<Post> {
        val id = topic.id
        val posts = mutableListOf<Post>()
        var res = ""
        if (topic.id > 0) {
            val map = mapOf(
                "topic_id" to id.toString(),
                "offset" to "20",
                "limit" to "20",
                "sort_filter" to "hot-rank"
            )
            val url =
                "https://api.xiaoheihe.cn/bbs/app/topic/feeds?${ParamsBuilder(map).build("/bbs/app/topic/feeds/")}"
            res = get(url)
        } else if (topic.id == -1) {
            val params = mapOf(
                "offset" to "0",
                "limit" to "30",
                "tag" to "-1",
                "rec_mark" to "timeline",
                "news_list_type" to "normal",
                "is_first" to "0",
                "news_list_group" to "control-group",
            )
            val url =
                "https://api.xiaoheihe.cn/bbs/app/feeds/news?${ParamsBuilder(params).build("/bbs/app/feeds/news/")}"
            res = get(url)
        } else {
            val params = mapOf(
                "pull" to "1",
                "use_history" to "0",
                "last_pull" to "1",
                "is_first" to "0"
            )
            val url = "https://api.xiaoheihe.cn/bbs/app/feeds?${AppParamsBuilder(params).build("/bbs/app/feeds/")}"

            res = get(url)
        }
        Json.decodeFromString<JsonObject>(res)["links"]!!.jsonArray.forEach {
            if (!it.toString().substring(0, 25).contains("banner")) {
                val obj = it.jsonObject
                val post = parsePost(obj)
                if (topic.id == -1) {
                    post.isHTML = true
                }
                if (post.title != "") {
                    posts.add(post)
                }
            }
        }
        return posts
    }

    fun parsePost(obj: JsonObject):Post {
        val post = Post()
        if (!obj.containsKey("linkid")) {
            return post
        }
        post.title = Ksoup.parse(obj.get("title")!!.jsonPrimitive.content).text()

        post.postId = obj.get("linkid")!!.jsonPrimitive.content
        post.userId = obj.get("userid")!!.jsonPrimitive.content
        if (obj.containsKey("user")) {
            post.userAvatar = obj.get("user")!!.jsonObject.get("avatar")!!.jsonPrimitive.content
            post.userName = obj.get("user")!!.jsonObject.get("username")!!.jsonPrimitive.content
        } else {
            post.userAvatar = "https://avatars.akamai.steamstatic.com/6a477d65670b03bae1c5f48988211ff0366c6a8c_full.jpg"
            post.userName = "狗熊岭军师熊二"
        }
        post.description = Ksoup.parse(obj.get("description")!!.jsonPrimitive.content).text()
        val list = mutableListOf<String>()

        if (obj.containsKey("imgs")) {
            obj.get("imgs")!!.jsonArray.forEach {
                list.add(it.jsonPrimitive.content.replace("webp", "jpg"))
            }
        } else {
            Json.decodeFromString<JsonArray>(obj.get("text")!!.jsonPrimitive.content).forEach {
                var o = it.jsonObject
                if (o.containsKey("url")) {
                    list.add(o.get("url")!!.jsonPrimitive.content)
                }
            }
        }
        post.images = list
        if (obj.containsKey("create_str")) {
            post.createAt = obj.get("create_str")!!.jsonPrimitive.content
        }
        if (obj.containsKey("formated_time")) {
            post.createAt = obj.get("formated_time")!!.jsonPrimitive.content
        }
        if (obj.containsKey("create_at")) {
            post.createAt = parseTime(obj.get("create_at")!!.jsonPrimitive.content.toLong() * 1000)
        }
        post.tags = mutableListOf()

        if (obj.containsKey("topics")) {
            obj["topics"]!!.jsonArray.forEach {
                val topic = Topic(
                    id = it.jsonObject.get("topic_id")!!.jsonPrimitive.int,
                    name = it.jsonObject.get("name")!!.jsonPrimitive.content,
                    icon = it.jsonObject.get("pic_url")!!.jsonPrimitive.content
                )
                post.tags.add(topic)

            }
        }

        post.tags.clear()
        post.tags.addAll(post.tags.distinctBy { it })

        post.comments = obj.get("comment_num")!!.jsonPrimitive.int
        post.likes = obj.get("link_award_num")!!.jsonPrimitive.int
        return post
    }

    override suspend fun getPost(id: String): Post {
        val params = mapOf(
            "link_id" to id,
            "page" to "1",
            "limit" to "10"
        )
        val url =
            "https://api.xiaoheihe.cn/bbs/app/link/tree/?${ParamsBuilder(params).build("/bbs/app/link/tree/")}"
        val res = get(url)
        val post = parsePost(Json.decodeFromString<JsonObject>(res)["link"]!!.jsonObject)
        return post
    }


    override suspend fun getGame(id: String): Game {
        val game = Game()
        return game
    }


    override suspend fun getComments(postId: String, page: Int): List<Comment> {
        val comments = mutableListOf<Comment>()
        val map = mapOf(
            "link_id" to postId,
            "page" to "${page.toString()}",
            "limit" to "10",
            "sort_filter" to "hot",
        )
        val url =
            "https://api.xiaoheihe.cn/bbs/app/link/tree?${ParamsBuilder(map).build("/bbs/app/link/tree/")}"
        val str = get(url)
        Json.decodeFromString<JsonObject>(str).get("comments")!!.jsonArray.forEach {
            var isFirst = true
            var comment = Comment()
            val sub = mutableListOf<Comment>()
            it.jsonObject.get("comment")!!.jsonArray.forEach {
                var comment1 = parseComment(it.jsonObject)
                if (isFirst) {
                    isFirst = false;
                    comment = comment1
                } else {
                    sub.add(comment1)
                }
            }
            comment.subComments = sub
            comments.add(comment)
        }
        comments.forEach {
            it.subComments!!.forEach {
                it.postId = postId
            }
            it.postId = postId
        }
        return comments
    }

    override suspend fun reply(postId: String, text: String, rootId: String?, images: List<String>) {
        var img = ""
        images.forEach {
            img += "${it};"
        }
        img = if (img.isEmpty()) "" else img.substring(0, img.length - 1)
        val params = mapOf<String, String>()
        val url =
            "https://api.xiaoheihe.cn/bbs/app/comment/create?${ParamsBuilder(params).build("/bbs/app/comment/create/")}"


            val res = ktorClient.submitForm(
                url,
                parameters {
                    append("link_id",postId)
                    append("text",text)
                    append("root_id",rootId ?: "-1")
                    append("reply_id",rootId ?: "-1")
                    append("imgs", img)
                    append("is_cy", "0")
                }
            ).bodyAsText()
            res
    }

    override suspend fun genQRCode(): String {
        val params = mapOf<String, String>()
        val url =
            "https://api.xiaoheihe.cn/account/get_qrcode_url?${ParamsBuilder(params).build("/account/get_qrcode_url/")}"
        val str = get(url)
        return Json.decodeFromString<JsonObject>(str)["qr_url"]!!.jsonPrimitive.content
    }

    fun parseComment(json: JsonObject): Comment {
        var comment = Comment()
        val userObject = json.get("user")!!.jsonObject
        comment.userId = userObject.get("userid")!!.jsonPrimitive.content
        comment.userName = userObject.get("username")!!.jsonPrimitive.content
        comment.userAvatar = userObject.get("avatar")!!.jsonPrimitive.content
        comment.content = Ksoup.parse(json.get("text")!!.jsonPrimitive.content).text()
        comment.commentId = json.get("commentid")!!.jsonPrimitive.content
        try {
            comment.replyId = json.get("replyuser")!!.jsonObject.get("userid")!!.jsonPrimitive.content
        } catch (_: Exception) {
        }
        try {
            comment.replyName = json.get("replyuser")!!.jsonObject.get("username")!!.jsonPrimitive.content
        } catch (_: Exception) {
        }
        try {
            comment.hasMore
            comment.hasMore = json.get("has_more")!!.jsonPrimitive.int == 1
        } catch (_: Exception) {
        }
        val images = emptyList<String>().toMutableList()
        if (json.containsKey("imgs")) {
            json["imgs"]!!.jsonArray.forEach {
                var obj = it.jsonObject
                images.add(obj.get("url")!!.jsonPrimitive.content)
            }
        }
        comment.likes = json.get("up")!!.jsonPrimitive.int
        comment.images = images
        comment.createdAt = parseTime(json.get("create_at")!!.jsonPrimitive.content.toLong() * 1000)
        if (json.containsKey("is_support")) {
            comment.liked = json.get("is_support")!!.jsonPrimitive.content == "1"
        }

        return comment

    }

    @OptIn(InternalAPI::class)
    override suspend fun uploadImage(): String {


        val params = mapOf<String, String>()
        val content =
            "{\"type\":\"pic\",\"source\":\"post_img\",\"upload_infos\":[{\"ext\":\"png\",\"file_size\":1.8277034759521484}]}"


        val url =
            "https://chat.xiaoheihe.cn/chatroom/v2/common/cos/upload/token?${ParamsBuilder(params).build("/chatroom/v2/common/cos/upload/token/")}"
        var str = ktorClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(content)
            }.bodyAsText()


        val obj = Json.decodeFromString<JsonObject>(str)["result"]!!.jsonObject["info"]!!.jsonObject
        val token = obj["token"]!!.jsonObject
        val credentials = token["credentials"]!!.jsonObject
        val startTime = token.get("startTime")!!.jsonPrimitive.int
        val expiredTime = token.get("expiredTime")!!.jsonPrimitive.int
        val sessionToken = credentials["sessionToken"]!!.jsonPrimitive.content
        val tmpSecretId = credentials["tmpSecretId"]!!.jsonPrimitive.content
        val tmpSecretKey = credentials["tmpSecretKey"]!!.jsonPrimitive.content
        val path =
            "https://${obj["bucket"]!!.jsonPrimitive.content}.cos.ap-shanghai.myqcloud.com${obj["key"]!!.jsonPrimitive.content}"

        val byteArray: ByteArray? = Platform.getClipImage()


        val signKey = Encrypt.HMAC_SHA1("${startTime};${expiredTime}", tmpSecretKey)

        val httpStringBuilder = StringBuilder()
        httpStringBuilder.append("put\n")
        httpStringBuilder.append(getPathFromUrl(path)).append("\n\n")
        httpStringBuilder.append("content-length=${byteArray!!.size}&host=chat-1251007209.cos.ap-shanghai.myqcloud.com")
            .append("\n")

        val httpString = httpStringBuilder.toString()
        val StringToSign = "sha1\n${startTime};${expiredTime}\n${Encrypt.SHA1(httpString)}\n"

        val sign = Encrypt.HMAC_SHA1(StringToSign, signKey)

        val template =
            "q-sign-algorithm=sha1&q-ak=${tmpSecretId}&q-sign-time=${startTime};${expiredTime}&q-key-time=${startTime};${expiredTime}&q-header-list=content-length;host&q-url-param-list=&q-signature=${sign}"

        //val res = cosClient.newCall(request.build()).execute().body!!.string()
            ktorClient.put(path) {
                body = byteArray
                headers {
                    append("Authorization",template)
                    append("x-cos-security-token",sessionToken)
                }
        }
        return path
    }
    fun getPathFromUrl(url: String): String {
        // 找到第一个 '/' 的位置，跳过协议部分 (http:// or https://)
        val startIndex = url.indexOf("://")?.let { it + 3 } ?: 0
        val pathStartIndex = url.indexOf('/', startIndex)

        return if (pathStartIndex != -1) {
            url.substring(pathStartIndex)
        } else {
            "/"
        }
    }

    override suspend fun checkLogin(raw: String): Boolean {
        val queryParams = raw.substringAfter("?", "").split("&")
        val qrParam = queryParams.find { it.startsWith("qr=") }
        //val uuid = obj.query.split("&")[0].replace("qr=", "")
        val uuid = qrParam!!.substringAfter("qr=")
        val params = mapOf(
            "qr" to uuid,
            "web_source" to "open"
        )
        val url = "https://api.xiaoheihe.cn/account/qr_state?${ParamsBuilder(params).build("/account/qr_state/")}"
        val str = get(url)
        if (str.contains("登录成功")) {
            var jsonObject = Json.decodeFromString<JsonObject>(str)
            user.userId = jsonObject["heyboxid"]!!.jsonPrimitive.content
            user.userName = jsonObject["nickname"]!!.jsonPrimitive.content
            user.avatar = jsonObject["avatar"]!!.jsonPrimitive.content
            return true
        }
        return false
    }

    override suspend fun like(commentId: String) {
        /*
        val params = mapOf<String, String>()
        val url =
            "https://api.xiaoheihe.cn/bbs/app/comment/support?${ParamsBuilder(params).build("/bbs/app/comment/support/")}"
        runBlocking {
            ktorClient.submitForm(
                url,
                parameters {
                    append("comment_id", commentId)
                    append("support_type", "1")
                }
            )
        }

         */
    }

    suspend fun User.getFollowers(page: Int): List<User> {
        val params = mapOf(
            "userid" to this.userId,
            "limit" to "30",
            "offset" to "${(page - 1) * 30}"
        )
        val url =
            "https://api.xiaoheihe.cn/bbs/app/profile/following/list?${ParamsBuilder(params).build("/bbs/app/profile/following/list/")}"
        val str = get(url)
        val json = Json.decodeFromString<JsonObject>(str)
        val list = mutableListOf<User>()
        json["follow_list"]!!.jsonArray.forEach {
            val obj = it.jsonObject
            val user = User()
            user.avatar = obj["avatar"]!!.jsonPrimitive.content
            user.userName = obj["username"]!!.jsonPrimitive.content
            user.userId = obj["userid"]!!.jsonPrimitive.content
            list.add(user)

        }
        return list
    }

    suspend fun User.getFollowings(page: Int): List<User> {
        val params = mapOf(
            "userid" to this.userId,
            "limit" to "30",
            "offset" to "${(page - 1) * 30}"
        )
        val url =
            "https://api.xiaoheihe.cn/bbs/app/profile/follower/list?${ParamsBuilder(params).build("/bbs/app/profile/follower/list/")}"
        val str = get(url)
        val json = Json.decodeFromString<JsonObject>(str)
        val list = mutableListOf<User>()
        json["follow_list"]!!.jsonArray.forEach {
            val obj = it.jsonObject
            val user = User()
            user.avatar = obj["avatar"]!!.jsonPrimitive.content
            user.userName = obj["username"]!!.jsonPrimitive.content
            user.userId = obj["userid"]!!.jsonPrimitive.content
            list.add(user)

        }
        return list
    }

    suspend fun get(url: String): String {
        var string = ""
        var modifiedResponseString = ""
        /*
        runBlocking {


        }

         */
            string = ktorClient.get(url).bodyAsText()
            var obj = Json.decodeFromString<JsonObject>(string)
            modifiedResponseString = obj.toString()
            if (obj.contains("result") && obj.get("result") is JsonObject) {
                modifiedResponseString = obj.get("result").toString()
            }
        return modifiedResponseString
    }


    suspend fun searchSuggestion(keyword: String): List<String> {
        val params = mapOf(
            "q" to keyword,
        )
        val list = mutableListOf<String>()
        val url =
            "https://api.xiaoheihe.cn/bbs/app/api/search/suggestion/v2?${ParamsBuilder(params).build("/bbs/app/api/search/suggestion/v2/")}"
        val str = get(url)
        val obj = Json.decodeFromString<JsonObject>(str)
        obj["suggestions"]!!.jsonArray.forEach {
            list.add(it.jsonObject["text"]!!.jsonPrimitive.content)
        }
        return list
    }

    suspend fun searchPost(key: String, page: Int): List<Post> {
        val params = mapOf(
            "q" to key,
            "search_type" to "link",
            "limit" to "30",
            "offset" to "${(page - 1) * 30}",
            "time_range" to "",
            "sort_filter" to "sort_filter"
        )
        val list = mutableListOf<Post>()
        val url =
            "https://api.xiaoheihe.cn/bbs/app/api/general/search/v1?${ParamsBuilder(params).build("/bbs/app/api/general/search/v1/")}"
        val str = get(url)
        Json.decodeFromString<JsonObject>(str)["items"]!!.jsonArray.forEach {
            if (it.jsonObject.get("type")!!.jsonPrimitive.content == "link") {
                list.add(parsePost(it.jsonObject["info"]!!.jsonObject))
            }
        }
        return list
    }


    suspend fun test() {
        val params = mapOf(
            "pull" to "1",
            "use_history" to "0",
            "last_pull" to "1",
            "is_first" to "0",

            )
        val url = "https://api.xiaoheihe.cn/bbs/app/feeds?${ParamsBuilder(params).build("/bbs/app/feeds/")}"
        val str = get(url)
        println(str)
    }


}


fun parseTime(timeInMillis: Long): String {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val diff = currentTime - timeInMillis

    return when {
        diff < 1.toDuration(DurationUnit.MINUTES).inWholeMilliseconds -> {
            val seconds = diff.toDuration(DurationUnit.MILLISECONDS).inWholeSeconds
            "$seconds 秒前"
        }

        diff < 1.toDuration(DurationUnit.HOURS).inWholeMilliseconds -> {
            val minutes = diff.toDuration(DurationUnit.MILLISECONDS).inWholeMinutes
            "$minutes 分钟前"
        }

        diff < 1.toDuration(DurationUnit.DAYS).inWholeMilliseconds -> {
            val hours = diff.toDuration(DurationUnit.MILLISECONDS).inWholeHours
            "$hours 小时前"
        }

        diff < 30.toDuration(DurationUnit.DAYS).inWholeMilliseconds -> {
            val days = diff.toDuration(DurationUnit.MILLISECONDS).inWholeDays
            "$days 天前"
        }

        diff < 365.toDuration(DurationUnit.DAYS).inWholeMilliseconds -> {
            val months = diff.toDuration(DurationUnit.MILLISECONDS).inWholeDays / 30
            "$months 个月前"
        }

        else -> {
            val years = diff.toDuration(DurationUnit.MILLISECONDS).inWholeDays / 365
            "$years 年前"
        }
    }
}


val HeaderInterceptor = createClientPlugin("CustomHeaderPlugin") {
    onRequest { request, _ ->
        request.headers.append("Cookie", HeyClient.cookie)
        request.headers.append("Referer", "https://chat.xiaoheihe.cn/")
    }
    onResponse { response ->
        val cookie = response.headers["Set-Cookie"]
        if (cookie != null) {
            HeyClient.cookie = cookie
        }
    }
}
