package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.dto.RealTimeData;
import cn.edu.just.ytc.seems.pojo.entity.BatteryLog;
import cn.edu.just.ytc.seems.pojo.entity.GPSLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ShipLogService extends IBaseUserInfo{
    List<GPSLog> getNearlyShipGPSLog();
    List<GPSLog> getShipNavByDateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 计算船舶每个工况的持续时间
    Map<String, Long> getShipWorkDuration(LocalDateTime startTime, LocalDateTime endTime);

    LocalDateTime getNearlyShipLogDate();
    RealTimeData getRealTimeData();

    List<BatteryLog> getShipSocByDateTimeBetween(LocalDateTime parse, LocalDateTime parse1);
}
