package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.mapper.BatteryLogMapper;
import cn.edu.just.ytc.seems.pojo.entity.BatteryLog;
import cn.edu.just.ytc.seems.service.BatterySOCService;
// BaseService在Impl包中
import cn.edu.just.ytc.seems.service.Impl.BaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 电池SOC异常值检测与处理服务实现类
 */
@Service
public class BatterySOCServiceImpl extends BaseService implements BatterySOCService {

    @Resource
    private BatteryLogMapper batteryLogMapper;
    
    // SOC突变阈值，默认为30%，超过此阈值的变化被视为异常
    private double socChangeThreshold = 30.0;
    
    /**
     * 获取处理后的SOC数据（检测并修复异常值 + 平滑处理）
     */
    @Override
    public List<BatteryLog> getProcessedSOCData(BigInteger shipId, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 获取原始电池日志数据
        List<BatteryLog> batteryLogs = fetchBatteryLogsByTimeRange(shipId, startTime, endTime);
        
        if (batteryLogs == null || batteryLogs.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 检测异常值
        List<Integer> anomalyIndices = detectAnomalies(batteryLogs);
        
        // 3. 使用线性插值修复异常值
        List<BatteryLog> processedLogs;
        if (!anomalyIndices.isEmpty()) {
            processedLogs = fixAnomaliesWithLinearInterpolation(batteryLogs, anomalyIndices);
        } else {
            processedLogs = new ArrayList<>(batteryLogs);
        }
        
        // 4. 应用移动平均滤波来平滑SOC数据，减少小幅震荡
        List<BatteryLog> smoothedLogs = applyMovingAverageFilter(processedLogs, 3);
        
        return smoothedLogs;
    }
    
    /**
     * 应用移动平均滤波来平滑SOC数据
     * @param logs 电池日志列表
     * @param windowSize 窗口大小（建议3-5个数据点）
     * @return 平滑后的数据
     */
    private List<BatteryLog> applyMovingAverageFilter(List<BatteryLog> logs, int windowSize) {
        if (logs.size() <= windowSize) {
            // 如果数据点太少，直接返回原始数据
            return new ArrayList<>(logs);
        }
        
        List<BatteryLog> smoothedLogs = new ArrayList<>(logs.size());
        
        // 对每个数据点应用移动平均
        for (int i = 0; i < logs.size(); i++) {
            BatteryLog originalLog = logs.get(i);
            BatteryLog smoothedLog = cloneBatteryLog(originalLog);
            
            // 计算窗口范围
            int startIdx = Math.max(0, i - windowSize / 2);
            int endIdx = Math.min(logs.size() - 1, i + windowSize / 2);
            
            // 计算窗口内的平均值
            double sum = 0.0;
            int count = 0;
            for (int j = startIdx; j <= endIdx; j++) {
                if (logs.get(j).getSoc() != null) {
                    sum += logs.get(j).getSoc().doubleValue();
                    count++;
                }
            }
            
            // 设置平滑后的SOC值
            if (count > 0) {
                double averageSoc = sum / count;
                smoothedLog.setSoc(BigDecimal.valueOf(averageSoc).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            
            smoothedLogs.add(smoothedLog);
        }
        
        return smoothedLogs;
    }
    
    /**
     * 检测异常值
     */
    @Override
    public List<Integer> detectAnomalies(List<BatteryLog> batteryLogs) {
        List<Integer> anomalyIndices = new ArrayList<>();
        
        for (int i = 0; i < batteryLogs.size(); i++) {
            BatteryLog current = batteryLogs.get(i);
            boolean isAnomaly = false;
            
            // 检查当前SOC是否为null
            if (current.getSoc() == null) {
                isAnomaly = true;
            } else if (current.getSoc().compareTo(BigDecimal.ZERO) == 0) {
                // 检查当前SOC是否为0（特殊情况）
                isAnomaly = true;
            } else if (i > 0 && batteryLogs.get(i-1).getSoc() != null && i < batteryLogs.size() - 1 && batteryLogs.get(i+1).getSoc() != null) {
                // 使用前后两个点来判断是否异常（三点判断法）
                double prevSoc = batteryLogs.get(i-1).getSoc().doubleValue();
                double currentSoc = current.getSoc().doubleValue();
                double nextSoc = batteryLogs.get(i+1).getSoc().doubleValue();
                
                // 计算与前一个值的变化百分比
                double changeFromPrev = Math.abs((currentSoc - prevSoc) / Math.max(prevSoc, 0.01) * 100);
                // 计算与后一个值的变化百分比
                double changeToNext = Math.abs((nextSoc - currentSoc) / Math.max(currentSoc, 0.01) * 100);
                
                // 如果当前点与前后两点的变化都很大，则更可能是异常值
                if (changeFromPrev > socChangeThreshold && changeToNext > socChangeThreshold) {
                    isAnomaly = true;
                }
            } else if (i > 0 && batteryLogs.get(i-1).getSoc() != null) {
                // 只有前一个点的情况
                double prevSoc = batteryLogs.get(i-1).getSoc().doubleValue();
                double currentSoc = current.getSoc().doubleValue();
                double changePercent = Math.abs((currentSoc - prevSoc) / Math.max(prevSoc, 0.01) * 100);
                
                if (changePercent > socChangeThreshold) {
                    isAnomaly = true;
                }
            }
            
            if (isAnomaly) {
                anomalyIndices.add(i);
            }
        }
        
        return anomalyIndices;
    }
    
    @Override
    public List<BatteryLog> fixAnomaliesWithLinearInterpolation(List<BatteryLog> batteryLogs, List<Integer> anomalyIndices) {
        // 创建副本以避免修改原始数据
        List<BatteryLog> fixedLogs = batteryLogs.stream()
                .map(this::cloneBatteryLog)
                .collect(Collectors.toList());
        
        // 处理异常值索引列表，按照索引从大到小排序，避免修复顺序影响结果
        List<Integer> sortedIndices = new ArrayList<>(anomalyIndices);
        sortedIndices.sort((a, b) -> b - a);
        
        for (Integer index : sortedIndices) {
            // 找到异常值前后的有效数据点
            int prevValidIndex = findPreviousValidIndex(fixedLogs, index);
            int nextValidIndex = findNextValidIndex(fixedLogs, index);
            
            // 如果能找到前后有效数据点，则使用线性插值修复
            if (prevValidIndex != -1 && nextValidIndex != -1) {
                BatteryLog prevLog = fixedLogs.get(prevValidIndex);
                BatteryLog nextLog = fixedLogs.get(nextValidIndex);
                BatteryLog currentLog = fixedLogs.get(index);
                
                // 计算时间差比例
                long prevTime = prevLog.getTime().getTime();
                long nextTime = nextLog.getTime().getTime();
                long currentTime = currentLog.getTime().getTime();
                long totalTimeDiff = nextTime - prevTime;
                
                if (totalTimeDiff > 0) {
                    double timeRatio = (double) (currentTime - prevTime) / totalTimeDiff;
                    
                    // 线性插值计算SOC值
                    BigDecimal prevSoc = prevLog.getSoc();
                    BigDecimal nextSoc = nextLog.getSoc();
                    BigDecimal socDiff = nextSoc.subtract(prevSoc);
                    BigDecimal interpolatedSoc = prevSoc.add(BigDecimal.valueOf(timeRatio).multiply(socDiff));
                    
                    // 确保SOC值在合理范围内(0-100)
                    if (interpolatedSoc.compareTo(BigDecimal.ZERO) < 0) {
                        interpolatedSoc = BigDecimal.ZERO;
                    } else if (interpolatedSoc.compareTo(BigDecimal.valueOf(100)) > 0) {
                        interpolatedSoc = BigDecimal.valueOf(100);
                    }
                    
                    currentLog.setSoc(interpolatedSoc);
                    // 可以在这里设置一个标记，表示该值已被修复
                }
            }
        }
        
        return fixedLogs;
    }
    
    @Override
    public void setSOCChangeThreshold(double threshold) {
        this.socChangeThreshold = threshold;
    }
    
    /**
     * 查询指定时间范围内的电池日志数据
     */
    private List<BatteryLog> fetchBatteryLogsByTimeRange(BigInteger shipId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<BatteryLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BatteryLog::getShipId, shipId);
        queryWrapper.between(BatteryLog::getTime, 
                Timestamp.valueOf(startTime), 
                Timestamp.valueOf(endTime));
        queryWrapper.orderByAsc(BatteryLog::getTime);
        
        return batteryLogMapper.selectList(queryWrapper);
    }
    
    /**
     * 查找前一个有效数据点的索引
     */
    private int findPreviousValidIndex(List<BatteryLog> logs, int currentIndex) {
        for (int i = currentIndex - 1; i >= 0; i--) {
            if (logs.get(i).getSoc() != null && 
                logs.get(i).getSoc().compareTo(BigDecimal.ZERO) >= 0 && 
                logs.get(i).getSoc().compareTo(BigDecimal.valueOf(100)) <= 0) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 查找后一个有效数据点的索引
     */
    private int findNextValidIndex(List<BatteryLog> logs, int currentIndex) {
        for (int i = currentIndex + 1; i < logs.size(); i++) {
            if (logs.get(i).getSoc() != null && 
                logs.get(i).getSoc().compareTo(BigDecimal.ZERO) >= 0 && 
                logs.get(i).getSoc().compareTo(BigDecimal.valueOf(100)) <= 0) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 克隆BatteryLog对象
     */
    private BatteryLog cloneBatteryLog(BatteryLog log) {
        BatteryLog cloned = new BatteryLog();
        cloned.setId(log.getId());
        cloned.setShipId(log.getShipId());
        cloned.setTime(log.getTime());
        cloned.setVoyageId(log.getVoyageId());
        cloned.setSoc(log.getSoc());
        cloned.setVoltage(log.getVoltage());
        cloned.setElectricity(log.getElectricity());
        cloned.setPower(log.getPower());
        cloned.setTemperature(log.getTemperature());
        cloned.setPosition(log.getPosition());
        // Lombok会为isAlarm生成setAlarm方法
        cloned.setAlarm(log.isAlarm());
        cloned.setLinkId(log.getLinkId());
        // BaseEntity中的字段是createDate和updateDate，不是createTime和updateTime
        try {
            // 尝试设置基础字段
            if (log.getCreateDate() != null) {
                cloned.setCreateDate(log.getCreateDate());
            }
            if (log.getUpdateDate() != null) {
                cloned.setUpdateDate(log.getUpdateDate());
            }
        } catch (Exception e) {
            // 忽略可能不存在的方法
        }
        return cloned;
    }
}