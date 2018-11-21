package com.ywb.scrawler.model;

import com.ywb.scrawler.enums.SizeChartEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class StockXShoeListModel {
    private String sku;
    private String name;
    private String release_date;
    private String colorway;
    private String objectID;
    private Map<SizeChartEnum, StockXStockInfo> stocks;
}
