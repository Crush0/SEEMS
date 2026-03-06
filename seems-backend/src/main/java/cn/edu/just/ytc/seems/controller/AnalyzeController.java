package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.pojo.entity.AnalyzeData;
import cn.edu.just.ytc.seems.service.AnalyzeService;
import cn.edu.just.ytc.seems.utils.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/analysis")
public class AnalyzeController {
    @Resource
    private AnalyzeService analyzeService;

    @PostMapping("/get")
    public R getAnalyzeData(@RequestBody(required = false) Map<String, String> params) {
        List<AnalyzeData> analyzeData;
        if (params != null && !params.isEmpty()) {
            String startDate = params.get("startDate");
            String endDate = params.get("endDate");
            analyzeData = analyzeService.getNewestAnalyzeData(Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
        } else {
            LocalDate today = LocalDate.now();
            Timestamp startOfDay = Timestamp.valueOf(today.minusDays(30).atStartOfDay());
            Timestamp endOfDay = Timestamp.valueOf(today.plusDays(1).atStartOfDay());
            analyzeData = analyzeService.getNewestAnalyzeData(startOfDay, endOfDay);
        }
        return R.ok(analyzeData);
    }

    @GetMapping("/immediate")
    public R immediateAnalyzeData() {
        analyzeService.analyzeImmediately();
        return R.ok();
    }
}
