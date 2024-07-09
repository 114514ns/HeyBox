package cn.pprocket.items;

import cn.pprocket.HeyClient;
import cn.pprocket.utils.ParamsBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Post {
    private String title;
    private String postId;
    private String userId;
    private String userAvatar;
    private String userName;
    private String description;
    private List<String> images;
    private String createAt;
    private int comments;
    private int likes;
    private String content = "";
    public String fillContent() {
        String postId = this.postId; // 替换为实际的postId
        int page = 1; // 替换为实际的页数
        Map<String,String> map = new HashMap<String,String>();
        map.put("link_id", postId);
        map.put("page", Integer.toString(page));
        map.put("limit", "10");
        map.put("sort_filter", "hot");
        ParamsBuilder builder = new ParamsBuilder(map);
        String url = "https://api.xiaoheihe.cn/bbs/app/link/tree?" + builder.build("/bbs/app/link/tree/");
        String str = HeyClient.INSTANCE.get(url);
        JsonObject obj = JsonParser.parseString(str).getAsJsonObject().getAsJsonObject("link");
        String text = obj.get("text").getAsString();
        JsonArray array = JsonParser.parseString(text).getAsJsonArray();
        String text1 = array.get(0).getAsJsonObject().get("text").getAsString();
        return Jsoup.parse(text1).text();
    }
}
