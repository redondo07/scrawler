package com.ywb.scrawler.constants;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "calculate")
@Getter
@Setter
public class CalculateConstants {
    private Double currency;
    private Double shippingAndTaxUSD;
    private Double profitRate;
}
