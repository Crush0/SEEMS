package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.mapper.BatteryLogMapper;
import cn.edu.just.ytc.seems.pojo.entity.BatteryLog;
import cn.edu.just.ytc.seems.service.BatterySOCService;
import cn.edu.just.ytc.seems.service.Impl.BaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 电池SOC数据查询服务实现类
 * 直接返回原始真实数据，不进行任何插值、拟合或平滑处理
 */
@Service
public class BatterySOCServiceImpl extends BaseService implements BatterySOCService {

    @Resource
    private BatteryLogMapper batteryLogMapper;

    /**
     * 获取指定时间范围内的电池SOC真实数据
     * 直接返回原始测量值，不进行任何数据处理
     */
    @Override
    public List<BatteryLog> getProcessedSOCData(BigInteger shipId, LocalDateTime startTime, LocalDateTime endTime) {
        // 直接返回原始电池日志数据，不进行任何插值、拟合或异常处理
        return fetchBatteryLogsByTimeRange(shipId, startTime, endTime);
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

        List<BatteryLog> result = batteryLogMapper.selectList(queryWrapper);
        return result != null ? result : new ArrayList<>();
    }
}