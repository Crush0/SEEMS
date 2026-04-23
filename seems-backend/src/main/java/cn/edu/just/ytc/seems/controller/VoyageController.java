package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.pojo.entity.VoyageLog;
import cn.edu.just.ytc.seems.service.VoyageService;
import cn.edu.just.ytc.seems.utils.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/voyage")
@Tag(name = "航次管理", description = "航次数据管理接口")
public class VoyageController extends BaseController {

    @Resource
    private VoyageService voyageService;

    /**
     * 获取航次列表
     */
    @Operation(summary = "获取航次列表")
    @GetMapping("/list")
    public R getVoyageList(
            @RequestParam(required = false) Integer current,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        UserShipRole userShipRole = getUserShipRole();
        BigInteger shipId = userShipRole.getShipId();

        // 构建分页参数
        Page<VoyageLog> page = new Page<>(
                current != null ? current : 1,
                pageSize != null ? pageSize : 10
        );

        // 构建查询条件
        LambdaQueryWrapper<VoyageLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VoyageLog::getShipId, shipId);

        // 时间范围过滤
        if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.ge(VoyageLog::getStartTime, Timestamp.valueOf(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.le(VoyageLog::getEndTime, Timestamp.valueOf(endTime));
        }

        queryWrapper.orderByDesc(VoyageLog::getStartTime);

        IPage<VoyageLog> result = voyageService.page(page, queryWrapper);
        return R.ok(result);
    }

    /**
     * 获取航次详情
     */
    @Operation(summary = "获取航次详情")
    @GetMapping("/{id}")
    public R getVoyageDetail(@PathVariable BigInteger id) {
        VoyageLog voyageLog = voyageService.getById(id);
        if (voyageLog == null) {
            return R.error("航次不存在");
        }
        return R.ok(voyageLog);
    }

    /**
     * 获取航次统计数据
     */
    @Operation(summary = "获取航次统计数据")
    @GetMapping("/statistics")
    public R getVoyageStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        UserShipRole userShipRole = getUserShipRole();
        BigInteger shipId = userShipRole.getShipId();

        // 构建查询条件
        LambdaQueryWrapper<VoyageLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VoyageLog::getShipId, shipId);

        // 时间范围过滤
        if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.ge(VoyageLog::getStartTime, Timestamp.valueOf(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.le(VoyageLog::getEndTime, Timestamp.valueOf(endTime));
        }

        List<VoyageLog> voyageList = voyageService.list(queryWrapper);

        // 统计数据
        VoyageStatistics statistics = new VoyageStatistics();
        statistics.setTotalVoyages(voyageList.size());

        double totalDistance = 0;
        double totalPowerConsumption = 0;
        long totalTime = 0;

        for (VoyageLog voyage : voyageList) {
            if (voyage.getVoyageDistance() != null) {
                totalDistance += voyage.getVoyageDistance();
            }
            if (voyage.getVoyagePowerConsumption() != null) {
                totalPowerConsumption += voyage.getVoyagePowerConsumption();
            }
            if (voyage.getSailingTime() != null) {
                totalTime += voyage.getSailingTime();
            }
        }

        statistics.setTotalDistance(totalDistance);
        statistics.setTotalPowerConsumption(totalPowerConsumption);
        statistics.setTotalTime(totalTime);

        // 计算平均值
        if (voyageList.size() > 0) {
            statistics.setAverageDistance(totalDistance / voyageList.size());
            statistics.setAveragePowerConsumption(totalPowerConsumption / voyageList.size());
            statistics.setAverageTime(totalTime / voyageList.size());
        }

        return R.ok(statistics);
    }

    /**
     * 航次统计数据内部类
     */
    public static class VoyageStatistics {
        private Integer totalVoyages;
        private Double totalDistance;
        private Double totalPowerConsumption;
        private Long totalTime;
        private Double averageDistance;
        private Double averagePowerConsumption;
        private Long averageTime;

        // Getters and Setters
        public Integer getTotalVoyages() {
            return totalVoyages;
        }

        public void setTotalVoyages(Integer totalVoyages) {
            this.totalVoyages = totalVoyages;
        }

        public Double getTotalDistance() {
            return totalDistance;
        }

        public void setTotalDistance(Double totalDistance) {
            this.totalDistance = totalDistance;
        }

        public Double getTotalPowerConsumption() {
            return totalPowerConsumption;
        }

        public void setTotalPowerConsumption(Double totalPowerConsumption) {
            this.totalPowerConsumption = totalPowerConsumption;
        }

        public Long getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(Long totalTime) {
            this.totalTime = totalTime;
        }

        public Double getAverageDistance() {
            return averageDistance;
        }

        public void setAverageDistance(Double averageDistance) {
            this.averageDistance = averageDistance;
        }

        public Double getAveragePowerConsumption() {
            return averagePowerConsumption;
        }

        public void setAveragePowerConsumption(Double averagePowerConsumption) {
            this.averagePowerConsumption = averagePowerConsumption;
        }

        public Long getAverageTime() {
            return averageTime;
        }

        public void setAverageTime(Long averageTime) {
            this.averageTime = averageTime;
        }
    }
}