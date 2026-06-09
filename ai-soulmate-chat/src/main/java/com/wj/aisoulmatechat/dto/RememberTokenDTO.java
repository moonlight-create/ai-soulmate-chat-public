package com.wj.aisoulmatechat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RememberTokenDTO {
    private String username;
    private String series;
    private String tokenValue;
    private Date lastUsed;
}
