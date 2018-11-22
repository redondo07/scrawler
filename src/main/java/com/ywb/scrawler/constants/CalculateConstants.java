package com.ywb.scrawler.constants;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Component
@ConfigurationProperties(prefix = "calculate")
@Getter
@Setter
public class CalculateConstants {
    private static DecimalFormat format = new DecimalFormat("0.00");

    private Double currency;
    private Double shippingAndTaxUSD;
    private Double profitRate;


    public static BigDecimal calculateProfitRate(Double nicePrice, Double stockXPriceRmb){
        BigDecimal profitRate = new BigDecimal(
                format.format((nicePrice - stockXPriceRmb) / nicePrice))
                .setScale(2);

        return profitRate;
    }

    public Double getCalculatedStockXPriceRmb(Double stockXAmount){
        return (stockXAmount + this.shippingAndTaxUSD) * this.currency;
    }

    public static Double getCalculatedNicePrice(Double nicePrice){
        return (nicePrice - 10d) * 0.98d;
    }

    public static Double getSuggestNicePrice(Double nicePrice, Double stockXPriceRmb){
        return nicePrice - (nicePrice - stockXPriceRmb) * 0.1d;
    }

}
