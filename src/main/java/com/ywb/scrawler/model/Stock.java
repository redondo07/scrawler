package com.ywb.scrawler.model;

import java.math.BigDecimal;
import java.util.Date;

public class Stock {
    private Long id;
    private String name;
    private String cover;
    private String sku;
    private String sizeUS;
    private String sizeEU;
    private BigDecimal priceNice;
    private BigDecimal priceStockX;
    private BigDecimal calculatedNicePriceRmb;
    private BigDecimal calculatedStockXPriceRmb;
    private BigDecimal profit;
    private BigDecimal profitRate;
    private Date updateTime;
    private Date createTime;

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSizeUS() {
        return sizeUS;
    }

    public void setSizeUS(String sizeUS) {
        this.sizeUS = sizeUS;
    }

    public String getSizeEU() {
        return sizeEU;
    }

    public void setSizeEU(String sizeEU) {
        this.sizeEU = sizeEU;
    }

    public BigDecimal getPriceNice() {
        return priceNice;
    }

    public void setPriceNice(BigDecimal priceNice) {
        this.priceNice = priceNice;
    }

    public BigDecimal getPriceStockX() {
        return priceStockX;
    }

    public void setPriceStockX(BigDecimal priceStockX) {
        this.priceStockX = priceStockX;
    }

    public BigDecimal getCalculatedNicePriceRmb() {
        return calculatedNicePriceRmb;
    }

    public void setCalculatedNicePriceRmb(BigDecimal calculatedNicePriceRmb) {
        this.calculatedNicePriceRmb = calculatedNicePriceRmb;
    }

    public BigDecimal getCalculatedStockXPriceRmb() {
        return calculatedStockXPriceRmb;
    }

    public void setCalculatedStockXPriceRmb(BigDecimal calculatedStockXPriceRmb) {
        this.calculatedStockXPriceRmb = calculatedStockXPriceRmb;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public BigDecimal getProfitRate() {
        return profitRate;
    }

    public void setProfitRate(BigDecimal profitRate) {
        this.profitRate = profitRate;
    }
}
