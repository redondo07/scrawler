package com.ywb.scrawler.service;

import com.ywb.scrawler.model.StockXShoeListModel;

import java.util.List;

public interface StockXService {
    List<StockXShoeListModel> getProductList();

    StockXShoeListModel getProductDetail(StockXShoeListModel model);

    StockXShoeListModel getProductDetail2(StockXShoeListModel model);

    StockXShoeListModel searchItem(String sku);
}
