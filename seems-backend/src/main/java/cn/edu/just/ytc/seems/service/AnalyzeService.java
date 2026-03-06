package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.entity.AnalyzeData;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface AnalyzeService extends IBaseUserInfo{
    void analyzeAll();
    CompletableFuture<Double> analyzeDailyEnergyConsumptionByShipId(BigInteger shipId);

    CompletableFuture<Map<String, Object>> analyzeDailySailDataByShipId(BigInteger shipId);

    CompletableFuture<Double> analyzeVoyageEnergyConsumptionByShipIdAndVoyageId(BigInteger shipId, BigInteger voyageId, boolean saveToDb);

    CompletableFuture<Map<String, Object>> analyzeVoyageSailDataByShipIdAndVoyageId(BigInteger shipId, BigInteger voyageId, boolean saveToDb);

    CompletableFuture<Double> analyzeHourlyEnergyConsumptionByShipId(BigInteger shipId);

    AnalyzeData getAnalyzeDataByShipIdInRedis(BigInteger shipId);

    AnalyzeData analyzeByShipId(BigInteger shipId);

    List<AnalyzeData> getNewestAnalyzeData(Timestamp start, Timestamp end);

    void analyzeImmediately();

    void setNewestAnalyzeData(AnalyzeData analyzeData);
}
