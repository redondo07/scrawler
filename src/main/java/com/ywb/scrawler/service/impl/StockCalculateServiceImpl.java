package com.ywb.scrawler.service.impl;

import apple.laf.JRSUIConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ywb.scrawler.SizeChartEnum;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StockCalculateServiceImpl implements StockCalculateService {
    @Autowired
    private NiceApiService niceApiService;

    @Autowired
    private StockXService stockXService;

    @Autowired
    private CalculateConstants calculateConstants;
    private static DecimalFormat format = new DecimalFormat("0.00");;

    @PostConstruct
    private void init(){
        List<StockCalculatedRef> result = this.calculateDiff();
        log.info("can buy: {}", result);

        saveToExcel(result);
    }

    private void saveToExcel(List<StockCalculatedRef> refs) {
        Workbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        Sheet sheet = workbook.createSheet("bestbuy");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        List<String> headers = Lists.newArrayList("sku", "sizeUS", "sizeEU", "priceNice",
                "priceStockX", "calculateStockXPriceRmb", "profit", "profitRate");
        for(int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        for(StockCalculatedRef ref : refs){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(ref.getSku());
            row.createCell(1).setCellValue(ref.getSizeUS());
            row.createCell(2).setCellValue(ref.getSizeEU());
            row.createCell(3).setCellValue(ref.getPriceNice());
            row.createCell(4).setCellValue(ref.getPriceStockX());
            row.createCell(5).setCellValue(ref.getCalculateStockXPriceRmb());
            row.createCell(6).setCellValue(ref.getProfit());
            row.createCell(7).setCellValue(ref.getProfitRate());
        }

        for(int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        try {
            FileOutputStream fileOut = new FileOutputStream("/Users/didi/bestbuy/bestbuy.xlsx");
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<StockCalculatedRef> calculateDiff() {
        List<StockCalculatedRef> result = Lists.newArrayList();

        List<NiceShoeListModel> top200Nice = niceApiService.getProductList();
        for(NiceShoeListModel niceModel : top200Nice){
            System.out.println("Sleep " + niceModel.getSku());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
                    Double calculatedStockXPrice = (stockStockX.getAmount() + calculateConstants.getTax()) * calculateConstants.getCurrency();
                    if(stockNice.getPrice() > calculatedStockXPrice){
                        BigDecimal profitRate = new BigDecimal(
                                format.format((stockNice.getPrice() - calculatedStockXPrice) / calculatedStockXPrice))
                                .setScale(2);

                        if(profitRate.compareTo(BigDecimal.valueOf(calculateConstants.getProfitRate())) > 0){
                            StockCalculatedRef ref = new StockCalculatedRef();
                            ref.setSku(niceModel.getSku());
                            ref.setCalculateStockXPriceRmb(calculatedStockXPrice);
                            ref.setPriceNice(stockNice.getPrice());
                            ref.setPriceStockX(stockStockX.getAmount());
                            ref.setProfit(stockNice.getPrice() - calculatedStockXPrice);
                            ref.setProfitRate(profitRate.doubleValue());
                            ref.setSizeEU(sizeEnum.getSizeEU());
                            ref.setSizeUS(sizeEnum.getSizeUS());

                            System.out.println(ref);
                            result.add(ref);
                        }
                    }
                }
            }

            System.out.println("Wake up");
        }
        return result;
    }

    public static void main(String[] args){
        double a = 2d;
        double b = 3d;
        BigDecimal profitRate = new BigDecimal(
                format.format(a / b))
                .setScale(2);
        System.out.println(profitRate);
    }
}
