package com.ywb.scrawler.dao;

import com.ywb.scrawler.model.Stock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StockDaoTest {

    @Resource
    private StockDao stockDao;

    @Test
    public void selectStockTest() {
        // Stock stock = stockDao.selectStock(1L);
        // System.out.println(stock.getName());
    }

}