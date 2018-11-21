package com.ywb.scrawler.service;

import com.ywb.scrawler.model.NiceSaleListModel;
import com.ywb.scrawler.model.NiceShoeListModel;

import java.util.List;

public interface NiceApiService {

    List<NiceShoeListModel> getProductList();

    NiceShoeListModel getProductDetail(NiceShoeListModel model);

    List<NiceSaleListModel> getSaleList();

}
