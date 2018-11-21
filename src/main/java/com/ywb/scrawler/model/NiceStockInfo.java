package com.ywb.scrawler.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NiceStockInfo {
    private String size;
    private Double price;
    private String unit;
    private int stock;
    private String desc; // 预售
}
