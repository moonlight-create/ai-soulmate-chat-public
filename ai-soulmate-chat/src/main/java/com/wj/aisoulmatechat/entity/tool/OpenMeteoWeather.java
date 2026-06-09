package com.wj.aisoulmatechat.entity.tool;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoWeather {
    private Double latitude;
    private Double longitude;
    private Current current;
    private Daily daily;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {
        private String time;
        private Double temperature_2m;
        private Integer relative_humidity_2m;
        private Double apparent_temperature;
        private Double wind_speed_10m;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Daily {
        private List<String> time;
        private List<Double> temperature_2m_max;
        private List<Double> temperature_2m_min;
        private List<Integer> weather_code;
    }
}
