package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.annotation.HasPermission;
import cn.edu.just.ytc.seems.pojo.dto.NoonReportParams;
import cn.edu.just.ytc.seems.pojo.dto.ReportParams;
import cn.edu.just.ytc.seems.pojo.dto.ReportResponse;
import cn.edu.just.ytc.seems.pojo.entity.NoonReport;
import cn.edu.just.ytc.seems.pojo.entity.ReportData;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.ReportService;
import cn.edu.just.ytc.seems.utils.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
public class ReportController extends BaseController{
    @Resource
    private ReportService reportService;


    @HasPermission(role= UserShipRole.Role.OPERATOR)
    @GetMapping("/generate")
    public R generate(){
        return R.ok(reportService.generateNoonReport());
    }

    @HasPermission(role = UserShipRole.Role.OPERATOR)
    @PostMapping("/update-or-insert")
    public R updateOrInsert(@RequestBody NoonReport noonReport){
        reportService.updateOrInsert(noonReport);
        return R.ok();
    }

    @HasPermission
    @PostMapping("/query")
    public R query(@RequestBody NoonReportParams noonReportParams){
        return R.ok(reportService.queryNoonReport(noonReportParams));
    }

    @HasPermission
    @PostMapping("/query-history-report")
    public R queryHistoryReport(@RequestBody ReportParams reportParams){
        ReportData[] analysisData = reportService.queryReportData(reportParams, 5);
        return R.ok(new ReportResponse(reportParams, analysisData));
    }

}
