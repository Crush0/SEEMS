package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.dto.*;
import cn.edu.just.ytc.seems.pojo.entity.AlarmStrategy;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 报警策略服务接口
 */
public interface AlarmStrategyService extends IService<AlarmStrategy> {

    /**
     * 创建报警策略
     *
     * @param request 创建请求
     * @return 策略ID
     */
    Long createStrategy(CreateAlarmStrategyRequest request);

    /**
     * 更新报警策略
     *
     * @param request 更新请求
     * @return 是否成功
     */
    Boolean updateStrategy(UpdateAlarmStrategyRequest request);

    /**
     * 删除报警策略
     *
     * @param strategyId 策略ID
     * @return 是否成功
     */
    Boolean deleteStrategy(Long strategyId);

    /**
     * 查询策略列表（分页）
     *
     * @param request 查询请求
     * @return 策略列表
     */
    Page<AlarmStrategyVO> queryStrategyList(QueryAlarmStrategyRequest request);

    /**
     * 获取策略详情
     *
     * @param strategyId 策略ID
     * @return 策略详情
     */
    AlarmStrategyVO getStrategy(Long strategyId);

    /**
     * 启用/禁用策略
     *
     * @param strategyId 策略ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    Boolean enableStrategy(Long strategyId, Boolean enabled);

    /**
     * 获取有效的策略（船舶特定策略 > 全局策略）
     *
     * @param shipId 船舶ID
     * @param alarmType 报警类型
     * @return 策略对象
     */
    AlarmStrategy getEffectiveStrategy(Long shipId, String alarmType);

    /**
     * 应用策略（Python脚本调用）
     *
     * @param request 应用请求
     * @return 是否触发报警
     */
    Boolean applyStrategy(ApplyAlarmStrategyRequest request);

    /**
     * 检查触发条件
     *
     * @param value 当前值
     * @param strategy 策略对象
     * @return 是否触发
     */
    Boolean checkTriggerCondition(Double value, AlarmStrategy strategy);

    /**
     * 渲染模板
     *
     * @param template 模板字符串
     * @param variables 模板变量
     * @return 渲染后的字符串
     */
    String renderTemplate(String template, java.util.Map<String, Object> variables);
}
