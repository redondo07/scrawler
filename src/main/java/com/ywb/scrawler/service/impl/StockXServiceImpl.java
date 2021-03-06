package com.ywb.scrawler.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.ywb.scrawler.enums.SizeChartEnum;
import com.ywb.scrawler.model.StockXShoeListModel;
import com.ywb.scrawler.model.StockXStockInfo;
import com.ywb.scrawler.service.StockXService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StockXServiceImpl implements StockXService {
    @Autowired
    private RestTemplate restTemplate;

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


    // @PostConstruct
    private void init(){
        // this.searchItem("555088 700");
        StockXShoeListModel model = new StockXShoeListModel();
        model.setObjectID("5c5d3150-983f-454f-aa43-03d59d2edc7c");
        this.getProductDetail(model);

    }

    @Override
    public List<StockXShoeListModel> getProductList() {
        return null;
    }

    @Override
    public StockXShoeListModel getProductDetail(StockXShoeListModel model) {
        int count = 0;
        try{
            if(null == model || Strings.isNullOrEmpty(model.getObjectID())){
                return null;
            }
            MultiValueMap<String, String> headers = new LinkedMultiValueMap();
            headers.add("jwt-authorization", "");
            headers.add("x-api-key", "99WtRZK6pS1Fqt8hXBfWq8BYQjErmwipa3a0hYxX");

            HttpEntity<String> entity = new HttpEntity<>("", headers);
            StringBuffer paramsUrl = new StringBuffer("https://gateway.stockx.com/api/v2/products/");
            paramsUrl.append(model.getObjectID());
            paramsUrl.append("/activity?state=480&page=1&sort=createdAt&limit=100&order=DESC&currency=USD");

            URI uri = URI.create(paramsUrl.toString());
            ResponseEntity<String> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            JSONObject json = JSONObject.parseObject(resp.getBody());
            log.info("[getProductDetail] sku: {}, objectId: {}, result: {}", model.getSku(), model.getObjectID(), resp.getBody());
            JSONArray stocksJson = json.getJSONArray("ProductActivity");

            Map<SizeChartEnum, StockXStockInfo> stocks = Maps.newHashMap();
            for (int i = 0; i < stocksJson.size(); i++) {
                JSONObject stock = stocksJson.getJSONObject(i);
                StockXStockInfo stockInfo = new StockXStockInfo();
                stockInfo.setSize(stock.getString("shoeSize"));
                stockInfo.setState(stock.getInteger("state"));
                stockInfo.setAmount(stock.getDouble("amount"));
                stockInfo.setDateStr(stock.getString("createdAt"));

                SizeChartEnum sizeEnum =  SizeChartEnum.getBySizeUS(stockInfo.getSize());
                if(null == sizeEnum){
                    continue;
                }
                if(null != stocks.get(sizeEnum)){
                    if(formatter.parse(stockInfo.getDateStr()).after(formatter.parse(stocks.get(sizeEnum).getDateStr()))) {
                        log.info("duplicate key and replace, stock: {}, stockInMap: {}", stockInfo, stocks.get(sizeEnum));
                        stocks.put(sizeEnum, stockInfo);
                    } else {
                        log.info("duplicate key, stock: {}, stockInMap: {}", stockInfo, stocks.get(sizeEnum));
                    }
                } else{
                    stocks.put(sizeEnum, stockInfo);
                }
            }
            model.setStocks(stocks);

            log.info("searched objectId: {}, sku: {}, model: {}", model.getObjectID(), model.getSku(), model);
            return model;
        } catch (Exception e){
            log.error("[getProductDetail] e: ", e);
            count++;
            if(count < 3){
                getProductDetail(model);
            }
        }

        return null;
    }

    @Override
    public StockXShoeListModel getProductDetail2(StockXShoeListModel model) {
        int count = 0;
        try{
            if(null == model || Strings.isNullOrEmpty(model.getObjectID())){
                return null;
            }
            MultiValueMap<String, String> headers = new LinkedMultiValueMap();
            headers.add("jwt-authorization", "");
            headers.add("x-api-key", "99WtRZK6pS1Fqt8hXBfWq8BYQjErmwipa3a0hYxX");

            HttpEntity<String> entity = new HttpEntity<>("", headers);
            StringBuffer paramsUrl = new StringBuffer("https://gateway.stockx.com/api/v2/products/");
            paramsUrl.append(model.getObjectID());
            paramsUrl.append("?includes=market,360&currency=USD");

            URI uri = URI.create(paramsUrl.toString());
            ResponseEntity<String> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            JSONObject json = JSONObject.parseObject(resp.getBody());
            log.info("[getProductDetail2] sku: {}, objectId: {}, result: {}", model.getSku(), model.getObjectID(), resp.getBody());
            JSONObject stocksJson = json.getJSONObject("Product").getJSONObject("children");

            Map<SizeChartEnum, StockXStockInfo> stocks = Maps.newHashMap();

            for(String key : stocksJson.keySet()) {
                JSONObject stock = stocksJson.getJSONObject(key);
                StockXStockInfo stockInfo = new StockXStockInfo();
                stockInfo.setSize(stock.getString("shoeSize"));
                stockInfo.setAmount(stock.getJSONObject("market").getDouble("lowestAsk"));
                stockInfo.setDateStr(stock.getJSONObject("market").getString("createdAt"));

                SizeChartEnum sizeEnum =  SizeChartEnum.getBySizeUS(stockInfo.getSize());
                if(null != sizeEnum && stockInfo.getAmount() > 0d){
                    stocks.put(sizeEnum, stockInfo);
                }
            }
//            for (int i = 0; i < stocksJson.size(); i++) {
//                JSONObject stock = stocksJson.getJSONObject(i);
//
//                StockXStockInfo stockInfo = new StockXStockInfo();
//                stockInfo.setSize(stock.getString("shoeSize"));
//                stockInfo.setState(stock.getInteger("state"));
//                stockInfo.setAmount(stock.getDouble("amount"));
//                stockInfo.setDateStr(stock.getString("createdAt"));
//
//                SizeChartEnum sizeEnum =  SizeChartEnum.getBySizeUS(stockInfo.getSize());
//                if(null == sizeEnum){
//                    continue;
//                }
//                if(null != stocks.get(sizeEnum)){
//                    if(formatter.parse(stockInfo.getDateStr()).after(formatter.parse(stocks.get(sizeEnum).getDateStr()))) {
//                        log.info("duplicate key and replace, stock: {}, stockInMap: {}", stockInfo, stocks.get(sizeEnum));
//                        stocks.put(sizeEnum, stockInfo);
//                    } else {
//                        log.info("duplicate key, stock: {}, stockInMap: {}", stockInfo, stocks.get(sizeEnum));
//                    }
//                } else{
//                    stocks.put(sizeEnum, stockInfo);
//                }
//            }
            model.setStocks(stocks);

            System.out.println(model.getStocks());
            log.info("searched objectId: {}, sku: {}, model: {}", model.getObjectID(), model.getSku(), model);
            return model;
        } catch (Exception e){
            log.error("[getProductDetail] e: ", e);
            count++;
            if(count < 3){
                getProductDetail(model);
            }
        }

        return null;
    }


    @Override
    public StockXShoeListModel searchItem(String sku) {
        int count = 0;
        try{
            MultiValueMap<String, String> headers = new LinkedMultiValueMap();
            headers.add("jwt-authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzdG9ja3guY29tIiwic3ViIjoic3RvY2t4LmNvbSIsImF1ZCI6IndlYiIsImFwcF9uYW1lIjoiaW9zIiwiYXBwX3ZlcnNpb24iOiIzLjguNC4xNDcwNyIsImlzc3VlZF9hdCI6IjIwMTgtMTEtMTkgMDc6Mzg6MDgiLCJjdXN0b21lcl9pZCI6bnVsbCwiZW1haWwiOm51bGwsImN1c3RvbWVyX3V1aWQiOm51bGwsImZpcnN0TmFtZSI6bnVsbCwibGFzdE5hbWUiOm51bGwsImdkcHJfc3RhdHVzIjpudWxsLCJkZWZhdWx0X2N1cnJlbmN5IjoiVVNEIiwic2hpcF9ieV9kYXRlIjpudWxsLCJ2YWNhdGlvbl9kYXRlIjpudWxsLCJwcm9kdWN0X2NhdGVnb3J5Ijoic25lYWtlcnMiLCJpc19hZG1pbiI6bnVsbCwic2Vzc2lvbl9pZCI6IjEyOTM5Njk2ODE0ODU1OTI5NTcxIiwiZXhwIjoxNTQzMjE3ODg4LCJhcGlfa2V5cyI6bnVsbH0.Xh3JtTENx0llMUhKiB7A1zMxTJXqrdpF2S6NxVjHBR4");
            headers.add("x-api-key", "99WtRZK6pS1Fqt8hXBfWq8BYQjErmwipa3a0hYxX");


            HttpEntity<String> entity = new HttpEntity<>("", headers);
            StringBuffer paramsUrl = new StringBuffer("https://gateway.stockx.com/api/v2/search?facets=%5B%22product_category%22%5D&page=0&currency=USD");
            paramsUrl.append("&query=" + URLEncoder.encode(sku, "utf-8"));
            URI uri = URI.create(paramsUrl.toString());
            ResponseEntity<String> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String result = resp.getBody();

            log.info("[searchItem] result: {}", result);
            JSONObject json = JSONObject.parseObject(result);
            JSONArray hits = json.getJSONArray("hits");
            if(null != hits && hits.size() >= 1){
                JSONObject hit = hits.getJSONObject(0);
                if(Strings.isNullOrEmpty(hit.getString("style_id"))){
                    return null;
                }
                String styleId = hit.getString("style_id").replace("-", " ");
                if(!styleId.equalsIgnoreCase(sku)){
                    log.error("[searchItem] not match, sku: {}, styleId: {}", sku, styleId);
                    return null;
                }
                StockXShoeListModel model = new StockXShoeListModel();
                model.setSku(sku);
                model.setName(hit.getString("name"));
                model.setColorway(hit.getString("colorway"));
                model.setRelease_date(hit.getString("release_date"));
                model.setObjectID(hit.getString("objectID"));

                if(Strings.isNullOrEmpty(model.getObjectID())){
                    log.error("[searchItem] objectId is null, sku: {}", sku);
                    return null;
                }
                return model;
            }
        } catch (Exception e){
            log.error("[searchItem] error. sku: " + sku + ", e: ", e);
            count++;
            if(count < 3){
                searchItem(sku);
            }
        }
        log.error("[searchItem] object not found, sku: {}", sku);

        return null;
    }

}
