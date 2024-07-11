package cn.pprocket.items;

import cn.pprocket.HeyClient;
import cn.pprocket.utils.ParamsBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class Comment {
    private String content;
    private String userId;
    private String commentId;
    private String userName;
    private String userAvatar;
    private boolean hasMore;
    private String replyName;
    private String replyId;
    private String createdAt;
    private List<String> images;
    private int likes;
    private String postId;
    private List<Comment> subComments;

    public List<Comment> fillSubComments() {
        if (!hasMore) return subComments;
        Map<String,String> params = new HashMap<>();
        params.put("root_comment_id",commentId);
        params.put("lastval",subComments.get(subComments.size() - 1).getCommentId());
        String url = "https://api.xiaoheihe.cn/bbs/app/comment/sub/comments?" + new ParamsBuilder(params).build("/bbs/app/comment/sub/comments/");
        String string = HeyClient.INSTANCE.get(url);
        JsonArray array = JsonParser.parseString(string).getAsJsonObject().getAsJsonArray("comments");
        array.forEach(jsonElement -> {
            Comment comment = HeyClient.INSTANCE.parseComment(jsonElement.getAsJsonObject());
            this.subComments.add(comment);
        });
        hasMore = JsonParser.parseString(string).getAsJsonObject().get("has_more").getAsBoolean();
        removeDuplicate(this.subComments);
        return this.subComments;

    }
    private static void removeDuplicate(List<Comment> comments) {
        Map<String, Comment> distinctCommentsMap = new LinkedHashMap<>();
        for (Comment comment : comments) {
            distinctCommentsMap.putIfAbsent(comment.commentId, comment);
        }
        comments.clear();
        comments.addAll(distinctCommentsMap.values());
    }
}
