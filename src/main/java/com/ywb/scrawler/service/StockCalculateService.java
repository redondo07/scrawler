package com.ywb.scrawler.service;

import com.ywb.scrawler.model.StockCalculatedRef;

import java.util.List;

public interface StockCalculateService {
    List<StockCalculatedRef> calculateDiff(long timeStamp);

}
