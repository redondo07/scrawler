package com.ywb.scrawler.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ywb.scrawler.SizeChartEnum;
import com.ywb.scrawler.model.NiceShoeListModel;
import com.ywb.scrawler.model.NiceStockInfo;
import com.ywb.scrawler.service.NiceApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
public class NiceApiServiceImpl implements NiceApiService {
    private final static Logger log = LoggerFactory.getLogger(NiceApiServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {
        // get all products
//        List<NiceShoeListModel> result = Lists.newArrayList();
//        List<NiceShoeListModel> models = this.getProductList();
//        for(NiceShoeListModel model : models){
//            result.add(this.getProductDetail(model));
//        }
    }

    @Override
    public NiceShoeListModel getProductDetail(NiceShoeListModel model) {
        try{
            ResponseEntity<String> resp = restTemplate.getForEntity(
                    "http://sneakers-wxmp.oneniceapp.com/product/stocks/" + model.getId(), String.class);
            JSONObject json = JSONObject.parseObject(resp.getBody());
            JSONArray stocksJson = json.getJSONObject("data").getJSONArray("stocks");

            log.info("[getProductDetail] stocks: {}", stocksJson.toJSONString());

            Map<SizeChartEnum, NiceStockInfo> stocks = Maps.newHashMap();
            for (int i = 0; i < stocksJson.size(); i++) {
                JSONObject stock = stocksJson.getJSONObject(i);
                NiceStockInfo stockInfo = new NiceStockInfo();
                stockInfo.setSize(stock.getString("size"));
                stockInfo.setPrice(stock.getDouble("price"));
                stockInfo.setStock(stock.getInteger("stock"));
                stockInfo.setDesc(stock.getString("desc"));
                SizeChartEnum sizeEnum =  SizeChartEnum.getBySizeEU(stockInfo.getSize());
                if(null == sizeEnum){
                    continue;
                }
                if(null != stocks.get(sizeEnum)){
                    log.info("duplicate key, stock: {}, stockInMap: {}", stockInfo, stocks.get(sizeEnum));
                }
                stocks.put(SizeChartEnum.getBySizeEU(stockInfo.getSize()), stockInfo);
            }
            model.setStocks(stocks);

            return model;
        } catch (Exception e){
            log.error("[getProductDetail]", e);
        }
        return null;

    }

    @Override
    public List<NiceShoeListModel> getProductList() {
        // first screen
        List<NiceShoeListModel> items = Lists.newArrayList();
        Map<String, NiceShoeListModel> map = Maps.newHashMap();

        for(NiceShoeListModel model : this.loadFirstScreen()){
            if(null == map.get(model.getSku())){
                map.put(model.getSku(), model);
            }
        }

        // load more
        int nextKey = 10;
        for(int i = 0; i < 10; i++){
            for(NiceShoeListModel model : this.loadMore(nextKey)){
                if(null == map.get(model.getSku())){
                    map.put(model.getSku(), model);
                }
            }
            nextKey += 10;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(Map.Entry<String, NiceShoeListModel> entry : map.entrySet()){
            items.add(entry.getValue());
        }

        System.out.println(items.size());

        for(NiceShoeListModel model : items){
            System.out.println(model.getSku());
        }

        return items;
    }

    private List<NiceShoeListModel> loadFirstScreen(){
        ResponseEntity<String> resp = restTemplate.getForEntity(
                "http://sneakers-wxmp.oneniceapp.com/index/first_screen", String.class);
        JSONObject json = JSONObject.parseObject(resp.getBody());
        JSONArray tabList = json.getJSONObject("data").getJSONArray("tab_list");

        for (int i = 0; i < tabList.size(); i++) {
            JSONObject tab = tabList.getJSONObject(i);
            if(tab.getString("title").equalsIgnoreCase("球鞋")){
                JSONArray products = tab.getJSONArray("hot_products");
                return this.buildProducts(products);
            }
        }

        return Lists.newArrayList();
    }

    private List<NiceShoeListModel> loadMore(int nextKey){
        HttpEntity<String> entity = new HttpEntity<>(
                "categories=%5B5%5D&sub_type=hot&nextkey=" + nextKey, new LinkedMultiValueMap());
        ResponseEntity<String> resp = restTemplate.exchange(
                "http://sneakers-wxmp.oneniceapp.com/index/load_more",
                HttpMethod.POST, entity, String.class);
        String result = resp.getBody();

        JSONObject json = JSONObject.parseObject(result);
        JSONArray list = json.getJSONObject("data").getJSONArray("products");


        List<NiceShoeListModel> resultList = this.buildProducts(list);
//        System.out.println("categories=%5B5%5D&sub_type=hot&nextkey=" + nextKey);
//
//        for(NiceShoeListModel model : resultList){
//            System.out.println(model.getSku());
//        }
        return resultList;
    }

    private List<NiceShoeListModel> buildProducts(JSONArray products){
        List<NiceShoeListModel> items = Lists.newArrayList();

        for (int i = 0; i < products.size(); i++) {
            JSONObject item = products.getJSONObject(i);

            NiceShoeListModel model = new NiceShoeListModel();
            model.setId(item.getString("id"));
            model.setName(item.getString("name"));
            model.setCover(item.getString("cover"));
            model.setSku(item.getString("sku"));
            model.setDeal_num(item.getString("deal_num"));

            items.add(model);
        }

        return items;
    }

    @Deprecated
    private List<NiceShoeListModel> getDataByEncryptedUrl(){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap();
        headers.add("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>("nice-sign-v1://c6d3ce5697413129b9488fc537bd6297:73d6739ddd9cb07c/{\"token\":\"8dvGFRNl8izMftPmiwfbPpiCwigS2NLL\",\"tab\":\"hot\",\"type\":\"All\",\"categoryIds\":\"\",\"nextkey\":\"\"}", headers);
        ResponseEntity<String> resp = restTemplate.exchange("http://api.oneniceapp.com/product/listTab?abroad=no&appv=5.2.14.20&did=eeb4f168016f955f9ebe4365f4f63656&dn=Wenbiao%E7%9A%84%20iPhone&dt=iPhone10%2C3&geoacc=11&im=52DDFF9C-6CFE-4D53-AE78-56091BAD8269&la=cn&lm=weixin&lp=-1.000000&net=0-0-wifi&osn=iOS&osv=12.1&seid=f3d7675ce55ab0c8c1baeb1d49aec0d4&sh=812.000000&sw=375.000000&token=8dvGFRNl8izMftPmiwfbPpiCwigS2NLL&ts=1542535786813",
                HttpMethod.POST, entity, String.class);
        String result = resp.getBody();

        JSONObject json = JSONObject.parseObject(result);
        JSONArray list = json.getJSONObject("data").getJSONArray("list");

        List<NiceShoeListModel> items = Lists.newArrayList();
        for (int i = 0; i < list.size(); i++) {
            JSONObject item = list.getJSONObject(i);
            NiceShoeListModel model = new NiceShoeListModel();
            model.setId(item.getString("id"));
            model.setName(item.getString("name"));
            model.setCover(item.getString("cover"));
            model.setRelease_time(item.getString("release_time"));
            model.setSku(item.getString("sku"));
            model.setDeal_num(item.getString("deal_num"));

            items.add(model);
        }

        return items;
    }


}
