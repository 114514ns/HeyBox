package cn.pprocket.items;

import lombok.Data;

import java.util.List;

@Data
public class Game {
    @Data
    public static class Statistic {
        private double heyUser;
        private double heyTime;
        private double online;
    }

    private boolean free;
    private String name;
    private double price;
    private double lowest;
    private double rating;
    private List<String> platforms;
    private List<String> tags;
    private String release;
    private String developer;
    private String series;
    private String description;
    private Statistic statistic;
    private List<String> awards;
    private List<String> screenshots;
    private String discount;



}
