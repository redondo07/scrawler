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
    private Double shippingFee;
    private Double tax = 7d;
    private Double profitRate = 0.1;
}
