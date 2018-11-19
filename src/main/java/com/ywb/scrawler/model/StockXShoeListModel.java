package com.ywb.scrawler.model;

import com.ywb.scrawler.SizeChartEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class StockXShoeListModel {
    private String name;
    private String release_date;
    private String colorway;
    private String objectID;
    private Map<SizeChartEnum, StockXStockInfo> stocks;
}
