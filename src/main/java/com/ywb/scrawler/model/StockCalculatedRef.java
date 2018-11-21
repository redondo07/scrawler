package com.ywb.scrawler.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

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

    // onsale部分
    private Double newProfit;
    private String status; // 需下架，需调整价格，正常
    private Double salePrice;

    public Stock buildStockModel() {
        Stock stock = new Stock();
        stock.setName(name);
        stock.setCover(imgUrl);
        stock.setSku(sku);
        stock.setSizeUS(sizeUS);
        stock.setSizeEU(sizeEU);
        stock.setPriceNice(new BigDecimal(priceNice));
        stock.setPriceStockX(new BigDecimal(priceStockX));
        stock.setCalculatedNicePriceRmb(new BigDecimal(calculatedNicePriceRmb));
        stock.setCalculatedStockXPriceRmb(new BigDecimal(calculateStockXPriceRmb));
        stock.setProfit(new BigDecimal(profit));
        stock.setProfitRate(new BigDecimal(profitRate));
        stock.setUpdateTime(new Date());
        stock.setCreateTime(new Date());
        return stock;
    }

}
