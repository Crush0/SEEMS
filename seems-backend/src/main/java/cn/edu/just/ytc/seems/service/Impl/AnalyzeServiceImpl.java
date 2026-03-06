package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.analyze.EnergyConsumptionCalc;
import cn.edu.just.ytc.seems.analyze.GPSCalc;
import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.*;
import cn.edu.just.ytc.seems.pojo.entity.*;
import cn.edu.just.ytc.seems.service.AnalyzeService;
import cn.edu.just.ytc.seems.utils.RedisUtils;
import cn.edu.just.ytc.seems.utils.SpringUtils;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service(value = "analyzeService")
public class AnalyzeServiceImpl implements AnalyzeService {

    @Resource
    private BatteryLogMapper batteryLogMapper;
    @Resource
    private AnalyzeDataMapper analyzeDataMapper;

    @Resource
    private RedisUtils   redisUtils;

    @Resource
    private ShipMapper shipMapper;

    @Resource
    private GPSLogMapper gpsLogMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private VoyageLogMapper voyageLogMapper;



    private UserShipRole getUserShipRole() {
        User user = getUser();
        UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(user.getId());
        if (userShipRole == null) {
            throw new ServerException("用户没有船舶权限");
        }
        return userShipRole;
    }

    public void analyzeImmediately() {
        UserShipRole userShipRole = getUserShipRole();
        ShipBaseInfo shipBaseInfo = shipMapper.selectById(userShipRole.getShipId());
        if (shipBaseInfo == null) {
            throw new ServerException("船舶不存在");
        }
        this.analyzeByShipId(userShipRole.getShipId());
    }

//    @Override
//    @Scheduled(cron = "0/30 * * * * ?")
//    public void analyzeDailyEnergyConsumption() {
//        AnalyzeService analyzeService = SpringUtils.getBean(AnalyzeService.class);
//        // 得到所有船只的ID
//        List<BigInteger> shipIdList = shipMapper.selectAllShipId();
//        for (BigInteger shipId : shipIdList) {
//            analyzeService.analyzeDailyEnergyConsumptionByShipId(shipId);
//        }
//    }

    public AnalyzeData analyzeByShipId(BigInteger shipId) {

        AnalyzeService analyzeService = SpringUtils.getBean(AnalyzeService.class);
        AnalyzeData lastAnalyzeData = analyzeDataMapper.getLastHourData(shipId);
        CompletableFuture<Double> future = analyzeService.analyzeDailyEnergyConsumptionByShipId(shipId);
        Double totalEnergyConsumption = future.join();
//            log.info("船舶ID为{}的日用电量数据分析完毕，总电能消耗为{}kWh", shipId, totalEnergyConsumption);
//            redisUtils.add2List("daily-energy-consumption-%s".formatted(shipId), new HashMap<>(){{
//                put("date", System.currentTimeMillis());
//                put("totalEnergyConsumption", totalEnergyConsumption);
//            }}, 24, TimeUnit.HOURS);

        future = analyzeService.analyzeHourlyEnergyConsumptionByShipId(shipId);
        Double hourlyEnergyConsumption = future.join();
//            log.info("船舶ID为{}的每小时用电量数据分析完毕，总电能消耗为{}kWh", shipId, hourlyEnergyConsumption);
//            redisUtils.add2List("hourly-energy-consumption-%s".formatted(shipId), new HashMap<>(){{
//                put("date", System.currentTimeMillis());
//                put("hourlyEnergyConsumption", hourlyEnergyConsumption);
//            }}, 24, TimeUnit.HOURS);
        BigDecimal totalCarbonEmission = EnergyConsumptionCalc.calculateTotalCarbonEmission(BigDecimal.valueOf(totalEnergyConsumption));
        CompletableFuture<Map<String, Object>> sailTimeFuture = analyzeService.analyzeDailySailDataByShipId(shipId);
        Map<String, Object> sailObj = sailTimeFuture.join();
        AnalyzeData analyzeData = new AnalyzeData();
        analyzeData.setAnalyzeTime(new Date());
        analyzeData.setShipId(shipId);
        analyzeData.setDailyCarbonEmission(totalCarbonEmission);
        analyzeData.setDailyEnergyConsumption(BigDecimal.valueOf(totalEnergyConsumption));
        analyzeData.setPreHourEnergyConsumption(BigDecimal.valueOf(hourlyEnergyConsumption));
        analyzeData.setDailySailingDuration((Long) sailObj.getOrDefault("totalDailySailingTime", 0L));
        analyzeData.setDailySailingDistance(BigDecimal.valueOf((Double) sailObj.getOrDefault("totalDailySailingDistance", 0.0)));
        if (analyzeData.getDailySailingDistance().compareTo(new BigDecimal("0.0")) > 0) { // 防止除数为0（会有异常 NaN）
            // 计算每公里用电量
            analyzeData.setPreDistanceEnergyConsumption(analyzeData.getDailyEnergyConsumption().divide(analyzeData.getDailySailingDistance(), RoundingMode.HALF_UP));
        }
        // 航程累计
        if (lastAnalyzeData == null) {
            analyzeData.setSailingDistance(analyzeData.getDailySailingDistance());
        } else {
            analyzeData.setSailingDistance(lastAnalyzeData.getSailingDistance());
        }
        analyzeService.setNewestAnalyzeData(analyzeData);
        redisUtils.set("newest-analyze-data-%s".formatted(shipId), JSONObject.toJSONString(analyzeData));
        analyzeDataMapper.insert(analyzeData);
        return analyzeData;
    }

    @Override
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void analyzeAll() {
        log.info("开始分析所有船只的日用电量数据");

        // 得到所有船只的ID
        List<BigInteger> shipIdList = shipMapper.selectAllShipId();
        shipIdList.forEach(this::analyzeByShipId);
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Double> analyzeDailyEnergyConsumptionByShipId(BigInteger shipId) {
        ShipBaseInfo shipBaseInfo = shipMapper.selectById(shipId);
//        log.info("开始分析船舶ID为{}的日用电量数据", shipId);
        BigDecimal maxBatteryCapacity = shipBaseInfo.getMaxBatteryCapacity();
        List<List<BatteryLog>> batteryClusters = EnergyConsumptionCalc.fetchBatteryLogsForToday(batteryLogMapper, shipId);
        BigDecimal totalEnergyConsumption = EnergyConsumptionCalc.calculateTotalEnergyConsumption(batteryClusters, maxBatteryCapacity);
        return CompletableFuture.completedFuture(totalEnergyConsumption.doubleValue());
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Map<String, Object>> analyzeDailySailDataByShipId(BigInteger shipId) {
        LocalDate today = LocalDate.now();
        Timestamp startOfDay = Timestamp.valueOf(today.atStartOfDay());
        Timestamp endOfDay = Timestamp.valueOf(today.plusDays(1).atStartOfDay());
        List<GPSLog> gpsLogs = gpsLogMapper.getShipGPSLogByShipIdAndDateTimeBetween(shipId, startOfDay, endOfDay);
        Long totalDailySailingTime = GPSCalc.calculateTotalSailingTime(gpsLogs);
        Double totalDailySailingDistance = GPSCalc.calculateTotalSailingDistance(gpsLogs);
        return CompletableFuture.completedFuture(new HashMap<>(){{
            put("totalDailySailingTime", totalDailySailingTime);
            put("totalDailySailingDistance", totalDailySailingDistance);
        }});
    }

    @Async("taskExecutor")
    @Override
    public CompletableFuture<Double> analyzeVoyageEnergyConsumptionByShipIdAndVoyageId(BigInteger shipId, BigInteger voyageId, boolean saveToDb) {
        ShipBaseInfo shipBaseInfo = shipMapper.selectById(shipId);
        List<List<BatteryLog>> batteryClusters = EnergyConsumptionCalc.fetchBatteryLogsByVoyage(batteryLogMapper, shipId, voyageId);
        BigDecimal totalEnergyConsumption = EnergyConsumptionCalc.calculateTotalEnergyConsumption(batteryClusters, shipBaseInfo.getMaxBatteryCapacity());
        if (saveToDb) {
            VoyageLog voyageLog = voyageLogMapper.selectById(voyageId);
            if (voyageLog == null) {
                throw new ServerException("航次不存在");
            }
            voyageLog.setVoyagePowerConsumption(totalEnergyConsumption.doubleValue());
            voyageLogMapper.updateById(voyageLog);
        }
        return CompletableFuture.completedFuture(totalEnergyConsumption.doubleValue());
    }

    @Async("taskExecutor")
    @Override
    public CompletableFuture<Map<String, Object>> analyzeVoyageSailDataByShipIdAndVoyageId(BigInteger shipId, BigInteger voyageId, boolean saveToDb) {
        List<GPSLog> gpsLogs = gpsLogMapper.getShipGPSLogByShipIdAndVoyageId(shipId, voyageId);
        Long totalVoyageSailingTime = GPSCalc.calculateTotalSailingTime(gpsLogs);
        Double totalVoyageSailingDistance = GPSCalc.calculateTotalSailingDistance(gpsLogs);
        if (saveToDb) {
            VoyageLog voyageLog = voyageLogMapper.selectById(voyageId);
            if (voyageLog == null) {
                throw new ServerException("航次不存在");
            }
            voyageLog.setSailingTime(totalVoyageSailingTime);
            voyageLog.setVoyageDistance(totalVoyageSailingDistance);
            voyageLogMapper.updateById(voyageLog);
        }
        return CompletableFuture.completedFuture(new HashMap<>(){{
            put("totalVoyageSailingTime", totalVoyageSailingTime);
            put("totalVoyageSailingDistance", totalVoyageSailingDistance);
        }});
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Double> analyzeHourlyEnergyConsumptionByShipId(BigInteger shipId) {
        ShipBaseInfo shipBaseInfo = shipMapper.selectById(shipId);
        BigDecimal maxBatteryCapacity = shipBaseInfo.getMaxBatteryCapacity();
        List<List<BatteryLog>> batteryClusters = EnergyConsumptionCalc.fetchBatteryLogsForHourly(batteryLogMapper, shipId);
        BigDecimal hourlyEnergyConsumption = EnergyConsumptionCalc.calculateTotalEnergyConsumption(batteryClusters, maxBatteryCapacity);
//        log.info("开始分析船舶ID为{}的每小时用电量数据", shipId);
        return CompletableFuture.completedFuture(hourlyEnergyConsumption.doubleValue());
    }

    @Override
    public AnalyzeData getAnalyzeDataByShipIdInRedis(BigInteger shipId) {
        String key = "newest-analyze-data-%s".formatted(shipId);
        AnalyzeData newestAnalyzeData = redisUtils.get(key, AnalyzeData.class);
        if (newestAnalyzeData == null) {
            throw new ServerException("暂无最新分析数据");
        }
        return newestAnalyzeData;
    }

    @Override
    public void setNewestAnalyzeData(AnalyzeData analyzeData) {
        BigInteger shipId = analyzeData.getShipId();
        String key = "newest-analyze-data-%s".formatted(shipId);
        if (redisUtils.hasKey(key)) {
            redisUtils.delete(key);
        }
        redisUtils.set(key, JSONObject.toJSONString(analyzeData));
    }

    @Override
    public List<AnalyzeData> getNewestAnalyzeData(Timestamp startTime, Timestamp endTime) {
        UserShipRole userShipRole = getUserShipRole();
        BigInteger shipId = userShipRole.getShipId();
        return analyzeDataMapper.getNewestData(shipId, startTime, endTime);
    }
}
