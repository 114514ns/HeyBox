package cn.pprocket.items;

import cn.pprocket.HeyClient;
import cn.pprocket.utils.ParamsBuilder;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Topic {
    private String name;
    private int id;
    private String icon;
    public static Topic DAILY = new Topic("盒友杂谈", 7214,"");
    public static Topic LOVE = new Topic("情投一盒", 416158,"");
    public static Topic SCHOOL = new Topic("校园生活", 549999,"");
    public static Topic HARDWARE = new Topic("数码硬件", 18745,"");
    public static Topic WORK = new Topic("职场工作", 550000,"");
    public static Topic HOTS = new Topic("热点", -1,"");
    public static Topic RECOMMEND = new Topic("推荐", -2,"");
    public static Topic MAX = new Topic("Max家", 475512,"");

    public static List<Topic> getTopics() {
        Map<String, String> params = new HashMap<String, String>();
        String url = "https://api.xiaoheihe.cn/bbs/app/topic/categories?" + new ParamsBuilder(params).build("/bbs/app/topic/categories/");
        String string = HeyClient.INSTANCE.get(url);
        List<Topic> topics = new ArrayList<>();
        JsonParser.parseString(string).getAsJsonObject().getAsJsonObject("latest_hot_topics").getAsJsonArray("children").forEach(e -> {
            Topic topic = new Topic(e.getAsJsonObject().get("name").getAsString(),
                    e.getAsJsonObject().get("topic_id").getAsInt(),
                    e.getAsJsonObject().get("small_pic_url").getAsString());
            topics.add(topic);
        });
        return topics;
    }
}

