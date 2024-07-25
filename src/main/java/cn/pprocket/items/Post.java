package cn.pprocket.items;

import cn.pprocket.HeyClient;
import cn.pprocket.utils.ParamsBuilder;
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
    private List<String> tags;

    public String fillContent() {
        Map<String, String> map = new HashMap<String, String>();
        if (!userName.contains("熊二")) {
            map.put("link_id", this.postId);
            map.put("page", "1");
            map.put("limit", "10");
            String url =
                    "https://api.xiaoheihe.cn/bbs/app/link/tree?" + new ParamsBuilder(map).build("/bbs/app/link/tree");
            String res = HeyClient.INSTANCE.get(url);
            JsonObject parsed = JsonParser.parseString(res).getAsJsonObject().getAsJsonObject("link");
            StringBuilder builder = new StringBuilder();

            JsonParser.parseString(parsed.get("text").getAsString()).getAsJsonArray().forEach(ele -> {
                JsonObject object = ele.getAsJsonObject();
                if (object.get("type").getAsString().equals("text")) {
                    builder.append(object.get("text").getAsString()).append("\n");
                }
            });
            return builder.toString();
        } else {

            StringBuilder sb = new StringBuilder();
            map.put("link_id", postId);
            map.put("return_json", "1");
            map.put("index", "1");
            String url = "https://api.xiaoheihe.cn/bbs/app/link/web/view?" + new ParamsBuilder(map).build("/bbs/app/link/web/view/");
            String string = HeyClient.INSTANCE.get(url);
            JsonObject parsed = JsonParser.parseString(string).getAsJsonObject().getAsJsonObject("link");
            String str = parsed.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString();

            Jsoup.parse(str).getElementsByTag("body").get(0).children().forEach(ele -> {
                sb.append(ele.text()).append(System.lineSeparator());
            });
            if (this.userName.contains("熊二")) {
                this.userName = parsed.getAsJsonObject("poster").get("username").getAsString();
                this.userAvatar = parsed.getAsJsonObject("poster").get("avatar").getAsString();
            }
            return sb.toString();
        }


    }
}
