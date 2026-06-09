package com.wj.aisoulmatechat.constants;

public interface Constant {

    interface Tools {
        String ARCHIVE_IMPORTANT_SCENE = "当AI伴侣需要记忆用户某些信息或事件或用户说让记住某些话的时候调用；永久记住用户个人信息、生日、饮食偏好、重要纪念日、私人经历等伴侣需要记住的事件。";
        String GET_WEATHER = "查询指定经纬度的实时天气和今日预报,如果上下文有用户的所在位置则优先获取用户所在城市的天气，否则获取AI人设所在城市的天气（当用户提到天气或者AI伴侣想获取天气时调用此工具）";
        String GET_TIME = "获取当前日期、时间（当用户提到当前日期、时间或者AI伴侣想获取当前日期、时间时调用此工具）";

    }

    interface ToolParams {
        String IMPORTANT_SCENE = "需要长期留存的伴侣记忆内容";
        String WEATHER_CITY = "城市";
    }

}
