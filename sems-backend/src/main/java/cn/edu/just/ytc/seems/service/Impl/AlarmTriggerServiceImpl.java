package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.pojo.dto.ApplyAlarmStrategyRequest;
import cn.edu.just.ytc.seems.pojo.entity.AlarmStrategy;
import cn.edu.just.ytc.seems.service.AlarmStrategyService;
import cn.edu.just.ytc.seems.service.AlarmTriggerService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 报警触发服务实现类
 */
@Service
@Slf4j
public class AlarmTriggerServiceImpl implements AlarmTriggerService {

    @Resource
    private AlarmStrategyService alarmStrategyService;

    @Override
    public Boolean checkAndTrigger(Long shipId, String alarmType, Double value, Long relatedDataId) {
        try {
            // 构建请求
            ApplyAlarmStrategyRequest request = new ApplyAlarmStrategyRequest();
            request.setShipId(shipId);
            request.setAlarmType(alarmType);
            request.setValue(value);
            request.setRelatedDataId(relatedDataId);

            // 应用策略
            return alarmStrategyService.applyStrategy(request);
        } catch (Exception e) {
            log.error("检查并触发报警失败: shipId={}, alarmType={}, value={}, error={}",
                    shipId, alarmType, value, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Boolean checkAndTriggerWithVariables(ApplyAlarmStrategyRequest request) {
        try {
            return alarmStrategyService.applyStrategy(request);
        } catch (Exception e) {
            log.error("检查并触发报警失败: shipId={}, alarmType={}, value={}, error={}",
                    request.getShipId(), request.getAlarmType(), request.getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Boolean shouldTrigger(Long shipId, String alarmType, Double value) {
        try {
            // 获取有效策略
            AlarmStrategy strategy = alarmStrategyService.getEffectiveStrategy(shipId, alarmType);
            if (strategy == null) {
                return false;
            }

            // 检查触发条件
            return alarmStrategyService.checkTriggerCondition(value, strategy);
        } catch (Exception e) {
            log.error("检查是否应该触发报警失败: shipId={}, alarmType={}, value={}, error={}",
                    shipId, alarmType, value, e.getMessage());
            return false;
        }
    }
}
