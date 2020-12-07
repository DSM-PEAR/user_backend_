package com.dsmpear.main.payload.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ReportListResponse extends PageResponse{

    private List<ReportResponse> reportResponses;

    @Builder
    public ReportListResponse(int totalElements, int totalPages, List<ReportResponse> reportResponses) {
        super(totalElements, totalPages);
        this.reportResponses = reportResponses;
    }

}
