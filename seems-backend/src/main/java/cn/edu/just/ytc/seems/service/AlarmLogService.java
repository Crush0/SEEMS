package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.dto.CreateAlarmLogRequest;
import cn.edu.just.ytc.seems.pojo.dto.QueryAlarmLogRequest;
import cn.edu.just.ytc.seems.pojo.dto.QueryAlarmLogResp;

/**
 * 报警日志服务接口
 */
public interface AlarmLogService {

    /**
     * 创建报警日志
     *
     * @param request 创建报警请求
     */
    void createAlarm(CreateAlarmLogRequest request);

    /**
     * 查询报警日志列表
     *
     * @param request 查询请求
     * @return 查询响应
     */
    QueryAlarmLogResp queryAlarmList(QueryAlarmLogRequest request);

    /**
     * 标记报警为已处理
     *
     * @param alarmId 报警ID
     */
    void markAsHandled(Long alarmId);

    /**
     * 获取未处理报警数量
     *
     * @param shipId 船舶ID
     * @return 未处理报警数量
     */
    Integer getUnhandledCount(Long shipId);

    /**
     * 创建SOC低电量报警
     *
     * @param shipId 船舶ID
     * @param socValue 当前SOC值
     * @param relatedDataId 关联数据ID
     */
    void createLowSocAlarm(Long shipId, Double socValue, Long relatedDataId);
}
