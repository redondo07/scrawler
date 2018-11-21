package com.ywb.scrawler.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StockCalculatedRef {
    private String sku;
    private String sizeUS;
    private String sizeEU;
    private Double priceNice;
    private Double priceStockX;
    private Double calculatedNicePriceRmb;
    private Double calculateStockXPriceRmb;
    private Double profit;
    private Double profitRate;
    private String name;
    private String imgUrl;
    private String desc;

}
