package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.annotation.HasPermission;
import cn.edu.just.ytc.seems.pojo.dto.*;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.AlarmLogService;
import cn.edu.just.ytc.seems.utils.R;
import cn.edu.just.ytc.seems.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 报警日志控制器
 */
@Tag(name = "报警日志管理")
@RestController
@RequestMapping("/api/alarm")
public class AlarmLogController extends BaseController {

    @Resource
    private AlarmLogService alarmLogService;

    /**
     * 创建报警日志
     */
    @Operation(summary = "创建报警日志")
    @PostMapping("/create")
    @HasPermission(role = UserShipRole.Role.ADMIN)
    public R createAlarm(@RequestBody CreateAlarmLogRequest request) {
        alarmLogService.createAlarm(request);
        return R.ok();
    }

    /**
     * 查询报警日志列表
     */
    @Operation(summary = "查询报警日志列表")
    @PostMapping("/list")
    public R queryAlarmList(@RequestBody QueryAlarmLogRequest request) {
        return R.ok(alarmLogService.queryAlarmList(request));
    }

    /**
     * 标记报警为已处理
     */
    @Operation(summary = "标记报警为已处理")
    @PostMapping("/handle")
    public R markAsHandled(@RequestParam Long alarmId) {
        alarmLogService.markAsHandled(alarmId);
        return R.ok();
    }

    /**
     * 获取未处理报警数量
     */
    @Operation(summary = "获取未处理报警数量")
    @GetMapping("/unhandled-count")
    public R getUnhandledCount(@RequestParam Long shipId) {
        Integer count = alarmLogService.getUnhandledCount(shipId);
        return R.ok(count);
    }

    /**
     * 创建SOC低电量报警
     */
    @Operation(summary = "创建SOC低电量报警")
    @PostMapping("/low-soc")
    public R createLowSocAlarm(
            @RequestParam Long shipId,
            @RequestParam Double socValue,
            @RequestParam(required = false) Long relatedDataId
    ) {
        alarmLogService.createLowSocAlarm(shipId, socValue, relatedDataId);
        return R.ok();
    }
}
