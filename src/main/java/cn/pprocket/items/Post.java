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
        if (userName.contains("熊二")) {
            Map<String, String> params = new HashMap<>();
            params.put("return_json", "1");
            params.put("link_id", postId);
            String url = "https://api.xiaoheihe.cn/bbs/app/link/web/view?" + new ParamsBuilder(params).build("/bbs/app/link/web/view/");
            String string = HeyClient.INSTANCE.get(url);
            JsonObject obj = JsonParser.parseString(string).getAsJsonObject().getAsJsonObject("link");
            userName = obj.getAsJsonObject("poster").get("username").getAsString();
            userAvatar = obj.getAsJsonObject("poster").get("avatar").getAsString();
            JsonArray content1 = obj.getAsJsonArray("content");
            String text2 = content1.get(0).getAsJsonObject().get("text").getAsString();
            for (int i = 1; i < content1.size(); i++) {
                JsonObject o = content1.get(i).getAsJsonObject();
                images.add(o.get("url").getAsString());
            }
            content = Jsoup.parse(text2).text();
            return Jsoup.parse(text2).text();
        } else {
            String postId = this.postId;
            int page = 1;
            Map<String, String> map = new HashMap<String, String>();
            map.put("link_id", postId);
            map.put("page", Integer.toString(page));
            map.put("limit", "10");
            map.put("sort_filter", "hot");
            ParamsBuilder builder = new ParamsBuilder(map);
            String url = "https://api.xiaoheihe.cn/bbs/app/link/tree?" + builder.build("/bbs/app/link/tree/");
            String str = HeyClient.INSTANCE.get(url);
            JsonObject obj = JsonParser.parseString(str).getAsJsonObject().getAsJsonObject("link");
            String text = obj.get("text").getAsString().trim();
            JsonArray array = JsonParser.parseString(text).getAsJsonArray();
            String text1 = array.get(0).getAsJsonObject().get("text").getAsString();
            return Jsoup.parse(text1).text();
        }

    }
}
