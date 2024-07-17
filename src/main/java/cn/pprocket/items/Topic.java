package cn.pprocket.items;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Topic {
    private String name;
    private int id;
    public static Topic DAILY = new Topic("盒友杂谈", 7214);
    public static Topic LOVE = new Topic("情投一盒", 416158);
    public static Topic SCHOOL = new Topic("校园生活", 549999);
    public static Topic HARDWARE = new Topic("数码硬件", 18745);
    public static Topic WORK = new Topic("职场工作", 550000);
    public static Topic HOTS = new Topic("热点", -1);
    public static Topic RECOMMEND = new Topic("推荐",-2);

}

