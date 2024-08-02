package cn.pprocket.items;

import cn.pprocket.HeyClient;
import cn.pprocket.utils.ParamsBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.Request;
import org.example.cn.pprocket.utils.app.AppParamsBuilder;

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

    @SneakyThrows
    public static List<Topic> getTopics() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("type","list");
        params.put("is_post","1");
        params.put("post_tab","1");
        String url = "https://api.xiaoheihe.cn/bbs/app/api/topic/index?" + new ParamsBuilder(params).build("/bbs/app/api/topic/index/");
        String string = HeyClient.INSTANCE.get(url);
        List<Topic> topics = new ArrayList<>();
        JsonParser.parseString(string).getAsJsonObject().getAsJsonArray("topics_list").forEach(e -> {
            e.getAsJsonObject().getAsJsonArray("children").forEach( e0 -> {
                JsonObject e1 = e0.getAsJsonObject();

                Topic topic = new Topic();
                topic.setId(e1.get("topic_id").getAsInt());
                topic.setName(e1.get("name").getAsString());
                topic.setIcon(e1.get("pic_url").getAsString());
                topics.add(topic);
            });
        });
        return topics;
    }
}

