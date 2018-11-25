package com.ywb.scrawler.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ywb.scrawler.enums.SizeChartEnum;
import com.ywb.scrawler.model.NiceSaleListModel;
import com.ywb.scrawler.model.NiceShoeListModel;
import com.ywb.scrawler.model.NiceStockInfo;
import com.ywb.scrawler.service.NiceApiService;
import com.ywb.scrawler.service.NiceStockInfoPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class NiceApiServiceImpl implements NiceApiService {
    private final static Logger log = LoggerFactory.getLogger(NiceApiServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private NiceStockInfoPageService niceStockInfoPageService;

    private final Map<String, String> mapNextKeyToData = ImmutableMap.of(
            "", "nice-sign-v1://1882c3ca9becbbf0937c7e6294f8e630:4c1941b83ebb5692/{\"token\":\"arQAiGVk839UoBT-CBeAPItmt-wneZEF\",\"tab\":\"hot\",\"type\":\"Shoes\",\"categoryIds\":\"5\",\"nextkey\":\"\"}",
            "20", "nice-sign-v1://a253431fc88c5a6cb92cd93fe5585bf9:05fe5827867a3e85/{\"token\":\"arQAiGVk839UoBT-CBeAPItmt-wneZEF\",\"tab\":\"hot\",\"type\":\"Shoes\",\"categoryIds\":\"5\",\"nextkey\":\"20\"}",
            "40", "nice-sign-v1://d652693d8fef89f2f2d5c3987e6ae0f7:d6a4538f2bbe98ef/{\"token\":\"arQAiGVk839UoBT-CBeAPItmt-wneZEF\",\"tab\":\"hot\",\"type\":\"Shoes\",\"categoryIds\":\"5\",\"nextkey\":\"40\"}",
            "60", "nice-sign-v1://149711e9805e15370aa2ef12ef97d138:3f91ec22440e3086/{\"token\":\"arQAiGVk839UoBT-CBeAPItmt-wneZEF\",\"tab\":\"hot\",\"type\":\"Shoes\",\"categoryIds\":\"5\",\"nextkey\":\"60\"}");

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
                    if(stocks.get(sizeEnum).getPrice() > stockInfo.getPrice()){
                        stocks.put(SizeChartEnum.getBySizeEU(stockInfo.getSize()), stockInfo);
                        log.info("duplicate key and replace, stock: {}, stockInMap: {}", stockInfo, stocks.get(sizeEnum));
                    } else{
                        log.info("duplicate key, stock: {}, stockInMap: {}", stockInfo, stocks.get(sizeEnum));
                    }
                } else{
                    stocks.put(SizeChartEnum.getBySizeEU(stockInfo.getSize()), stockInfo);
                }
            }
            model.setStocks(stocks);

            return model;
        } catch (Exception e){
            log.error("[getProductDetail] e: ", e);
        }
        return null;

    }

    @Override
    public List<NiceSaleListModel> getSaleList() {
        List<NiceSaleListModel> result = Lists.newArrayList();

        try{
            HttpEntity<String> entity = new HttpEntity<>(
                    "nice-sign-v1://e57f2dc866c9381102d2d0c943e83ca5:9f76a740a0027d2f/{\"nextkey\":\"\",\"status\":\"pass\",\"token\":\"arQAiGVk839UoBT-CBeAPItmt-wneZEF\"}", new LinkedMultiValueMap());
            ResponseEntity<String> resp = restTemplate.exchange(
                    "http://115.182.19.34/Sneakersale/getsaleList?abroad=no&appv=5.2.14.20&ch=AppStore_5.2.14.20&did=eeb4f168016f955f9ebe4365f4f63656&dn=Wenbiao%E7%9A%84%20iPhone&dt=iPhone10%2C3&geoacc=10&im=52DDFF9C-6CFE-4D53-AE78-56091BAD8269&la=cn&lm=mobile&lp=-1.000000&net=0-0-wifi&osn=iOS&osv=12.1&seid=f8e475ab8669d6321430fb1cd4abe5f9&sh=812.000000&src=user_live&sw=375.000000&token=arQAiGVk839UoBT-CBeAPItmt-wneZEF&ts=1542795738869",
                    HttpMethod.POST, entity, String.class);
            String body = resp.getBody();
            JSONArray json = JSONObject.parseObject(body).getJSONObject("data").getJSONArray("list");

            for(int i = 0; i < json.size(); i++){
                try{
                    JSONObject item = json.getJSONObject(i);

                    // TODO 判断下架产品
                    boolean isOnSale = true;
                    if(!isOnSale){
                        continue;
                    }

                    // text不为空，表示有多个产品
                    if(!Strings.isNullOrEmpty(item.getString("text"))) {
                        JSONObject goodInfo = item.getJSONObject("goods_info");

                        // 爬取html获取多个尺码商品价格
                        List<NiceSaleListModel> models = niceStockInfoPageService.getStockInfoByGoodsId(goodInfo.getString("id"));
                        for(NiceSaleListModel model : models){
                            model.setCover(goodInfo.getString("cover"));
                            model.setName(goodInfo.getString("name"));
                            model.setSku(goodInfo.getString("sku"));
                        }

                        result.addAll(models);
                    } else {
                        NiceSaleListModel model = new NiceSaleListModel();
                        JSONObject goodInfo = item.getJSONObject("goods_info");
                        model.setCover(goodInfo.getString("cover"));
                        model.setName(goodInfo.getString("name"));
                        model.setSize(goodInfo.getString("size").replace("码", ""));
                        model.setSku(goodInfo.getString("sku"));
                        model.setSalePrice(item.getDouble("price"));

                        result.add(model);
                    }
                } catch(Exception e){
                    log.error("[getSaleList] e: ", e);
                }
            }
        } catch (Exception e){
            log.error("[getSaleList] e: ", e);
        }
        System.out.println(result);

        return result;
    }

    @Override
    public List<NiceShoeListModel> getProductList() {
        return this.getDataByEncryptedUrl();

        // first screen
//        List<NiceShoeListModel> items = Lists.newArrayList();
//        Map<String, NiceShoeListModel> map = Maps.newHashMap();
//
//        for(NiceShoeListModel model : this.loadFirstScreen()){
//            if(null == map.get(model.getSku())){
//                map.put(model.getSku(), model);
//            }
//        }
//
//        // load more
//        int nextKey = 10;
//        for(int i = 0; i < 10; i++){
//            for(NiceShoeListModel model : this.loadMore(nextKey)){
//                if(null == map.get(model.getSku())){
//                    map.put(model.getSku(), model);
//                }
//            }
//            nextKey += 10;
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        for(Map.Entry<String, NiceShoeListModel> entry : map.entrySet()){
//            items.add(entry.getValue());
//        }
//
//        System.out.println(items.size());
//
//        for(NiceShoeListModel model : items){
//            System.out.println(model.getSku());
//        }
//
//        return items;
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

    private List<NiceShoeListModel> getDataByEncryptedUrl(){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap();
        headers.add("Content-Type", "application/json");

        List<NiceShoeListModel> items = Lists.newArrayList();

        for(Map.Entry<String, String> entry : mapNextKeyToData.entrySet()){
            HttpEntity<String> entity = new HttpEntity<>(entry.getValue(), headers);
            ResponseEntity<String> resp = restTemplate.exchange("http://api.oneniceapp.com/product/listTab?abroad=no&amap_latitude=40.043895&amap_longitude=116.289610&appv=5.2.14.20&ch=AppStore_5.2.14.20&did=eeb4f168016f955f9ebe4365f4f63656&dn=Wenbiao%E7%9A%84%20iPhone&dt=iPhone10%2C3&geoacc=10&im=52DDFF9C-6CFE-4D53-AE78-56091BAD8269&la=cn&latitude=40.042652&lm=mobile&longitude=116.283545&lp=-1.000000&net=0-0-wifi&osn=iOS&osv=12.1&seid=cbbd6f2700ee84d9580f0e3a11c43d80&sh=812.000000&sw=375.000000&token=arQAiGVk839UoBT-CBeAPItmt-wneZEF&ts=1542801272367",
                    HttpMethod.POST, entity, String.class);

            JSONObject json = JSONObject.parseObject(resp.getBody());
            log.info("[getDataByEncryptedUrl] key: {}, response: {}", entry.getKey(), json.toJSONString());

            JSONArray list = json.getJSONObject("data").getJSONArray("list");

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
        }

        for(NiceShoeListModel item : items){
            System.out.println(item.getSku());
        }
        return items;
    }


}
