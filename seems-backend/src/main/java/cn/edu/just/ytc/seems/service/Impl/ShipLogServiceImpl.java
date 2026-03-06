package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.BatteryLogMapper;
import cn.edu.just.ytc.seems.mapper.GPSLogMapper;
import cn.edu.just.ytc.seems.mapper.PropellerMapper;
import cn.edu.just.ytc.seems.mapper.WindLogMapper;
import cn.edu.just.ytc.seems.pojo.dto.RealTimeData;
import cn.edu.just.ytc.seems.pojo.dto.RealtimeRedisMsg;
import cn.edu.just.ytc.seems.pojo.entity.*;
import cn.edu.just.ytc.seems.pojo.vo.Position;
import cn.edu.just.ytc.seems.service.AnalyzeService;
import cn.edu.just.ytc.seems.service.BatterySOCService;
import cn.edu.just.ytc.seems.service.ShipLogService;
import cn.edu.just.ytc.seems.utils.RedisUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ShipLogServiceImpl extends BaseService implements ShipLogService {
    @Resource
    private GPSLogMapper shipGPSLogMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private GPSLogMapper gpsLogMapper;

    @Resource
    private BatteryLogMapper batteryLogMapper;

    @Resource
    private PropellerMapper propellerMapper;

    @Resource
    private WindLogMapper windLogMapper;

    @Resource
    private AnalyzeService analyzeService;


    @Override
    public List<GPSLog> getNearlyShipGPSLog() {
        UserShipRole userShipRole = getUserShipRole();
//        String key = "ship_gps_log_" + userShipRole.getShipId();
//        List<GPSLog> shipGPSLogList = redisUtils.getList(key);
//        if (shipGPSLogList.isEmpty()) {
//            shipGPSLogList = shipGPSLogMapper.getNearlyShipGPSLogByShipId(userShipRole.getShipId());
//            redisUtils.setList(key, shipGPSLogList, 10, TimeUnit.MINUTES);
//        }
        return shipGPSLogMapper.getNearlyShipGPSLogByShipId(userShipRole.getShipId());
    }

    @Override
    public List<GPSLog> getShipNavByDateTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        UserShipRole userShipRole = getUserShipRole();
        return shipGPSLogMapper.getShipGPSLogByShipIdAndDateTimeBetween(userShipRole.getShipId(), Timestamp.valueOf(startTime), Timestamp.valueOf(endTime));
    }

    @Override
    // 计算船舶每个工况的持续时间
    public Map<String, Long> getShipWorkDuration(LocalDateTime startTime, LocalDateTime endTime) {
        List<GPSLog> shipGPSLogList = getShipNavByDateTimeBetween(startTime, endTime);
        Map<String, Long> workDurationMap = new HashMap<>();

        GPSLog previousLog = null;

        for (GPSLog gpsLog : shipGPSLogList) {
            if (previousLog != null && previousLog.getWorkStatus() != null) {
                // 计算当前工况的持续时间
                Duration duration = Duration.between(
                        previousLog.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        gpsLog.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                );

                // 获取当前工况
                String workStatus = previousLog.getWorkStatus().getCode();

                // 累加该工况的持续时间
                workDurationMap.merge(workStatus, duration.getSeconds(), Long::sum);
            }
            previousLog = gpsLog;
        }

        return workDurationMap;
    }

    @Override
    public LocalDateTime getNearlyShipLogDate() {
        UserShipRole userShipRole = getUserShipRole();
        return shipGPSLogMapper.getNearlyShipGPSLogDate(userShipRole.getShipId());
    }

    @Override
    public RealTimeData getRealTimeData() {
        UserShipRole userShipRole = getUserShipRole();
        RealTimeData realTimeData = new RealTimeData();

        // 设置当前时间
        realTimeData.setTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        // 从 Redis 中获取实时数据
        Object redisRealtimeStr = redisUtils.getString("realtime:%s".formatted(userShipRole.getShipId()));
        AnalyzeData analyzeDataMsg = redisUtils.get("newest-analyze-data-%s".formatted(userShipRole.getShipId()), AnalyzeData.class);
        RealtimeRedisMsg redisMsg = JSONObject.parseObject(JSON.toJSONString(redisRealtimeStr), RealtimeRedisMsg.class);



        // 获取 GPS 日志
        GPSLog gpsLog = (redisMsg != null) ? redisMsg.getGps_log() : gpsLogMapper.selectLeastGPSLogByShipId(userShipRole.getShipId());
        if (gpsLog != null) {
            realTimeData.setDirection(gpsLog.getDirection());
            realTimeData.setSpeed(gpsLog.getSpeed());
            realTimeData.setPosition(new Position(gpsLog.getLatitude(), gpsLog.getLongitude()));
            realTimeData.setWorkStatus(gpsLog.getWorkStatus());
        }


        // 获取风速日志
        Object redisWeatherStr = redisUtils.getString("weather:%s".formatted(userShipRole.getShipId()));
        WindLog weatherData = JSONObject.parseObject(JSON.toJSONString(redisWeatherStr), WindLog.class);
        if (weatherData != null) {
            realTimeData.setWindSpeed(weatherData.getWindSpeed());
            realTimeData.setWindDirection(weatherData.getWindDirection());
        } else {
            WindLog windLog = windLogMapper.selectLeastWindLogByShipId(userShipRole.getShipId());
            if (windLog != null) {
                realTimeData.setWindSpeed(windLog.getWindSpeed());
                realTimeData.setWindDirection(windLog.getWindDirection());
            }
        }


        // 获取电池状态
        Double leftBatterySoc;
        if (redisMsg != null && redisMsg.getLeft_battery_log() != null && redisMsg.getLeft_battery_log().getSoc() != null) {
            leftBatterySoc = redisMsg.getLeft_battery_log().getSoc().doubleValue();
        } else {
            leftBatterySoc = batteryLogMapper.getLatestLeftSoc(userShipRole.getShipId());
        }

        Double rightBatterySoc;
        if (redisMsg != null && redisMsg.getRight_battery_log() != null && redisMsg.getRight_battery_log().getSoc() != null) {
            rightBatterySoc = redisMsg.getRight_battery_log().getSoc().doubleValue();
        } else {
            rightBatterySoc = batteryLogMapper.getLatestRightSoc(userShipRole.getShipId());
        }

        // 设置电池容量
        realTimeData.setLeftBatteryCapacity(leftBatterySoc);
        realTimeData.setRightBatteryCapacity(rightBatterySoc);

        // 获取螺旋桨数据
        PropellerData leftPropeller = (redisMsg != null) ? redisMsg.getLeft_propeller_data() : propellerMapper.getLatestLeftPropeller(userShipRole.getShipId());
        PropellerData rightPropeller = (redisMsg != null) ? redisMsg.getRight_propeller_data() : propellerMapper.getLatestRightPropeller(userShipRole.getShipId());

        // 设置螺旋桨数据
        realTimeData.setLeftPropeller(leftPropeller);
        realTimeData.setRightPropeller(rightPropeller);

        // 获取分析数据
        try {
            AnalyzeData newestAnalyzeData = (analyzeDataMsg != null) ? analyzeDataMsg : analyzeService.getAnalyzeDataByShipIdInRedis(userShipRole.getShipId());
            realTimeData.setSailDuration(Double.valueOf(newestAnalyzeData.getDailySailingDuration()));
            realTimeData.setSailRange(ObjectUtils.defaultIfNull(newestAnalyzeData.getDailySailingDistance(), 0.0).doubleValue());
            realTimeData.setPowerDissipation(ObjectUtils.defaultIfNull(newestAnalyzeData.getPreDistanceEnergyConsumption(), 0.0).doubleValue());
        } catch (ServerException ignored) {}

        // 初始化电池报警
        List<Boolean> mockBatteryAlarm = new ArrayList<>(Collections.nCopies(16, false));
        realTimeData.setLeftBatteryAlarm(mockBatteryAlarm);
        realTimeData.setRightBatteryAlarm(mockBatteryAlarm);

        return realTimeData;
    }


    @Resource
    private BatterySOCService batterySOCService;
    
    @Override
    public List<BatteryLog> getShipSocByDateTimeBetween(LocalDateTime start, LocalDateTime end) {
        UserShipRole userShipRole = getUserShipRole();
        // 使用BatterySOCService获取经过异常值检测和修复的SOC数据
        return batterySOCService.getProcessedSOCData(userShipRole.getShipId(), start, end);
    }
}
