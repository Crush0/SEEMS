package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.pojo.dto.*;
import cn.edu.just.ytc.seems.pojo.entity.AlarmStrategy;
import cn.edu.just.ytc.seems.service.AlarmStrategyService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.edu.just.ytc.seems.utils.R;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 报警策略管理控制器
 */

@Tag(name = "报警策略管理", description = "报警策略配置相关接口")
@Slf4j
@RestController
@RequestMapping("/alarm-strategy")
public class AlarmStrategyController {

    @Resource
    private AlarmStrategyService alarmStrategyService;

    @PostMapping("/create")
    @Operation(summary = "创建报警策略")
    public R createStrategy(@Valid @RequestBody CreateAlarmStrategyRequest request) {
        Long strategyId = alarmStrategyService.createStrategy(request);
        return R.ok(strategyId);
    }

    @PostMapping("/update")
    @Operation(summary = "更新报警策略")
    public R updateStrategy(@Valid @RequestBody UpdateAlarmStrategyRequest request) {
        Boolean result = alarmStrategyService.updateStrategy(request);
        return R.ok(result);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除报警策略")
    public R deleteStrategy(@RequestParam Long strategyId) {
        Boolean result = alarmStrategyService.deleteStrategy(strategyId);
        return R.ok(result);
    }

    @PostMapping("/list")
    @Operation(summary = "查询报警策略列表")
    public R queryStrategyList(@RequestBody QueryAlarmStrategyRequest request) {
        Page<AlarmStrategyVO> page = alarmStrategyService.queryStrategyList(request);
        return R.ok(page);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取报警策略详情")
    public R getStrategy(@RequestParam Long strategyId) {
        AlarmStrategyVO vo = alarmStrategyService.getStrategy(strategyId);
        return R.ok(vo);
    }

    @PostMapping("/enable")
    @Operation(summary = "启用/禁用报警策略")
    public R enableStrategy(
            @RequestParam Long strategyId,
            @RequestParam Boolean enabled) {
        Boolean result = alarmStrategyService.enableStrategy(strategyId, enabled);
        return R.ok(result);
    }

    @PostMapping("/apply")
    @Operation(summary = "应用报警策略（Python脚本调用）")
    public R applyStrategy(@Valid @RequestBody ApplyAlarmStrategyRequest request) {
        Boolean result = alarmStrategyService.applyStrategy(request);
        return R.ok(result);
    }

    @GetMapping("/effective")
    @Operation(summary = "获取有效的报警策略")
    public R getEffectiveStrategy(
            @RequestParam Long shipId,
            @RequestParam String alarmType) {
        AlarmStrategy strategy = alarmStrategyService.getEffectiveStrategy(shipId, alarmType);
        if (strategy == null) {
            return R.ok(null);
        }

        // 转换为VO
        AlarmStrategyVO vo = alarmStrategyService.getStrategy(strategy.getId().longValue());
        return R.ok(vo);
    }
}
