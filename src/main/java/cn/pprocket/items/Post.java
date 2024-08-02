package cn.pprocket.items;

import cn.pprocket.HeyClient;
import cn.pprocket.utils.ParamsBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private List<Topic> tags;
    private boolean isHTML = false;

    public String fillContent() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("link_id", this.postId);
        map.put("page", "1");
        map.put("limit", "10");
        String url =
                "https://api.xiaoheihe.cn/bbs/app/link/tree?" + new ParamsBuilder(map).build("/bbs/app/link/tree");
        String res = HeyClient.INSTANCE.get(url);
        JsonObject parsed = JsonParser.parseString(res).getAsJsonObject().getAsJsonObject("link");
        StringBuilder builder = new StringBuilder();
        try {
            JsonParser.parseString(parsed.get("text").getAsString()).getAsJsonArray().forEach(ele -> {
                JsonObject object = ele.getAsJsonObject();
                if (object.get("type").getAsString().equals("text")) {
                    builder.append(object.get("text").getAsString()).append("\n");
                }
            });
            return builder.toString();
        } catch (Exception e) {
            return this.description;
        }

    }

    public List<Tag> renderHTML() {
        List<Tag> tags = new ArrayList<>();
        Map<String, String> map = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();
        map.put("link_id", postId);
        map.put("return_json", "1");
        map.put("index", "1");
        String url = "https://api.xiaoheihe.cn/bbs/app/link/web/view?" + new ParamsBuilder(map).build("/bbs/app/link/web/view/");
        String string = HeyClient.INSTANCE.get(url);
        JsonObject parsed = JsonParser.parseString(string).getAsJsonObject().getAsJsonObject("link");
        String str = parsed.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString();
        List<Element> elements = Jsoup.parse(str).getElementsByTag("body").get(0).children();
        elements.forEach(ele -> {
            Tag tag = new Tag();
            if (ele.childNodes().size() != 0) {
                if (
                        !ele.getElementsByTag("h3").isEmpty() ||
                                !ele.getElementsByTag("h2").isEmpty()) {
                    tag.setTagType("title");
                    tag.setTagValue(ele.text());

                } else if (!ele.getElementsByTag("img").isEmpty()) {
                    Element img = ele.getElementsByTag("img").get(0);
                    if (img.hasAttr("data-original")) {
                        tag.setTagType("image");
                        tag.setTagValue(img.attr("data-original"));
                    } else {
                        tag.setTagType("gameCard");
                        tag.setTagValue(img.attr("data-gameid"));
                    }
                } else if (ele.childNodes().get(0) instanceof TextNode) {
                    tag.setTagType("text");
                    tag.setTagValue(ele.text());
                } /*else if (ele instanceof TextNode) {
                    tag.setTagType("text");
                    tag.setTagValue(ele.text());
                } */ else if (ele.getElementsByTag("b").size() != 0 && ele.childNodes().size() > 1) {
                    ele.childNodes().forEach(e -> {

                    });
                } else if (!ele.getElementsByTag("a").isEmpty()) {
                    tag.setTagType("link");
                    tag.setTagValue(ele.attr("href"));
                } else if (!ele.getElementsByTag("blockquote").isEmpty()) {
                    tag.setTagType("ref");
                    tag.setTagValue(ele.text());
                }
                tags.add(tag);
            }
        });
        if (this.userName.contains("熊二")) {
            this.userName = parsed.getAsJsonObject("poster").get("username").getAsString();
            this.userAvatar = parsed.getAsJsonObject("poster").get("avatar").getAsString();
        }
        if (tags.isEmpty()) {
            Tag tag = new Tag();
            tag.setTagType("text");
            tag.setTagValue(removeSpanTags(str));
            tags.add(tag);
        }
        return tags;
    }
    public static String removeSpanTags(String input) {
        // 使用正则表达式去除所有<span>标签，同时保留标签内的文本内容
        String regex = "<\\s*span[^>]*>(.*?)<\\s*/\\s*span\\s*>";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        // 通过替换匹配到的内容来去除<span>标签
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, matcher.group(1));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
