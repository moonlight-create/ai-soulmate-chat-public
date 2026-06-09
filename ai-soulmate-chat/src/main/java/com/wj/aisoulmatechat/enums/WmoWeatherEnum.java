package com.wj.aisoulmatechat.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum WmoWeatherEnum {
    CODE_00("00", "晴，无云"),
    CODE_01("01", "多云，逐渐转晴"),
    CODE_02("02", "多云，无变化"),
    CODE_03("03", "多云，逐渐转阴"),
    CODE_04("04", "烟雾"),
    CODE_05("05", "霾"),
    CODE_06("06", "浮尘"),
    CODE_07("07", "扬沙"),
    CODE_08("08", "局地扬尘"),
    CODE_09("09", "尘卷风"),
    CODE_10("10", "轻雾"),
    CODE_11("11", "片雾"),
    CODE_12("12", "雾（减弱）"),
    CODE_13("13", "雾（稳定）"),
    CODE_14("14", "雾（加重）"),
    CODE_15("15", "雾伴雾凇"),
    CODE_16("16", "雾伴降水"),
    CODE_17("17", "高雾"),
    CODE_18("18", "冰雾"),
    CODE_19("19", "龙卷/水龙卷"),
    CODE_20("20", "一小时前有雾（已停）"),
    CODE_21("21", "一小时前有降水（已停）"),
    CODE_22("22", "一小时前有毛毛雨（已停）"),
    CODE_23("23", "一小时前有雨（已停）"),
    CODE_24("24", "一小时前有雪（已停）"),
    CODE_25("25", "一小时前有阵雨（已停）"),
    CODE_26("26", "一小时前有阵雪（已停）"),
    CODE_27("27", "一小时前有冰雹（已停）"),
    CODE_28("28", "一小时前有冰雾（已停）"),
    CODE_29("29", "一小时前有雷暴（已停）"),
    CODE_30("30", "沙尘暴（减弱）"),
    CODE_31("31", "沙尘暴（稳定）"),
    CODE_32("32", "沙尘暴（增强）"),
    CODE_33("33", "强沙尘暴（减弱）"),
    CODE_34("34", "强沙尘暴（稳定）"),
    CODE_35("35", "强沙尘暴（增强）"),
    CODE_36("36", "高吹雪"),
    CODE_37("37", "低吹雪"),
    CODE_38("38", "风雪+沙尘"),
    CODE_39("39", "强吹雪"),
    CODE_50("50", "间歇性小毛毛雨"),
    CODE_51("51", "连续性小毛毛雨"),
    CODE_52("52", "间歇性中毛毛雨"),
    CODE_53("53", "连续性中毛毛雨"),
    CODE_54("54", "间歇性大毛毛雨"),
    CODE_55("55", "连续性大毛毛雨"),
    CODE_56("56", "间歇性冻毛毛雨"),
    CODE_57("57", "连续性冻毛毛雨"),
    CODE_60("60", "间歇性小雨"),
    CODE_61("61", "连续性小雨"),
    CODE_62("62", "间歇性中雨"),
    CODE_63("63", "连续性中雨"),
    CODE_64("64", "间歇性大雨"),
    CODE_65("65", "连续性大雨"),
    CODE_66("66", "间歇性冻雨"),
    CODE_67("67", "连续性冻雨"),
    CODE_68("68", "间歇性雨夹雪"),
    CODE_69("69", "连续性雨夹雪"),
    CODE_70("70", "间歇性小雪"),
    CODE_71("71", "连续性小雪"),
    CODE_72("72", "间歇性中雪"),
    CODE_73("73", "连续性中雪"),
    CODE_74("74", "间歇性大雪"),
    CODE_75("75", "连续性大雪"),
    CODE_76("76", "钻石尘"),
    CODE_77("77", "雪粒"),
    CODE_78("78", "星形冰晶"),
    CODE_79("79", "冰粒（米雪）"),
    CODE_80("80", "小阵雨"),
    CODE_81("81", "中/大阵雨"),
    CODE_82("82", "强阵雨"),
    CODE_83("83", "小雨夹雪阵"),
    CODE_84("84", "大雨夹雪阵"),
    CODE_85("85", "小阵雪"),
    CODE_86("86", "大阵雪"),
    CODE_87("87", "阵雨伴小冰雹"),
    CODE_88("88", "阵雪伴冰雹"),
    CODE_89("89", "强冰雹"),
    CODE_90("90", "远处雷暴（无降水）"),
    CODE_91("91", "弱雷暴+小雨"),
    CODE_92("92", "弱雷暴+大雨"),
    CODE_93("93", "弱雷暴+小雪"),
    CODE_94("94", "弱雷暴+降雪"),
    CODE_95("95", "中等雷暴"),
    CODE_96("96", "雷暴+小冰雹"),
    CODE_97("97", "强雷暴"),
    CODE_98("98", "雷暴+沙尘大风"),
    CODE_99("99", "强雷暴+大冰雹"),

    UNKNOWN("999", "未知天气");

    private final String code;
    private final String desc;

    private static final Map<String, WmoWeatherEnum> CODE_MAP = new HashMap<>();

    static {
        for (WmoWeatherEnum e : values()) {
            CODE_MAP.put(e.code, e);
        }
    }

    WmoWeatherEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    @JsonValue
    public String getDesc() {
        return desc;
    }

    public static String getDescByCode(int code) {
        String key = String.format("%02d", code);
        WmoWeatherEnum e = CODE_MAP.get(key);
        return e == null ? UNKNOWN.desc : e.desc;
    }
}
