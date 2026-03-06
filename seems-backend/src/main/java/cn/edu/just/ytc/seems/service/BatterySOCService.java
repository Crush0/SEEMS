package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.entity.BatteryLog;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 电池SOC异常值检测与处理服务接口
 */
public interface BatterySOCService extends IBaseUserInfo {
    
    /**
     * 获取指定时间范围内的电池SOC数据，并自动检测和修复异常值
     * @param shipId 船舶ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 修复后的电池SOC数据列表
     */
    List<BatteryLog> getProcessedSOCData(BigInteger shipId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 检测电池SOC数据中的异常值
     * @param batteryLogs 电池日志数据列表
     * @return 异常值索引列表
     */
    List<Integer> detectAnomalies(List<BatteryLog> batteryLogs);
    
    /**
     * 使用线性插值法修复异常值
     * @param batteryLogs 电池日志数据列表
     * @param anomalyIndices 异常值索引列表
     * @return 修复后的电池日志数据列表
     */
    List<BatteryLog> fixAnomaliesWithLinearInterpolation(List<BatteryLog> batteryLogs, List<Integer> anomalyIndices);
    
    /**
     * 设置SOC突变阈值（百分比）
     * @param threshold 阈值，默认为30%
     */
    void setSOCChangeThreshold(double threshold);
}