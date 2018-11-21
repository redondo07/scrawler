package com.ywb.scrawler.dao;

import com.ywb.scrawler.model.Stock;
import org.apache.ibatis.annotations.Param;

public interface StockDao {

    Stock selectStock(@Param("id")Long id);

    void insert(Stock stock);

}
