package com.ywb.scrawler.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StockXStockInfo {
    private String size;
    private Double amount;
    private int state;
    private String dateStr;
}
