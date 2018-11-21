package com.ywb.scrawler.service;

import com.google.common.collect.Lists;
import com.ywb.scrawler.model.NiceShoeListModel;
import com.ywb.scrawler.model.NiceStockInfo;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class NiceStockInfoPageService {

    public List<NiceStockInfo> getStockInfoByGoodsId(String goodsId) {
        List<NiceStockInfo> stocks = Lists.newArrayList();
        String url = "https://m.oneniceapp.com/sneakersale/stockinfos?goods_id=" + goodsId;
        try {
            String html = getConnection(url).execute().body();
            Elements elements = Jsoup.parse(html).select("div.sizeItem");
            for (Element e : elements) {
                Elements spans = e.select("span");

                NiceStockInfo model = new NiceStockInfo();
                model.setSize(spans.select("span.sizeItemSize").get(0).childNode(0).toString());
                model.setUnit(spans.select("span.sizeItemPrice").get(0).childNode(0).toString());
                model.setPrice(Double.valueOf(spans.select("span.sizeItemPrice").get(0).childNode(1).toString()));
                model.setDesc(spans.select("span.sizeItemBtn").get(0).childNode(0).toString());

                Integer stock = 0;
                try {
                    stock =  Integer.valueOf(spans.select("span.sizeItemNum").get(0).childNode(0).toString());
                } catch (NumberFormatException e1) {
                    System.out.println("库存不为数字。。");
                    stock = 0;
                }
                model.setStock(stock);
                stocks.add(model);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stocks;
    }

    private Connection getConnection(String url) {
        String cookie = "did=eeb4f168016f955f9ebe4365f4f63656; id=28985015; lan=cn; name=Stanley261840; sign=9e4948a93028acbbfcb08bac3d8eb467; time=1542796205; token=arQAiGVk839UoBT-CBeAPItmt-wneZEF; uid=28985015; niceUser=%7B%22uid%22%3A%2228985015%22%2C%22rand%22%3A2056%2C%22expire%22%3A1543400972%2C%22sign%22%3A%222eb1b7ed52f09874b7794f0d0e48f843%22%7D; lang=zh-cn; nuid=CgoKDFvvu7s0tZskCtw5Ag==";

        Connection conn = Jsoup.connect(url);
        conn.userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 12_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16B92 iPhone10,3 NiceBrowser/5.2.14.20");
        conn.header("Accept-Encoding", "");
        conn.header("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
        conn.header("Accept-Language", "zh-CN,zh;q=0.8");
        conn.header("Cache-Control", "max-age=0");
        conn.header("Cookie", cookie);
        conn.timeout(15 * 1000);
        conn.ignoreContentType(true);
        return conn;
    }
}
