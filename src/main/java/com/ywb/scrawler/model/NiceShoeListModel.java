package com.ywb.scrawler.model;

import com.ywb.scrawler.enums.SizeChartEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
@Setter
public class NiceShoeListModel {
    private String id;
    private String name;
    private String cover;
    private String release_time;
    private String deal_num;
    private String sku;
    private Map<SizeChartEnum, NiceStockInfo> stocks;
}
