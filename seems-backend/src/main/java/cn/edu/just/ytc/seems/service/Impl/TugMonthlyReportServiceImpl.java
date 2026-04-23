package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.mapper.GPSLogMapper;
import cn.edu.just.ytc.seems.mapper.ShipMapper;
import cn.edu.just.ytc.seems.mapper.VoyageLogMapper;
import cn.edu.just.ytc.seems.pojo.dto.TugMonthlyReportDTO;
import cn.edu.just.ytc.seems.pojo.dto.TugMonthlyReportQuery;
import cn.edu.just.ytc.seems.pojo.entity.GPSLog;
import cn.edu.just.ytc.seems.pojo.entity.ShipBaseInfo;
import cn.edu.just.ytc.seems.pojo.entity.VoyageLog;
import cn.edu.just.ytc.seems.service.TugMonthlyReportService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 拖轮月度报表服务实现
 */
@Service
public class TugMonthlyReportServiceImpl implements TugMonthlyReportService {

    @Resource
    private VoyageLogMapper voyageLogMapper;

    @Resource
    private GPSLogMapper gpsLogMapper;

    @Resource
    private ShipMapper shipMapper;

    @Override
    public TugMonthlyReportDTO generateMonthlyReport(TugMonthlyReportQuery query) {
        // 获取船舶信息
        ShipBaseInfo shipBaseInfo = shipMapper.selectById(query.getShipId());

        // 获取月份的开始和结束时间
        YearMonth yearMonth = YearMonth.of(query.getYear(), query.getMonth());
        LocalDateTime startTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 查询航次数据
        LambdaQueryWrapper<VoyageLog> voyageWrapper = new LambdaQueryWrapper<>();
        voyageWrapper.eq(VoyageLog::getShipId, query.getShipId())
                .ge(VoyageLog::getStartTime, startTime)
                .le(VoyageLog::getEndTime, endTime);
        List<VoyageLog> voyageLogs = voyageLogMapper.selectList(voyageWrapper);

        // 查询GPS数据用于计算每日能耗
        LambdaQueryWrapper<GPSLog> gpsWrapper = new LambdaQueryWrapper<>();
        gpsWrapper.eq(GPSLog::getShipId, query.getShipId())
                .ge(GPSLog::getTime, startTime)
                .le(GPSLog::getTime, endTime)
                .orderByAsc(GPSLog::getTime);
        List<GPSLog> gpsLogs = gpsLogMapper.selectList(gpsWrapper);

        // 计算统计数据
        int totalOperations = voyageLogs.size();
        double totalOperationHours = voyageLogs.stream()
                .mapToDouble(v -> v.getSailingTime() != null ? v.getSailingTime() / 3600.0 : 0.0)
                .sum();
        double averageOperationDuration = totalOperations > 0 ? totalOperationHours / totalOperations : 0.0;
        double totalVoyageDistance = voyageLogs.stream()
                .mapToDouble(v -> v.getVoyageDistance() != null ? v.getVoyageDistance() : 0.0)
                .sum();
        double totalPowerConsumption = voyageLogs.stream()
                .mapToDouble(v -> v.getVoyagePowerConsumption() != null ? v.getVoyagePowerConsumption() : 0.0)
                .sum();
        double averagePowerConsumptionPerOperation = totalOperations > 0 ? totalPowerConsumption / totalOperations : 0.0;
        double powerConsumptionPerNauticalMile = totalVoyageDistance > 0 ? totalPowerConsumption / totalVoyageDistance : 0.0;

        // 计算每日能耗
        List<TugMonthlyReportDTO.DailyEnergyConsumption> dailyEnergyConsumptions =
                calculateDailyEnergyConsumption(gpsLogs, voyageLogs, yearMonth);

        return TugMonthlyReportDTO.builder()
                .shipId(query.getShipId())
                .shipName(shipBaseInfo != null ? shipBaseInfo.getName() : "")
                .year(query.getYear())
                .month(query.getMonth())
                .totalOperations(totalOperations)
                .totalOperationHours(Math.round(totalOperationHours * 100.0) / 100.0)
                .averageOperationDuration(Math.round(averageOperationDuration * 100.0) / 100.0)
                .totalVoyageDistance(Math.round(totalVoyageDistance * 100.0) / 100.0)
                .totalPowerConsumption(Math.round(totalPowerConsumption * 100.0) / 100.0)
                .averagePowerConsumptionPerOperation(Math.round(averagePowerConsumptionPerOperation * 100.0) / 100.0)
                .powerConsumptionPerNauticalMile(Math.round(powerConsumptionPerNauticalMile * 100.0) / 100.0)
                .dailyEnergyConsumptions(dailyEnergyConsumptions)
                .build();
    }

    /**
     * 计算每日能耗
     */
    private List<TugMonthlyReportDTO.DailyEnergyConsumption> calculateDailyEnergyConsumption(
            List<GPSLog> gpsLogs, List<VoyageLog> voyageLogs, YearMonth yearMonth) {

        // 按日期分组航次数据
        Map<String, List<VoyageLog>> voyageByDate = voyageLogs.stream()
                .collect(Collectors.groupingBy(v -> {
                    if (v.getStartTime() != null) {
                        return new java.sql.Timestamp(v.getStartTime().getTime())
                                .toLocalDateTime()
                                .toLocalDate()
                                .toString();
                    }
                    return "";
                }));

        // 按日期分组GPS数据，计算能耗
        Map<String, List<GPSLog>> gpsByDate = gpsLogs.stream()
                .collect(Collectors.groupingBy(g -> {
                    if (g.getTime() != null) {
                        return new java.sql.Timestamp(g.getTime().getTime())
                                .toLocalDateTime()
                                .toLocalDate()
                                .toString();
                    }
                    return "";
                }));

        // 生成该月的所有日期
        List<TugMonthlyReportDTO.DailyEnergyConsumption> dailyData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            String dateStr = date.format(formatter);

            List<VoyageLog> dayVoyages = voyageByDate.getOrDefault(dateStr, new ArrayList<>());
            List<GPSLog> dayGpsLogs = gpsByDate.getOrDefault(dateStr, new ArrayList<>());

            // 计算当日能耗
            double dayEnergyConsumption = dayVoyages.stream()
                    .mapToDouble(v -> v.getVoyagePowerConsumption() != null ? v.getVoyagePowerConsumption() : 0.0)
                    .sum();

            // 计算当日作业艘次
            int dayOperations = dayVoyages.size();

            // 计算当日航行里程
            double dayVoyageDistance = dayVoyages.stream()
                    .mapToDouble(v -> v.getVoyageDistance() != null ? v.getVoyageDistance() : 0.0)
                    .sum();

            dailyData.add(TugMonthlyReportDTO.DailyEnergyConsumption.builder()
                    .date(dateStr)
                    .energyConsumption(Math.round(dayEnergyConsumption * 100.0) / 100.0)
                    .operations(dayOperations)
                    .voyageDistance(Math.round(dayVoyageDistance * 100.0) / 100.0)
                    .build());
        }

        return dailyData;
    }
}
