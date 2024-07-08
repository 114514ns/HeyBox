package cn.pprocket

import cn.pprocket.items.*
import cn.pprocket.items.User.HardWare
import cn.pprocket.utils.ParamsBuilder
import cn.pprocket.utils.SignService
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.Buffer
import okio.BufferedSource
import java.util.*

object HeyClient : Client {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HeyInterceptor())
        .build()
    var cookie: String = ""
    val cleanClient = OkHttpClient.Builder().build()
    override fun login(cookie: String) {
        this.cookie = cookie
    }

    override fun getUSer(heyId: String): User {
        val user = User()
        runBlocking {
            launch {
                val params = mutableMapOf(
                    "userid" to heyId,
                )
                val str =
                    get("https://api.xiaoheihe.cn/bbs/app/profile/user/profile?${ParamsBuilder(params).build("/bbs/app/profile/user/profile/")}")
                var obj = JsonParser.parseString(str).asJsonObject.getAsJsonObject("account_detail")
                user.userName = obj.get("username").asString
                user.avatar = obj.get("avatar").asString
                user.level = obj.getAsJsonObject("level_info").get("level").asInt
                val bbs = obj.getAsJsonObject("bbs_info")
                user.posts = bbs.get("post_link_num").asInt
                user.location = obj.get("ip_location").asString
                user.followers = bbs.get("follow_num").asInt
                user.signature = obj.get("signature").asString
                user.followers = bbs.get("fan_num").asInt
            }
            launch {
                val params = mapOf(
                    "userid" to heyId
                )
                val str =
                    get("https://api.xiaoheihe.cn/account/heybox_home_v2?${ParamsBuilder(params).build("/account/heybox_home_v2/")}")
                val obj = JsonParser.parseString(str).asJsonObject
                user.games = obj.get("game_count").asInt
                if (obj.has("game_overview")) {
                    user.hours = obj.getAsJsonArray("game_overview")[1].asJsonObject.get("value").asDouble
                    user.value = obj.getAsJsonArray("game_overview")[0].asJsonObject.get("value").asDouble
                }
                if (obj.has("hardware_info")) {
                    val hardWare = HardWare()
                    val o = obj.getAsJsonObject("hardware_info")
                    hardWare.processor = o.get("cpu").asString
                    hardWare.graphs = o.get("gpu").asString
                    hardWare.board = o.get("board").asString
                }
                if (obj.has("steam_id_info")) {
                    user.steamId = obj.getAsJsonObject("steam_id_info").get("steamid").asString
                    user.steamId = obj.getAsJsonObject("steam_id_info").get("level").asString
                }

            }
        }
        return user
    }

    override fun getPosts(topic: Topic): List<Post> {
        val id = topic.id
        val posts = mutableListOf<Post>()
        val map = mapOf(
            "topic_id" to id.toString(),
            "offset" to "20",
            "limit" to "20",
            "sort_filter" to "hot-rank"
        )
        val url = "https://api.xiaoheihe.cn/bbs/app/topic/feeds?${ParamsBuilder(map).build("/bbs/app/topic/feeds/")}"
        val res = get(url)
        JsonParser.parseString(res).asJsonObject.getAsJsonArray("links").forEach {
            val post = Post()
            var obj = it.asJsonObject
            post.title = obj.get("title").asString
            post.postId = obj.get("linkid").asString
            post.userId = obj.get("userid").asString
            post.userAvatar = obj.getAsJsonObject("user").get("avatar").asString
            post.userName = obj.getAsJsonObject("user").get("username").asString
            post.description = obj.get("description").asString
            val list = mutableListOf<String>()
            obj.get("imgs").asJsonArray.forEach {
                list.add(it.asString)
            }
            post.images = list
            post.createAt = obj.get("create_str").asString
            post.comments = obj.get("comment_num").asInt
            post.likes = obj.get("link_award_num").asInt
            posts.add(post)

        }
        return posts
    }

    override fun getPost(id: String): Post {
        TODO("Not yet implemented")
    }

    override fun getGame(id: String): Game {
        var time = (System.currentTimeMillis() / 1000).toString()
        val nonce = SignService.md5(Random().nextDouble().toString()).uppercase()
        var string = directGet(
            "https://api.xiaoheihe.cn/game/get_game_detail/?os_type=web&app=heybox&client_type=mobile&version=999.0.3&x_client_type=web&x_os_type=Windows&x_client_version=&x_app=heybox&heybox_id=-1&steam_appid=${id}&hkey=${
                SignService.calc(
                    "/game/get_game_detail/",
                    time,
                    nonce
                )
            }&_time=${time}&nonce=${nonce}"
        )
        val obj = JsonParser.parseString(string).asJsonObject.getAsJsonObject("result")
        val game = Game()
        game.isFree = obj.get("is_free").asBoolean
        game.rating = obj.get("score").asDouble
        game.name = obj.get("name").asString
        game.description = obj.get("about_the_game").asString
        game.price = if (game.isFree) 0.0 else obj.get("price").asJsonObject.get("current").asDouble
        game.lowest = if (game.isFree) 0.0 else obj.get("price").asJsonObject.get("lowest_price").asDouble
        val arr = obj.getAsJsonObject("user_num").getAsJsonArray("game_data")
        val statistic = Game.Statistic()

        statistic.heyTime = arr[7].asJsonObject.get("value").asString.replace("h", "").toDouble()
        statistic.heyUser =
            arr[6].asJsonObject.get("value").asString.toDouble() * if (arr[6].toString().contains("万")) 10000 else 1
        statistic.online =
            arr[0].asJsonObject.get("value").asString.toDouble() * if (arr[0].toString().contains("万")) 10000 else 1
        game.statistic = statistic
        val platforms = mutableListOf<String>()
        obj.getAsJsonArray("platforms").forEach {
            platforms.add(it.asString)
        }
        game.platforms = platforms
        val menu = obj.getAsJsonArray("menu_v2")
        game.release = menu[0].asJsonObject.get("value").asString
        game.developer = menu[1].asJsonObject.get("value").asString
        val tags = mutableListOf<String>()
        obj.getAsJsonArray("common_tags").forEach {
            try {
                tags.add(it.asString)
            } catch (e: UnsupportedOperationException) {

            }
        }
        game.tags = tags
        val awards = mutableListOf<String>()
        obj.getAsJsonArray("game_award").forEach {
            val o = it.asJsonObject
            awards.add(o.get("desc").asString + " " + o.get("detail_name").asString)
        }
        game.awards = awards
        val screenshots = mutableListOf<String>()
        obj.getAsJsonArray("screenshots").forEach {
            val o = it.asJsonObject
            screenshots.add(o.get("url").asString)
        }
        game.screenshots = screenshots
        return game
    }

    override fun getComments(postId: String, page: Int): List<Comment> {
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
        val obj = JsonParser.parseString(str).asJsonObject.getAsJsonArray("comments")
        obj.forEach {
            var isFirst = true
            var comment = Comment()
            val sub = mutableListOf<Comment>()
            it.asJsonObject.getAsJsonArray("comment").forEach {
                var comment1 = parseComment(it.asJsonObject)
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
        return comments
    }

    public fun parseComment(json: JsonObject): Comment {

        val comment = Comment()
        comment.userId = json.getAsJsonObject("user").get("userid").asString
        comment.userName = json.getAsJsonObject("user").get("username").asString
        comment.userAvatar = json.getAsJsonObject("user").get("avatar").asString
        comment.content = json.get("text").asString
        comment.commentId = json.get("commentid").asString
        try {
            comment.replyId = json.getAsJsonObject("replyuser").get("userid").asString
        } catch (_: Exception) {}
        try {
            comment.replyName = json.getAsJsonObject("replyuser").get("username").asString
        } catch (_: Exception) {}
        try {
            comment.isHasMore = json.get("has_more").asInt == 1
        } catch (_: Exception) {}
        return comment

    }

    fun get(url: String): String {
        var string = client.newCall(
            Request.Builder()
                .url(url)
                .get()
                .build()
        ).execute().body.string()
        return string
    }

    private fun directGet(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()
        return cleanClient.newCall(request).execute().body.string()
    }

}

class HeyInterceptor : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        var url = chain.request().url.toUrl().toString()
        var newBuilder = chain.request().newBuilder()
        newBuilder.addHeader("Cookie", HeyClient.cookie)
        val response = chain.proceed(chain.request())
        // 获取原始响应内容
        val originalBody = response.body ?: return response

        // 读取原始响应内容
        val source: BufferedSource = originalBody.source()
        source.request(Long.MAX_VALUE)
        val buffer: Buffer = source.buffer

        // 将原始响应内容转换为字符串
        val originalResponseString = buffer.clone().readString(Charsets.UTF_8)
        val modifiedResponseString =
            JsonParser.parseString(originalResponseString).asJsonObject.getAsJsonObject("result").toString()
        // 创建新的响应体
        val modifiedResponseBody = ResponseBody.create("application/json".toMediaTypeOrNull(), modifiedResponseString)

        // 创建新的响应
        return response.newBuilder()
            .body(modifiedResponseBody)
            .build()
    }


}
