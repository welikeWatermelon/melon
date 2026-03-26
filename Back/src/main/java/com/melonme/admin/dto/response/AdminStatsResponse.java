package com.melonme.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminStatsResponse {

    private long wau;
    private long mau;
    private long weeklyPostCount;
    private double uploaderRatio;
    private double retentionRate;
}
