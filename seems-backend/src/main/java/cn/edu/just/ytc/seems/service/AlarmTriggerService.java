package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.dto.ApplyAlarmStrategyRequest;

/**
 * 报警触发服务接口
 */
public interface AlarmTriggerService {

    /**
     * 检查并触发报警（Python脚本调用）
     *
     * @param shipId 船舶ID
     * @param alarmType 报警类型
     * @param value 当前值
     * @param relatedDataId 关联数据ID
     * @return 是否触发报警
     */
    Boolean checkAndTrigger(Long shipId, String alarmType, Double value, Long relatedDataId);

    /**
     * 检查并触发报警（带自定义模板变量）
     *
     * @param request 应用请求
     * @return 是否触发报警
     */
    Boolean checkAndTriggerWithVariables(ApplyAlarmStrategyRequest request);

    /**
     * 检查是否应该触发报警（不实际触发）
     *
     * @param shipId 船舶ID
     * @param alarmType 报警类型
     * @param value 当前值
     * @return 是否应该触发
     */
    Boolean shouldTrigger(Long shipId, String alarmType, Double value);
}
