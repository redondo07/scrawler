package com.ywb.scrawler.enums;

public enum SizeChartEnum {

    US_7("7", "40"),
    US_75("7.5", "40.5"),
    US_8("8", "41"),
    US_85("8.5", "42"),
    US_9("9", "42.5"),
    US_95("9.5", "43"),
    US_10("10", "44"),
    US_105("10.5", "44.5"),
    US_11("11", "45"),
    US_115("11.5", "45.5"),
    US_12("12", "46"),

    ;

    private String sizeUS;
    private String sizeEU;

    SizeChartEnum(String sizeUS, String sizeEU) {
        this.sizeUS = sizeUS;
        this.sizeEU = sizeEU;
    }

    public static SizeChartEnum getBySizeUS(String sizeUS) {
        for(SizeChartEnum size : SizeChartEnum.values()){
            if(sizeUS.equalsIgnoreCase(size.sizeUS)){
                return size;
            }
        }
        return null;
    }

    public static SizeChartEnum getBySizeEU(String sizeEU) {
        for(SizeChartEnum size : SizeChartEnum.values()){
            if(sizeEU.equalsIgnoreCase(size.sizeEU)){
                return size;
            }
        }
        return null;
    }

    public String getSizeUS() {
        return sizeUS;
    }

    public String getSizeEU() {
        return sizeEU;
    }
}
