package com.ywb.scrawler.service.impl;

import com.google.common.collect.Lists;
import com.ywb.scrawler.dao.StockDao;
import com.ywb.scrawler.enums.SizeChartEnum;
import com.ywb.scrawler.constants.CalculateConstants;
import com.ywb.scrawler.model.*;
import com.ywb.scrawler.service.NiceApiService;
import com.ywb.scrawler.service.StockCalculateService;
import com.ywb.scrawler.service.StockXService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StockCalculateServiceImpl implements StockCalculateService {
    @Resource
    private NiceApiService niceApiService;
    @Resource
    private StockXService stockXService;
    @Resource
    private CalculateConstants calculateConstants;
    @Resource
    private StockDao stockDao;

    private static DecimalFormat format = new DecimalFormat("0.00");

   //  @PostConstruct
    private void init(){
        List<StockCalculatedRef> result = this.calculateDiff();
        log.info("can buy: {}", result);

        saveToExcel(result);
    }


    @Override
    public List<StockCalculatedRef> calculateDiff() {
        List<StockCalculatedRef> result = Lists.newArrayList();

        List<NiceShoeListModel> top100Nice = niceApiService.getProductList();
        int count = 0;
        System.out.println("topNice size: " + top100Nice.size());
        for(NiceShoeListModel niceModel : top100Nice){
             niceModel = niceApiService.getProductDetail(niceModel);
            if(null == niceModel){
                continue;
            }
            StockXShoeListModel stockXModel = stockXService.searchItem(niceModel.getSku());
            stockXModel = stockXService.getProductDetail(stockXModel);

            if(null == stockXModel){
                continue;
            }

            for(Map.Entry<SizeChartEnum, NiceStockInfo> entry : niceModel.getStocks().entrySet()){
                SizeChartEnum sizeEnum = entry.getKey();
                NiceStockInfo stockNice = entry.getValue();

                StockXStockInfo stockStockX = stockXModel.getStocks().get(sizeEnum);

                if(null != stockStockX){
                    Double calculatedStockXPrice = (stockStockX.getAmount() + calculateConstants.getShippingAndTaxUSD()) * calculateConstants.getCurrency();
                    Double calculatedNicePrice = (stockNice.getPrice() - 10d) * 0.95d;

                    if(calculatedNicePrice > calculatedStockXPrice){
                        BigDecimal profitRate = new BigDecimal(
                                format.format((calculatedNicePrice - calculatedStockXPrice) / calculatedStockXPrice))
                                .setScale(2);

                        if(profitRate.compareTo(BigDecimal.valueOf(calculateConstants.getProfitRate())) > 0 &&
                                profitRate.compareTo(BigDecimal.ONE) < 0){
                            StockCalculatedRef ref = new StockCalculatedRef();
                            ref.setSku(niceModel.getSku());
                            ref.setCalculateStockXPriceRmb(calculatedStockXPrice);
                            ref.setPriceNice(stockNice.getPrice());
                            ref.setCalculatedNicePriceRmb(calculatedNicePrice);
                            ref.setPriceStockX(stockStockX.getAmount());
                            ref.setProfit(stockNice.getPrice() - calculatedStockXPrice);
                            ref.setProfitRate(profitRate.doubleValue());
                            ref.setSizeEU(sizeEnum.getSizeEU());
                            ref.setSizeUS(sizeEnum.getSizeUS());
                            ref.setDesc(stockNice.getDesc());
                            ref.setImgUrl(niceModel.getCover());
                            ref.setName(niceModel.getName());
                            result.add(ref);

                            stockDao.insert(ref.buildStockModel());
                        }
                    }
                }
            }

            System.out.println("Wake up " + count++);
        }

        log.info("[calculateDiff] result: {}", result);
        saveToExcel(result);

        return result;
    }

    private void saveToExcel(List<StockCalculatedRef> refs) {
        Workbook workbook = new XSSFWorkbook();
        // CreationHelper createHelper = workbook.getCreationHelper();
        Sheet sheet = workbook.createSheet("bestbuy");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        List<String> headers = Lists.newArrayList("name", "sku", "sizeUS", "sizeEU", "priceNice",
                "priceStockX", "calculatedNicePriceRmb", "calculateStockXPriceRmb", "profit", "profitRate", "cover");
        for(int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        for(StockCalculatedRef ref : refs){
            Row row = sheet.createRow(rowNum++);
            int cellNum = 0;
            row.createCell(cellNum++).setCellValue(ref.getName());
            row.createCell(cellNum++).setCellValue(ref.getSku());
            row.createCell(cellNum++).setCellValue(ref.getSizeUS());
            row.createCell(cellNum++).setCellValue(ref.getSizeEU());
            row.createCell(cellNum++).setCellValue(ref.getPriceNice());
            row.createCell(cellNum++).setCellValue(ref.getPriceStockX());
            row.createCell(cellNum++).setCellValue(ref.getCalculatedNicePriceRmb());
            row.createCell(cellNum++).setCellValue(ref.getCalculateStockXPriceRmb());
            row.createCell(cellNum++).setCellValue(ref.getProfit());
            row.createCell(cellNum++).setCellValue(ref.getProfitRate());
            row.createCell(cellNum++).setCellValue(ref.getImgUrl());

        }

//        for(int i = 0; i < headers.size(); i++) {
//            sheet.autoSizeColumn(i);
//        }

        try {
            String name = "./bestbuy/bestbuy_" + System.currentTimeMillis() + ".xlsx";
            FileOutputStream fileOut = new FileOutputStream(name);
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<StockCalculatedRef> loadFromExcel(String path) {
        return null;
    }

    private List<StockCalculatedRef> findOutOfStockItems(List<StockCalculatedRef> itemsPosted, List<StockCalculatedRef> newItems) {

        return null;
    }

    private List<StockCalculatedRef> findNewItems(List<StockCalculatedRef> itemsPosted, List<StockCalculatedRef> newItems) {

        return null;
    }

    private List<StockCalculatedRef> findExistingItems(List<StockCalculatedRef> itemsPosted, List<StockCalculatedRef> newItems) {

        return null;
    }
}
