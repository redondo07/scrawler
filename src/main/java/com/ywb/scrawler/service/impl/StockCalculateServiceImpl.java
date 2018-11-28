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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
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

    @Override
    public List<StockCalculatedRef> calculateDiff(long timeStamp) {
        List<StockCalculatedRef> result = Lists.newArrayList();

        List<NiceShoeListModel> top100Nice = niceApiService.getProductList();

        List<NiceSaleListModel> saleList = niceApiService.getSaleList();

        System.out.println("topNice size: " + top100Nice.size());

        int count = 0;
        for(NiceShoeListModel niceModel : top100Nice){
            niceModel = niceApiService.getProductDetail(niceModel);
            if(null == niceModel){
                continue;
            }
            StockXShoeListModel stockXModel = stockXService.searchItem(niceModel.getSku());
            stockXModel = stockXService.getProductDetail2(stockXModel);

            if(null == stockXModel){
                continue;
            }

            boolean found = false;
            List<String> foundSkus = Lists.newArrayList();
            for(NiceSaleListModel saleModel : saleList){
                if(saleModel.getSku().equalsIgnoreCase(niceModel.getSku())){
                    found = true;
                    foundSkus.add(niceModel.getSku());

                    SizeChartEnum sizeEnum = SizeChartEnum.getBySizeEU(saleModel.getSize());
                    if(null == sizeEnum){
                        break;
                    }

                    StockXStockInfo stockInfo = stockXModel.getStocks().get(sizeEnum);

                    StockCalculatedRef ref = new StockCalculatedRef();
                    ref.setName(saleModel.getName());
                    ref.setImgUrl(saleModel.getCover());
                    ref.setSizeUS(sizeEnum.getSizeUS());
                    ref.setSizeEU(sizeEnum.getSizeEU());
                    ref.setSku(saleModel.getSku());
                    ref.setCalculatedNicePriceRmb(saleModel.getSalePrice());
                    ref.setSalePrice(saleModel.getSalePrice());
                    result.add(ref);

                    if(null == stockInfo) {
                        ref.setStatus("stockX已无货， 需要下架");
                        continue;
                    }

                    Double calculatedStockXPriceRmb = calculateConstants.getCalculatedStockXPriceRmb(stockInfo.getAmount());
                    BigDecimal profitRate = CalculateConstants.calculateProfitRate(saleModel.getSalePrice(), calculatedStockXPriceRmb);
                    Double priceDiff = saleModel.getSalePrice() - calculatedStockXPriceRmb;
                    ref.setProfitRate(profitRate.doubleValue());
                    ref.setCalculateStockXPriceRmb(calculatedStockXPriceRmb);
                    ref.setPriceStockX(stockInfo.getAmount());
                    ref.setNewProfit(saleModel.getSalePrice() - calculatedStockXPriceRmb);

                    if(profitRate.compareTo(BigDecimal.valueOf(calculateConstants.getProfitRate())) < 0 &&
                            priceDiff < 300d){
                        ref.setStatus("价格变动，需要下架");
                    } else{
                        NiceStockInfo niceLowestPrice = niceModel.getStocks().get(sizeEnum);
                        if(null != niceLowestPrice && niceLowestPrice.getPrice() < saleModel.getSalePrice()){
                            ref.setStatus("Nice有更低价，需要调整。 currentPrice: " + saleModel.getSalePrice() + ", lowestPrice: " + niceLowestPrice.getPrice());
                        } else{
                            ref.setStatus("正常");
                        }
                    }
                }
            }

            if(found){
                Iterator<NiceSaleListModel> iter = saleList.iterator();
                while(iter.hasNext()){
                    NiceSaleListModel saleItem = iter.next();
                    for(String foundSku : foundSkus){
                        if(saleItem.getSku().equalsIgnoreCase(foundSku)){
                            iter.remove();
                            break;
                        }
                    }
                }

                continue;
            }

            for(Map.Entry<SizeChartEnum, NiceStockInfo> entry : niceModel.getStocks().entrySet()){
                SizeChartEnum sizeEnum = entry.getKey();
                NiceStockInfo stockNice = entry.getValue();

                StockXStockInfo stockStockX = stockXModel.getStocks().get(sizeEnum);

                if(null != stockStockX){
                    Double calculatedStockXPrice = calculateConstants.getCalculatedStockXPriceRmb(stockStockX.getAmount());
                    Double calculatedNicePrice = CalculateConstants.getCalculatedNicePrice(stockNice.getPrice());
                    Double suggestPrice = CalculateConstants.getSuggestNicePrice(calculatedNicePrice, calculatedStockXPrice);
                    Double priceDiff = suggestPrice - calculatedStockXPrice;

                    if(calculatedNicePrice > calculatedStockXPrice){
                        BigDecimal profitRate = CalculateConstants.calculateProfitRate(suggestPrice, calculatedStockXPrice);

                        if((profitRate.compareTo(BigDecimal.valueOf(calculateConstants.getProfitRate())) > 0 &&
                                profitRate.compareTo(BigDecimal.ONE) < 0) || priceDiff >= 300d){
                            StockCalculatedRef ref = new StockCalculatedRef();
                            ref.setSku(niceModel.getSku());
                            ref.setCalculateStockXPriceRmb(calculatedStockXPrice);
                            ref.setPriceNice(stockNice.getPrice());
                            ref.setCalculatedNicePriceRmb(suggestPrice);
                            ref.setPriceStockX(stockStockX.getAmount());
                            ref.setProfit(suggestPrice - calculatedStockXPrice);
                            ref.setProfitRate(profitRate.doubleValue());
                            ref.setSizeEU(sizeEnum.getSizeEU());
                            ref.setSizeUS(sizeEnum.getSizeUS());
                            ref.setDesc(stockNice.getDesc());
                            ref.setImgUrl(niceModel.getCover());
                            ref.setName(niceModel.getName());
                            result.add(ref);

                            // stockDao.insert(ref.buildStockModel());
                        }
                    }
                }
            }

            System.out.println("Wake up " + count++);
        }

        // search left items
        for(NiceSaleListModel saleModel : saleList){
            StockXShoeListModel stockXModel = stockXService.searchItem(saleModel.getSku());

            stockXModel = stockXService.getProductDetail(stockXModel);
            StockCalculatedRef ref = new StockCalculatedRef();
            ref.setName(saleModel.getName());
            ref.setImgUrl(saleModel.getCover());
            ref.setSizeEU(saleModel.getSize());
            ref.setSku(saleModel.getSku());
            ref.setCalculatedNicePriceRmb(saleModel.getSalePrice());
            ref.setSalePrice(saleModel.getSalePrice());

            result.add(ref);

            if(null == stockXModel){
                ref.setStatus("stockX已无货， 需要下架");
            } else{
                SizeChartEnum sizeEnum = SizeChartEnum.getBySizeEU(saleModel.getSize());

                StockXStockInfo stockInfo = stockXModel.getStocks().get(sizeEnum);
                if(null == stockInfo){
                    ref.setStatus("stockX已无货， 需要下架");
                } else{
                    Double calculatedStockXPriceRmb = calculateConstants.getCalculatedStockXPriceRmb(stockInfo.getAmount());
                    BigDecimal profitRate = CalculateConstants.calculateProfitRate(saleModel.getSalePrice(), calculatedStockXPriceRmb);
                    Double priceDiff = saleModel.getSalePrice() - calculatedStockXPriceRmb;


                    ref.setProfitRate(profitRate.doubleValue());
                    ref.setCalculateStockXPriceRmb(calculatedStockXPriceRmb);
                    ref.setPriceStockX(stockInfo.getAmount());
                    ref.setNewProfit(saleModel.getSalePrice() - calculatedStockXPriceRmb);

                    if(profitRate.compareTo(BigDecimal.valueOf(calculateConstants.getProfitRate())) < 0 &&
                            priceDiff < 300){
                        ref.setStatus("价格变动，需要下架");
                    } else{
                        ref.setStatus("正常");
                    }
                }
            }
        }
        log.info("[calculateDiff] result: {}", result);
        saveToExcel(result, timeStamp);

        return result;
    }

    private void saveToExcel(List<StockCalculatedRef> refs, long timeStamp) {
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
                "priceStockX", "calculatedNicePriceRmb", "calculateStockXPriceRmb", "profit", "profitRate", "newProfit", "status", "salePrice", "cover");
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
            row.createCell(cellNum++).setCellValue(null == ref.getPriceNice()? 0d : ref.getPriceNice());
            row.createCell(cellNum++).setCellValue(null == ref.getPriceStockX()? 0d : ref.getPriceStockX());
            row.createCell(cellNum++).setCellValue(null == ref.getCalculatedNicePriceRmb() ? 0d : ref.getCalculatedNicePriceRmb());
            row.createCell(cellNum++).setCellValue(null == ref.getCalculateStockXPriceRmb() ? 0d : ref.getCalculateStockXPriceRmb());
            row.createCell(cellNum++).setCellValue(null == ref.getProfit() ? 0d : ref.getProfit());
            row.createCell(cellNum++).setCellValue(null == ref.getProfitRate() ? 0d : ref.getProfitRate());
            row.createCell(cellNum++).setCellValue(null == ref.getNewProfit() ? 0d : ref.getNewProfit());
            row.createCell(cellNum++).setCellValue(ref.getStatus());
            row.createCell(cellNum++).setCellValue(null == ref.getSalePrice() ? 0d : ref.getSalePrice());
            row.createCell(cellNum++).setCellValue(ref.getImgUrl());
        }

//        for(int i = 0; i < headers.size(); i++) {
//            sheet.autoSizeColumn(i);
//        }

        try {
            String name = "/home/scrawler/bestbuy/bestbuy_" + timeStamp + ".xlsx";
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
