package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.entity.BatteryLog;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 电池SOC数据查询服务接口
 */
public interface BatterySOCService extends IBaseUserInfo {

    /**
     * 获取指定时间范围内的电池SOC真实数据
     * @param shipId 船舶ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 电池SOC数据列表（原始真实值）
     */
    List<BatteryLog> getProcessedSOCData(BigInteger shipId, LocalDateTime startTime, LocalDateTime endTime);
}