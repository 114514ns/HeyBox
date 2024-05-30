package cn.pprocket

import cn.pprocket.Client
import cn.pprocket.items.Game
import cn.pprocket.items.Post
import cn.pprocket.items.User
import cn.pprocket.items.User.HardWare
import cn.pprocket.utils.ParamsBuilder
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.parser.DefaultJSONParser
import com.github.unidbg.AndroidEmulator
import com.github.unidbg.Symbol
import com.github.unidbg.arm.backend.Unicorn2Factory
import com.github.unidbg.linux.android.AndroidEmulatorBuilder
import com.github.unidbg.linux.android.AndroidResolver
import com.github.unidbg.linux.android.dvm.AbstractJni
import com.github.unidbg.linux.android.dvm.DvmObject
import com.github.unidbg.linux.android.dvm.StringObject
import com.github.unidbg.linux.android.dvm.VM
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.Buffer
import okio.BufferedSource
import java.io.File
import java.lang.UnsupportedOperationException

object HeyClient : Client {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HeyInterceptor())
        .build()
    var cookie: String = ""

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
                val str = get("https://api.xiaoheihe.cn/bbs/app/profile/user/profile?${ParamsBuilder(params).build("/bbs/app/profile/user/profile/")}")
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
                val str = get("https://api.xiaoheihe.cn/account/heybox_home_v2?${ParamsBuilder(params).build("/account/heybox_home_v2/")}")
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

    override fun getPosts(): List<Post> {
        TODO("Not yet implemented")
    }

    override fun getPost(id: String): Post {
        TODO("Not yet implemented")
    }

    override fun getGame(id: String): Game {
        val params = mapOf(
            "appid" to id,
        )
        var build = ParamsBuilder(params).build("/game/get_game_detail/")
        var string = get("https://api.xiaoheihe.cn/game/get_game_detail/?${build}")
        val obj = JsonParser.parseString(string).asJsonObject
        val game = Game()
        game.isFree = obj.get("is_free").asBoolean
        game.rating = obj.get("score").asDouble
        game.name = obj.get("name").asString
        game.description = obj.get("about_the_game").asString
        game.price = if (game.isFree) 0.0 else obj.get("price").asJsonObject.get("current").asDouble
        game.lowest = if (game.isFree) 0.0 else obj.get("price").asJsonObject.get("lowest_price").asDouble
        val arr = obj.getAsJsonObject("user_num").getAsJsonArray("game_data")
        val statistic = Game.Statistic()

        statistic.heyTime = arr[7].asJsonObject.get("value").asString.replace("h","").toDouble()
        statistic.heyUser = arr[6].asJsonObject.get("value").asString.toDouble() * if (arr[6].toString().contains("万")) 10000 else 1
        statistic.online = arr[0].asJsonObject.get("value").asString.toDouble() * if (arr[0].toString().contains("万")) 10000 else 1
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
    private fun get(url:String) :String {
        var string = client.newCall(
            Request.Builder()
                .url(url)
                .get()
                .build()
        ).execute().body.string()
        return string
    }

}

class HeyInterceptor : Interceptor, AbstractJni() {


    override fun intercept(chain: Interceptor.Chain): Response {
        var url = chain.request().url.toUrl().toString()
        var newBuilder = chain.request().newBuilder()
        newBuilder.addHeader("Cookie",HeyClient.cookie)
        val response = chain.proceed(chain.request())
        // 获取原始响应内容
        val originalBody = response.body ?: return response

        // 读取原始响应内容
        val source: BufferedSource = originalBody.source()
        source.request(Long.MAX_VALUE)
        val buffer: Buffer = source.buffer

        // 将原始响应内容转换为字符串
        val originalResponseString = buffer.clone().readString(Charsets.UTF_8)
        val modifiedResponseString = JsonParser.parseString(originalResponseString).asJsonObject.getAsJsonObject("result").toString()
        // 创建新的响应体
        val modifiedResponseBody = ResponseBody.create("application/json".toMediaTypeOrNull(), modifiedResponseString)

        // 创建新的响应
        return response.newBuilder()
            .body(modifiedResponseBody)
            .build()
    }


}
object SignService :AbstractJni() {
    private var symbol: Symbol? = null
    private var emulator: AndroidEmulator? = null
    private var vm: VM? = null
    private var flag = false
    fun calc(path: String, time: String, nonce: String): String {
        val args: MutableList<Any> = ArrayList(10)
        args.add(vm!!.jniEnv)
        args.add(0)
        args.add(vm!!.addLocalObject(StringObject(vm, "/account/data_report/")))
        args.add(vm!!.addLocalObject(StringObject(vm, path)))
        args.add(vm!!.addLocalObject(StringObject(vm, time)))
        args.add(vm!!.addLocalObject(StringObject(vm, nonce)))
        val start = System.currentTimeMillis()
        val number = symbol!!.call(emulator, *args.toTypedArray())
        val result = vm!!.getObject<DvmObject<*>>(number.toInt()).value.toString()
        println("Sign took ${System.currentTimeMillis() - start} ms")
        return result
    }
    init {
        emulator = AndroidEmulatorBuilder.for64Bit()
            .setProcessName("com.qidian.dldl.official")
            .addBackendFactory(Unicorn2Factory(true))
            .build() // 创建模拟器实例，要模拟32位或者64位，在这里区分
        val memory = emulator!!.memory // 模拟器的内存操作接口
        memory.setLibraryResolver(AndroidResolver(23)) // 设置系统类库解析

        vm = emulator!!.createDalvikVM()
        vm!!.setVerbose(false)
        vm!!.setJni(this)
        val dm = vm!!.loadLibrary(File("libnative-lib.so"),true)
        val module = dm.module

        symbol = module.findSymbolByName("Java_com_starlightc_ucropplus_network_temp_TempEncodeUtil_encode")

    }
}
