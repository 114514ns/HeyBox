package cn.pprocket.items;

import lombok.Data;

@Data
public class User {
    private String userId;
    private String userName;
    private String location;
    private int level;
    private int followings;
    private int followers;
    private int posts;
    private String avatar;
    private String signature;
    private int games;
    private double hours;
    private double value;
    private String steamId;
    private int steamLevel;

}
