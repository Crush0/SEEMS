package cn.edu.just.ytc.seems.analyze;

import cn.edu.just.ytc.seems.mapper.BatteryLogMapper;
import cn.edu.just.ytc.seems.pojo.entity.BatteryLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class EnergyConsumptionCalc {
    public static final Double CarbonEquivalentCoefficient = 0.6829; /* 2018江苏省碳当量系数 (tCO2/MWh) */

    public static List<List<BatteryLog>> fetchBatteryLogsForToday(BatteryLogMapper batteryLogMapper, BigInteger shipId) {
        LocalDate today = LocalDate.now();
        Timestamp startOfDay = Timestamp.valueOf(today.atStartOfDay());
        Timestamp endOfDay = Timestamp.valueOf(today.plusDays(1).atStartOfDay());

        // 构建查询条件
        LambdaQueryWrapper<BatteryLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(BatteryLog::getTime, startOfDay, endOfDay);
        queryWrapper.eq(BatteryLog::getShipId, shipId);
        queryWrapper.orderByAsc(BatteryLog::getTime);
        List<BatteryLog> logs = batteryLogMapper.selectList(queryWrapper);

        // 将结果转化为按位置分组的列表
        Map<String, List<BatteryLog>> positionMap = new HashMap<>();
        for (BatteryLog log : logs) {
            positionMap.computeIfAbsent(log.getPosition(), k -> new ArrayList<>()).add(log);
        }

        // 转换为List<List<BatteryLog>>类型
        return new ArrayList<>(positionMap.values());
    }

    /**
     * 计算CII分数
     *
     * @param carbonEmission 碳排放量（单位：吨）
     * @param dwt            死重吨位（单位：吨）
     * @param distanceKm     航程（单位：千米）
     * @return CII 分数
     */
    public static BigDecimal calculateCII(BigDecimal carbonEmission, BigDecimal dwt, Double distanceKm) {
        // 验证输入参数是否有效
        if (carbonEmission == null || dwt == null || distanceKm == null || dwt.compareTo(BigDecimal.ZERO) == 0 || distanceKm <= 0) {
            return BigDecimal.ZERO;
        }

        // 将航程从公里转换为海里（1海里约等于1.852公里）
        BigDecimal distanceNm = BigDecimal.valueOf(distanceKm).divide(BigDecimal.valueOf(1.852), 10, RoundingMode.HALF_UP);

        // CII 公式: 碳排放量（吨） / (DWT * 航程（海里）)
        BigDecimal denominator = dwt.multiply(distanceNm);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // 无效输入参数
        }

        return carbonEmission.divide(denominator, 10, RoundingMode.HALF_UP);
    }


    public static List<List<BatteryLog>> fetchBatteryLogsBetween(BatteryLogMapper batteryLogMapper, Date start, Date end, BigInteger shipId) {
        // 将 Date 转换为Timestamp
        Timestamp startTimestamp = Timestamp.valueOf(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        Timestamp endTimestamp = Timestamp.valueOf(end.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        // 构建查询条件
        LambdaQueryWrapper<BatteryLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(BatteryLog::getTime, startTimestamp, endTimestamp);
        queryWrapper.eq(BatteryLog::getShipId, shipId);
        queryWrapper.orderByAsc(BatteryLog::getTime);
        List<BatteryLog> logs = batteryLogMapper.selectList(queryWrapper);

        // 将结果转化为按位置分组的列表
        Map<String, List<BatteryLog>> positionMap = new HashMap<>();
        for (BatteryLog log : logs) {
            positionMap.computeIfAbsent(log.getPosition(), k -> new ArrayList<>()).add(log);
        }

        // 转换为List<List<BatteryLog>>类型
        return new ArrayList<>(positionMap.values());
    }

    public static List<List<BatteryLog>> fetchBatteryLogsByVoyage(BatteryLogMapper batteryLogMapper, BigInteger shipId, BigInteger voyageId) {
        LambdaQueryWrapper<BatteryLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BatteryLog::getVoyageId, voyageId);
        queryWrapper.eq(BatteryLog::getShipId, shipId);
        queryWrapper.orderByAsc(BatteryLog::getTime);
        List<BatteryLog> logs = batteryLogMapper.selectList(queryWrapper);

        // 将结果转化为按位置分组的列表
        Map<String, List<BatteryLog>> positionMap = new HashMap<>();
        for (BatteryLog log : logs) {
            positionMap.computeIfAbsent(log.getPosition(), k -> new ArrayList<>()).add(log);
        }

        // 转换为List<List<BatteryLog>>类型
        return new ArrayList<>(positionMap.values());
    }

    public static List<List<BatteryLog>> fetchBatteryLogsForHourly(BatteryLogMapper batteryLogMapper, BigInteger shipId) {
        LocalDateTime now = LocalDateTime.now();
        // 计算上一个小时的开始和结束时间
        // start：当前小时的倾Ca时间（去年次）
        LocalDateTime hourStart = now.minusHours(1).withMinute(0).withSecond(0).withNano(0);
        // end：当前 ацә -分钟数
        LocalDateTime hourEnd = now.minusMinutes(now.getMinute()).withSecond(0).withNano(0);

        // 将 LocalDateTime 转换为Timestamp
        Timestamp startOfHour = Timestamp.valueOf(hourStart);
        Timestamp endOfHour = Timestamp.valueOf(hourEnd);

        // 构建查询条件
        LambdaQueryWrapper<BatteryLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(BatteryLog::getTime, startOfHour, endOfHour);
        queryWrapper.eq(BatteryLog::getShipId, shipId);
        queryWrapper.orderByAsc(BatteryLog::getTime);
        List<BatteryLog> logs = batteryLogMapper.selectList(queryWrapper);

        // 将结果转化为按位置分组的列表
        Map<String, List<BatteryLog>> positionMap = new HashMap<>();
        for (BatteryLog log : logs) {
            positionMap.computeIfAbsent(log.getPosition(), k -> new ArrayList<>()).add(log);
        }

        // 转换为 List<List<BatteryLog>> 类型
        return new ArrayList<>(positionMap.values());

    }

    public static BigDecimal calculateTotalEnergyConsumption(List<List<BatteryLog>> batteryClusters, BigDecimal totalMaxCapacity) {
        if (batteryClusters == null || batteryClusters.isEmpty() || totalMaxCapacity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO; // 没有数据或总容量无效则返回0
        }

        BigDecimal totalEnergyConsumption = BigDecimal.ZERO;

        BigDecimal avgMaxCapacity = totalMaxCapacity.divide(BigDecimal.valueOf(batteryClusters.size()), RoundingMode.HALF_UP);

        // 遍历每个电池簇
        for (List<BatteryLog> cluster : batteryClusters) {
            // 确保每个簇至少有两个记录
            if (cluster.size() < 2) continue;

            // 遍历簇中的每个BatteryLog
            for (int i = 1; i < cluster.size(); i++) {
                BigDecimal previousSoc = cluster.get(i - 1).getSoc();
                BigDecimal currentSoc = cluster.get(i).getSoc();
                BigDecimal change = BigDecimal.ZERO;
                try {
                    if (currentSoc == null) {
                        currentSoc = previousSoc;
                    }
                    // 计算SOC的变化
                    change = currentSoc.subtract(previousSoc);
                } catch (Exception ignored) {

                }

                // 只计算放电时的电能消耗以及忽略充电
                if (change.compareTo(BigDecimal.ZERO) < 0) {
                    // 计算电能消耗（kWh）
                    BigDecimal energyConsumption = change.abs().multiply(avgMaxCapacity).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP); // 将每项百分比换算为kWh
                    totalEnergyConsumption = totalEnergyConsumption.add(energyConsumption);
                }
            }
        }

        return totalEnergyConsumption; // 返回总电能消耗 (kWh)
    }

    public static BigDecimal calculateTotalCarbonEmission(BigDecimal totalEnergyConsumption) {
        if (totalEnergyConsumption.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO; // 没有数据或总电能消耗无效则返回0
        }
        // kwh -> MWh
        BigDecimal totalEnergyConsumptionMwh = totalEnergyConsumption.divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP);
        return totalEnergyConsumptionMwh.multiply(BigDecimal.valueOf(CarbonEquivalentCoefficient)); // 返回总碳排放量 (tCO2)
    }

    /**
     * 计算单位运输功电能消耗量
     * 对于纯电动拖轮，运输功 = 拖力(t) × 航行距离(km)
     * 单位运输功电能消耗 = 电能消耗(kWh) / (拖力(t) × 航行距离(km))
     *
     * @param energyConsumption      电能消耗（单位：kWh）
     * @param towingForce            拖力（单位：t）
     * @param sailingDistanceKm      航行距离（单位：km）
     * @return 单位运输功电能消耗（单位：kWh/(t·km)）
     */
    public static BigDecimal calculateUnitWorkEnergyConsumption(
            BigDecimal energyConsumption,
            BigDecimal towingForce,
            BigDecimal sailingDistanceKm) {
        // 验证输入参数是否有效
        if (energyConsumption == null || energyConsumption.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (towingForce == null || towingForce.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (sailingDistanceKm == null || sailingDistanceKm.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 计算运输功：拖力(t) × 航行距离(km)
        BigDecimal transportWork = towingForce.multiply(sailingDistanceKm);

        // 计算单位运输功电能消耗：电能消耗(kWh) / 运输功(t·km)
        return energyConsumption.divide(transportWork, 10, RoundingMode.HALF_UP);
    }

    /**
     * 计算单位航行距离电能消耗量
     * 单位距离电能消耗 = 电能消耗(kWh) / 航行距离(km)
     *
     * @param energyConsumption      电能消耗（单位：kWh）
     * @param sailingDistanceKm      航行距离（单位：km）
     * @return 单位距离电能消耗（单位：kWh/km）
     */
    public static BigDecimal calculateDistanceEnergyConsumption(
            BigDecimal energyConsumption,
            BigDecimal sailingDistanceKm) {
        // 验证输入参数是否有效
        if (energyConsumption == null || energyConsumption.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (sailingDistanceKm == null || sailingDistanceKm.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 计算单位距离电能消耗：电能消耗(kWh) / 航行距离(km)
        return energyConsumption.divide(sailingDistanceKm, 10, RoundingMode.HALF_UP);
    }
}
