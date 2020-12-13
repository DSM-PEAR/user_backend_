package com.dsmpear.main.controller;

import com.dsmpear.main.payload.request.ReportRequest;
import com.dsmpear.main.payload.response.ReportContentResponse;
import com.dsmpear.main.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void writeReport(@RequestBody @Valid ReportRequest reportRequest) {
        reportService.writeReport(reportRequest);
    }

    @GetMapping("/{reportId}")
    public ReportContentResponse getReportContent(@PathVariable Integer reportId) {
        return reportService.viewReport(reportId);
    }

    @PatchMapping("/{reportId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Integer updateReport(@PathVariable Integer reportId, @RequestBody ReportRequest reportRequest) {
        System.out.println("asdfasd");
        return reportService.updateReport(reportId, reportRequest);
    }

    @DeleteMapping("{reportId}")
    public void deleteReport(@PathVariable Integer reportId) {
        reportService.deleteReport(reportId);
    }

}