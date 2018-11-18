package com.ywb.scrawler.service.impl;

import com.ywb.scrawler.service.NiceApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class NiceApiServiceImpl implements NiceApiService {
    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {

        System.out.println("start init");
        this.getListTab();

        System.out.println("end init");


    }

    @Override
    public String getListTab() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap();
        headers.add("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>("nice-sign-v1://c6d3ce5697413129b9488fc537bd6297:73d6739ddd9cb07c/{\"token\":\"8dvGFRNl8izMftPmiwfbPpiCwigS2NLL\",\"tab\":\"hot\",\"type\":\"All\",\"categoryIds\":\"\",\"nextkey\":\"\"}", headers);
        ResponseEntity<String> resp = restTemplate.exchange("http://api.oneniceapp.com/product/listTab?abroad=no&appv=5.2.14.20&did=eeb4f168016f955f9ebe4365f4f63656&dn=Wenbiao%E7%9A%84%20iPhone&dt=iPhone10%2C3&geoacc=11&im=52DDFF9C-6CFE-4D53-AE78-56091BAD8269&la=cn&lm=weixin&lp=-1.000000&net=0-0-wifi&osn=iOS&osv=12.1&seid=f3d7675ce55ab0c8c1baeb1d49aec0d4&sh=812.000000&sw=375.000000&token=8dvGFRNl8izMftPmiwfbPpiCwigS2NLL&ts=1542535786813",
                HttpMethod.POST, entity, String.class);
        String result = resp.getBody();

        System.out.println(result);
        return null;
    }
}
