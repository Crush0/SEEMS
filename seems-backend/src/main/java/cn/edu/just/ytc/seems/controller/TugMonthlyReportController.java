package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.annotation.HasPermission;
import cn.edu.just.ytc.seems.pojo.dto.TugMonthlyReportDTO;
import cn.edu.just.ytc.seems.pojo.dto.TugMonthlyReportQuery;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.TugMonthlyReportService;
import cn.edu.just.ytc.seems.utils.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 拖轮月度报表控制器
 */
@RestController
@RequestMapping("/tug-monthly-report")
public class TugMonthlyReportController extends BaseController {

    @Resource
    private TugMonthlyReportService tugMonthlyReportService;

    /**
     * 生成月度报表
     */
    @HasPermission
    @PostMapping("/generate")
    public R generateMonthlyReport(@RequestBody TugMonthlyReportQuery query) {
        TugMonthlyReportDTO report = tugMonthlyReportService.generateMonthlyReport(query);
        return R.ok(report);
    }
}
