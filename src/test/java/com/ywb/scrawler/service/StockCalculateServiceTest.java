package com.ywb.scrawler.service;


import com.ywb.scrawler.model.NiceStockInfo;
import com.ywb.scrawler.model.StockCalculatedRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StockCalculateServiceTest {

    @Resource
    private StockCalculateService stockCalculateService;
    @Resource
    private NiceStockInfoPageService niceStockInfoPageService;

    @Test
    public void calculateDiffTest() {
        Long start = System.currentTimeMillis();
        List<StockCalculatedRef> result = stockCalculateService.calculateDiff();

        System.out.println(result.size());
        System.out.println("[calculateDiffTest] calculate cost " + (System.currentTimeMillis() - start) + " ms.");
    }

    @Test
    public void getStockInfoByGoodsIdTest() {
        String goodsId = "87342";
        List<NiceStockInfo> stockInfos = niceStockInfoPageService.getStockInfoByGoodsId(goodsId);
        System.out.println(stockInfos.size());
    }

}
