package com.wj.aisoulmatechat.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wj.aisoulmatechat.entity.tool.OpenMeteoWeather;
import com.wj.aisoulmatechat.enums.WmoWeatherEnum;
import com.wj.aisoulmatechat.service.AiToolsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiToolsServiceImpl implements AiToolsService {
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Override
    public String getWeatherByLatLon(double lat, double lon) {
        try {
            String jsonStr = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.open-meteo.com")
                            .path("/v1/forecast")
                            .queryParam("latitude", lat)
                            .queryParam("longitude", lon)
                            .queryParam("current", "temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m")
                            .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min")
                            .queryParam("timezone", "auto").build()
                    )
                    .retrieve()
                    .body(String.class);

            OpenMeteoWeather weather = objectMapper.readValue(jsonStr, OpenMeteoWeather.class);
            OpenMeteoWeather.Current current = weather.getCurrent();
            OpenMeteoWeather.Daily daily = weather.getDaily();

            return String.format("""
                            位置：纬度%.2f，经度%.2f
                            当前温度：%.1f℃，体感温度：%.1f℃
                            空气湿度：%d%%，风速：%.1f米/秒
                            今日天气：%s
                            今日温度区间：%.1f℃ ~ %.1f℃
                            """,
                    lat, lon,
                    current.getTemperature_2m(), current.getApparent_temperature(),
                    current.getRelative_humidity_2m(),
                    current.getWind_speed_10m(),
                    getWeatherDesc(daily.getWeather_code().get(0)),
                    daily.getTemperature_2m_min().get(0),
                    daily.getTemperature_2m_max().get(0)
            );
        } catch (Exception e) {
            log.error("天气接口调用异常", e);
            return "天气查询失败，请稍后重试";
        }
    }

    /**
     * 天气编码转中文描述
     */
    private String getWeatherDesc(int code) {
        return WmoWeatherEnum.getDescByCode(code);
    }

}
