package cn.pprocket.items

import cn.pprocket.HeyClient.domain
import cn.pprocket.HeyClient.get
import cn.pprocket.utils.ParamsBuilder
import kotlinx.serialization.json.*



data class Topic(
    val name: String,
    val id: Int,
    val icon: String,
) {


    companion object {
        var DAILY: Topic = Topic("盒友杂谈", 7214, "")
        var LOVE: Topic = Topic("情投一盒", 416158, "")
        var SCHOOL: Topic = Topic("校园生活", 549999, "")
        var HARDWARE: Topic = Topic("数码硬件", 18745, "")
        var WORK: Topic = Topic("职场工作", 550000, "")
        var HOTS: Topic = Topic("热点", -1, "")
        var RECOMMEND: Topic = Topic("推荐", -2, "")
        var MAX: Topic = Topic("Max家", 475512, "")


        suspend fun fetchTopics(): List<Topic> {
            val params: MutableMap<String, String> = HashMap()
            params["type"] = "list"
            params["is_post"] = "1"
            params["post_tab"] = "1"
            val url =
                "${domain}/bbs/app/api/topic/index?" + ParamsBuilder(params).build("/bbs/app/api/topic/index/")
            val string = get(url)
            val topics = mutableListOf<Topic>()
            Json.decodeFromString<JsonObject>(string).get("topics_list")!!.jsonArray.forEach {
                it.jsonObject.get("children")!!.jsonArray.forEach {
                    val o = it.jsonObject
                    val topic = Topic(
                        id = o.get("topic_id")!!.jsonPrimitive.int,
                        name = o.get("name")!!.jsonPrimitive.content,
                        icon = o.get("pic_url")!!.jsonPrimitive.content
                    )
                    topics.add(topic)
                }
            }
            return topics
        }
    }
}

