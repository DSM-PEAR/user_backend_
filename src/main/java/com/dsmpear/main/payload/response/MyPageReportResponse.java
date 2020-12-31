package com.dsmpear.main.payload.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyPageReportResponse {

    private Integer reportId;

    private String title;

    private LocalDateTime createdAt;

    private Boolean isSubmitted;

    private Boolean isAccepted; // 승인 or 미승인

}
