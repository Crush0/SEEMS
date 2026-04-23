package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.*;
import cn.edu.just.ytc.seems.pojo.dto.*;
import cn.edu.just.ytc.seems.pojo.entity.*;
import cn.edu.just.ytc.seems.pojo.enums.ReceiverType;
import cn.edu.just.ytc.seems.pojo.enums.TriggerCondition;
import cn.edu.just.ytc.seems.service.AlarmLogService;
import cn.edu.just.ytc.seems.service.AlarmStrategyService;
import cn.edu.just.ytc.seems.service.MessageService;
import cn.edu.just.ytc.seems.utils.SocketUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报警策略服务实现类
 */
@Service
@Slf4j
public class AlarmStrategyServiceImpl extends ServiceImpl<AlarmStrategyMapper, AlarmStrategy>
        implements AlarmStrategyService {

    private static final String STRATEGY_CACHE_KEY = "alarm:strategy:cache:";
    private static final String STRATEGY_LOCK_KEY = "alarm:strategy:lock:";

    @Resource
    private AlarmStrategyMapper strategyMapper;

    @Resource
    private AlarmStrategyReceiverMapper receiverMapper;

    @Resource
    private ShipMapper shipMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private AlarmLogService alarmLogService;

    @Resource
    private MessageService messageService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStrategy(CreateAlarmStrategyRequest request) {
        // 参数验证
        validateStrategyRequest(request);

        // 验证BETWEEN条件必须有两个阈值
        if (request.getTriggerCondition() == TriggerCondition.BETWEEN) {
            if (request.getThresholdValue2() == null) {
                throw new ServerException("BETWEEN条件必须设置两个阈值");
            }
            if (request.getThresholdValue() >= request.getThresholdValue2()) {
                throw new ServerException("阈值1必须小于阈值2");
            }
        }

        // 验证DURATION时机必须设置持续时间
        if (request.getTriggerTiming() == cn.edu.just.ytc.seems.pojo.enums.TriggerTiming.DURATION) {
            if (request.getDurationSeconds() == null || request.getDurationSeconds() <= 0) {
                throw new ServerException("持续触发必须设置有效持续时间");
            }
        }

        // 验证接收人列表
        if (request.getReceivers() == null || request.getReceivers().isEmpty()) {
            throw new ServerException("接收人列表不能为空");
        }

        // 验证船舶存在
        if (request.getShipId() != null) {
            ShipBaseInfo ship = shipMapper.selectById(request.getShipId());
            if (ship == null) {
                throw new ServerException("船舶不存在");
            }
        }

        // 创建策略
        AlarmStrategy strategy = new AlarmStrategy();
        strategy.setShipId(request.getShipId());
        strategy.setAlarmType(request.getAlarmType());
        strategy.setAlarmLevel(request.getAlarmLevel());
        strategy.setTriggerCondition(request.getTriggerCondition());
        strategy.setThresholdValue(java.math.BigDecimal.valueOf(request.getThresholdValue()));
        strategy.setThresholdValue2(request.getThresholdValue2() != null ?
                java.math.BigDecimal.valueOf(request.getThresholdValue2()) : null);
        strategy.setTriggerTiming(request.getTriggerTiming());
        strategy.setDurationSeconds(request.getDurationSeconds());
        strategy.setEnableNotification(request.getEnableNotification());
        strategy.setTitleTemplate(request.getTitleTemplate());
        strategy.setContentTemplate(request.getContentTemplate());
        strategy.setIsEnabled(true);
        strategy.setPriority(request.getPriority() != null ? request.getPriority() : 0);

        strategyMapper.insert(strategy);

        // 创建接收人关联
        for (ReceiverConfig receiverConfig : request.getReceivers()) {
            AlarmStrategyReceiver receiver = new AlarmStrategyReceiver();
            receiver.setStrategyId(strategy.getId().longValue());
            receiver.setReceiverType(receiverConfig.getReceiverType());
            receiver.setReceiverId(receiverConfig.getReceiverId());
            receiver.setReceiverRole(receiverConfig.getReceiverRole());
            receiverMapper.insert(receiver);
        }

        // 清除缓存
        clearStrategyCache(request.getShipId(), request.getAlarmType());

        log.info("创建报警策略成功: ID={}, 船舶ID={}, 类型={}",
                strategy.getId(), request.getShipId(), request.getAlarmType());

        return strategy.getId().longValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStrategy(UpdateAlarmStrategyRequest request) {
        if (request.getId() == null) {
            throw new ServerException("策略ID不能为空");
        }

        // 查询原策略
        AlarmStrategy existingStrategy = strategyMapper.selectById(request.getId());
        if (existingStrategy == null) {
            throw new ServerException("策略不存在");
        }

        // 参数验证
        if (request.getTriggerCondition() == TriggerCondition.BETWEEN) {
            if (request.getThresholdValue2() == null) {
                throw new ServerException("BETWEEN条件必须设置两个阈值");
            }
            if (request.getThresholdValue() >= request.getThresholdValue2()) {
                throw new ServerException("阈值1必须小于阈值2");
            }
        }

        if (request.getTriggerTiming() == cn.edu.just.ytc.seems.pojo.enums.TriggerTiming.DURATION) {
            if (request.getDurationSeconds() == null || request.getDurationSeconds() <= 0) {
                throw new ServerException("持续触发必须设置有效持续时间");
            }
        }

        // 验证船舶存在
        if (request.getShipId() != null) {
            ShipBaseInfo ship = shipMapper.selectById(request.getShipId());
            if (ship == null) {
                throw new ServerException("船舶不存在");
            }
        }

        // 更新策略
        existingStrategy.setShipId(request.getShipId());
        existingStrategy.setAlarmType(request.getAlarmType());
        existingStrategy.setAlarmLevel(request.getAlarmLevel());
        existingStrategy.setTriggerCondition(request.getTriggerCondition());
        existingStrategy.setThresholdValue(java.math.BigDecimal.valueOf(request.getThresholdValue()));
        existingStrategy.setThresholdValue2(request.getThresholdValue2() != null ?
                java.math.BigDecimal.valueOf(request.getThresholdValue2()) : null);
        existingStrategy.setTriggerTiming(request.getTriggerTiming());
        existingStrategy.setDurationSeconds(request.getDurationSeconds());
        existingStrategy.setEnableNotification(request.getEnableNotification());
        existingStrategy.setTitleTemplate(request.getTitleTemplate());
        existingStrategy.setContentTemplate(request.getContentTemplate());
        existingStrategy.setPriority(request.getPriority());

        strategyMapper.updateById(existingStrategy);

        // 删除旧的接收人关联
        receiverMapper.deleteByStrategyId(request.getId());

        // 创建新的接收人关联
        if (request.getReceivers() != null) {
            for (ReceiverConfig receiverConfig : request.getReceivers()) {
                AlarmStrategyReceiver receiver = new AlarmStrategyReceiver();
                receiver.setStrategyId(request.getId());
                receiver.setReceiverType(receiverConfig.getReceiverType());
                receiver.setReceiverId(receiverConfig.getReceiverId());
                receiver.setReceiverRole(receiverConfig.getReceiverRole());
                receiverMapper.insert(receiver);
            }
        }

        // 清除缓存
        clearStrategyCache(request.getShipId(), request.getAlarmType());

        log.info("更新报警策略成功: ID={}", request.getId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteStrategy(Long strategyId) {
        if (strategyId == null) {
            throw new ServerException("策略ID不能为空");
        }

        AlarmStrategy strategy = strategyMapper.selectById(strategyId);
        if (strategy == null) {
            throw new ServerException("策略不存在");
        }

        // 删除策略（逻辑删除）
        strategyMapper.deleteById(strategyId);

        // 删除接收人关联
        receiverMapper.deleteByStrategyId(strategyId);

        // 清除缓存
        clearStrategyCache(strategy.getShipId(), strategy.getAlarmType());

        log.info("删除报警策略成功: ID={}", strategyId);

        return true;
    }

    @Override
    public Page<AlarmStrategyVO> queryStrategyList(QueryAlarmStrategyRequest request) {
        // 构建分页对象
        Page<AlarmStrategy> page = Page.of(request.getCurrent(), request.getPageSize());

        // 构建查询条件
        QueryWrapper<AlarmStrategy> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);

        // 船舶ID筛选
        if (request.getShipId() != null) {
            queryWrapper.eq("ship_id", request.getShipId());
        }

        // 报警类型筛选
        if (StringUtils.hasText(request.getAlarmType())) {
            queryWrapper.eq("alarm_type", request.getAlarmType());
        }

        // 启用状态筛选
        if (request.getIsEnabled() != null) {
            queryWrapper.eq("is_enabled", request.getIsEnabled());
        }

        // 按优先级和创建时间排序
        queryWrapper.orderByDesc("priority");
        queryWrapper.orderByDesc("create_date");

        // 执行查询
        IPage<AlarmStrategy> strategyPage = strategyMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<AlarmStrategyVO> voList = strategyPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建响应
        Page<AlarmStrategyVO> result = new Page<>(request.getCurrent(), request.getPageSize(),
                strategyPage.getTotal());
        result.setRecords(voList);

        return result;
    }

    @Override
    public AlarmStrategyVO getStrategy(Long strategyId) {
        if (strategyId == null) {
            throw new ServerException("策略ID不能为空");
        }

        AlarmStrategy strategy = strategyMapper.selectById(strategyId);
        if (strategy == null) {
            throw new ServerException("策略不存在");
        }

        return convertToVO(strategy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enableStrategy(Long strategyId, Boolean enabled) {
        if (strategyId == null) {
            throw new ServerException("策略ID不能为空");
        }

        AlarmStrategy strategy = strategyMapper.selectById(strategyId);
        if (strategy == null) {
            throw new ServerException("策略不存在");
        }

        strategy.setIsEnabled(enabled);
        strategyMapper.updateById(strategy);

        // 清除缓存
        clearStrategyCache(strategy.getShipId(), strategy.getAlarmType());

        log.info("{}报警策略: ID={}", enabled ? "启用" : "禁用", strategyId);

        return true;
    }

    @Override
    public AlarmStrategy getEffectiveStrategy(Long shipId, String alarmType) {
        // 先从缓存获取
        String cacheKey = STRATEGY_CACHE_KEY + shipId + ":" + alarmType;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof AlarmStrategy) {
            return (AlarmStrategy) cached;
        }

        // 查询数据库
        AlarmStrategy strategy = strategyMapper.selectEffectiveStrategy(shipId, alarmType);

        // 缓存结果（5分钟）
        if (strategy != null) {
            redisTemplate.opsForValue().set(cacheKey, strategy, 300, java.util.concurrent.TimeUnit.SECONDS);
        }

        return strategy;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyStrategy(ApplyAlarmStrategyRequest request) {
        Long shipId = request.getShipId();
        String alarmType = request.getAlarmType();
        Double value = request.getValue();

        // 获取有效策略
        AlarmStrategy strategy = getEffectiveStrategy(shipId, alarmType);

        // 没有配置策略，不触发报警
        if (strategy == null) {
            log.debug("未配置报警策略: 船舶ID={}, 类型={}", shipId, alarmType);
            return false;
        }

        // 检查触发条件
        Boolean shouldTrigger = checkTriggerCondition(value, strategy);
        if (!shouldTrigger) {
            return false;
        }

        // 创建报警
        createAlarmByStrategy(strategy, request);

        return true;
    }

    @Override
    public Boolean checkTriggerCondition(Double value, AlarmStrategy strategy) {
        if (strategy == null || strategy.getTriggerCondition() == null) {
            return false;
        }

        TriggerCondition condition = strategy.getTriggerCondition();
        Double threshold1 = strategy.getThresholdValue().doubleValue();
        Double threshold2 = strategy.getThresholdValue2() != null ?
                strategy.getThresholdValue2().doubleValue() : null;

        switch (condition) {
            case LESS_THAN:
                return value < threshold1;
            case GREATER_THAN:
                return value > threshold1;
            case BETWEEN:
                return threshold2 != null && value >= threshold1 && value <= threshold2;
            case EQUAL:
                return Math.abs(value - threshold1) < 0.01;
            default:
                return false;
        }
    }

    @Override
    public String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null) {
            return "";
        }

        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * 根据策略创建报警
     */
    private void createAlarmByStrategy(AlarmStrategy strategy, ApplyAlarmStrategyRequest request) {
        try {
            // 查询船舶信息
            ShipBaseInfo ship = shipMapper.selectById(request.getShipId());
            if (ship == null) {
                log.error("船舶不存在: 船舶ID={}", request.getShipId());
                return;
            }

            // 准备模板变量
            Map<String, Object> variables = new HashMap<>();
            variables.put("ship_name", ship.getName());
            variables.put("alarm_type", getAlarmTypeName(strategy.getAlarmType()));
            variables.put("value", String.format("%.2f", request.getValue()));
            variables.put("threshold", strategy.getThresholdValue().toString());
            variables.put("alarm_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 添加自定义变量
            if (request.getTemplateVariables() != null) {
                variables.putAll(request.getTemplateVariables());
            }

            // 渲染标题和内容
            String title = renderTemplate(strategy.getTitleTemplate(), variables);
            String content = renderTemplate(strategy.getContentTemplate(), variables);

            // 创建报警日志
            CreateAlarmLogRequest alarmRequest = new CreateAlarmLogRequest();
            alarmRequest.setShipId(request.getShipId());
            alarmRequest.setAlarmType(strategy.getAlarmType());
            alarmRequest.setAlarmLevel(strategy.getAlarmLevel());
            alarmRequest.setTitle(title);
            alarmRequest.setContent(content);
            alarmRequest.setRelatedDataId(request.getRelatedDataId());

            alarmLogService.createAlarm(alarmRequest);

            log.warn("策略触发报警: 船舶={}, 类型={}, 值={}, 阈值={}",
                    ship.getName(), strategy.getAlarmType(),
                    request.getValue(), strategy.getThresholdValue());

            // 发送消息通知
            if (strategy.getEnableNotification()) {
                sendAlarmNotification(strategy, title, content, request.getShipId());
            }

        } catch (Exception e) {
            log.error("创建策略报警失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送报警通知
     */
    private void sendAlarmNotification(AlarmStrategy strategy, String title, String content, Long shipId) {
        try {
            // 查询接收人
            List<AlarmStrategyReceiver> receivers = receiverMapper.selectByStrategyId(strategy.getId().longValue());

            for (AlarmStrategyReceiver receiver : receivers) {
                if (receiver.getReceiverType() == ReceiverType.ROLE) {
                    // 发送给角色组
                    sendMessageToRole(receiver.getReceiverRole(), title, content);
                } else if (receiver.getReceiverType() == ReceiverType.USER) {
                    // 发送给具体用户
                    sendMessageToUser(receiver.getReceiverId(), title, content);
                }
            }

            log.info("报警通知已发送: 策略ID={}, 接收人数={}", strategy.getId(), receivers.size());
        } catch (Exception e) {
            log.error("发送报警通知失败: {}", e.getMessage());
        }
    }

    /**
     * 发送消息给角色组
     */
    private void sendMessageToRole(String role, String title, String content) {
        try {
            SendMessageRequest request = new SendMessageRequest();
            request.setRole(UserShipRole.Role.valueOf(role.toUpperCase()));
            request.setTitle(title);
            request.setContent(content);
            request.setType(Message.MessageType.NOTICE);

            messageService.sendMessage(request);
        } catch (Exception e) {
            log.error("发送角色消息失败: role={}, error={}", role, e.getMessage());
        }
    }

    /**
     * 发送消息给具体用户
     */
    private void sendMessageToUser(Long userId, String title, String content) {
        try {
            SendMessageRequest request = new SendMessageRequest();
            request.setReceiverId(java.math.BigInteger.valueOf(userId));
            request.setTitle(title);
            request.setContent(content);
            request.setType(Message.MessageType.NOTICE);

            messageService.sendMessage(request);

            // 实时推送
            SocketUtil.sendToOne(userId.toString(), Map.of(
                    "title", title,
                    "content", content,
                    "type", "alarm",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("发送用户消息失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 转换为VO对象
     */
    private AlarmStrategyVO convertToVO(AlarmStrategy strategy) {
        AlarmStrategyVO vo = new AlarmStrategyVO();
        vo.setId(strategy.getId().longValue());
        vo.setShipId(strategy.getShipId());

        // 查询船舶名称
        if (strategy.getShipId() != null) {
            ShipBaseInfo ship = shipMapper.selectById(strategy.getShipId());
            if (ship != null) {
                vo.setShipName(ship.getName());
            }
        }

        vo.setAlarmType(strategy.getAlarmType());
        vo.setAlarmTypeName(getAlarmTypeName(strategy.getAlarmType()));
        vo.setAlarmLevel(strategy.getAlarmLevel());
        vo.setAlarmLevelName(getAlarmLevelName(strategy.getAlarmLevel()));
        vo.setTriggerCondition(strategy.getTriggerCondition());
        vo.setThresholdValue(strategy.getThresholdValue() != null ?
                strategy.getThresholdValue().doubleValue() : null);
        vo.setThresholdValue2(strategy.getThresholdValue2() != null ?
                strategy.getThresholdValue2().doubleValue() : null);
        vo.setTriggerTiming(strategy.getTriggerTiming());
        vo.setDurationSeconds(strategy.getDurationSeconds());
        vo.setEnableNotification(strategy.getEnableNotification());
        vo.setTitleTemplate(strategy.getTitleTemplate());
        vo.setContentTemplate(strategy.getContentTemplate());
        vo.setIsEnabled(strategy.getIsEnabled());
        vo.setPriority(strategy.getPriority());
        vo.setCreateDate(strategy.getCreateDate());
        vo.setUpdateDate(strategy.getUpdateDate());

        // 查询接收人列表
        List<AlarmStrategyReceiver> receivers = receiverMapper.selectByStrategyId(strategy.getId().longValue());
        List<AlarmStrategyVO.ReceiverInfo> receiverInfos = receivers.stream().map(receiver -> {
            AlarmStrategyVO.ReceiverInfo info = new AlarmStrategyVO.ReceiverInfo();
            info.setId(receiver.getId().longValue());
            info.setReceiverType(receiver.getReceiverType());
            info.setReceiverId(receiver.getReceiverId());
            info.setReceiverRole(receiver.getReceiverRole());

            // 查询接收人名称
            if (receiver.getReceiverType() == ReceiverType.USER && receiver.getReceiverId() != null) {
                User user = userMapper.selectById(receiver.getReceiverId());
                if (user != null) {
                    info.setReceiverName(user.getUsername());
                }
            } else if (receiver.getReceiverType() == ReceiverType.ROLE) {
                info.setReceiverName(getRoleName(receiver.getReceiverRole()));
            }

            return info;
        }).collect(Collectors.toList());

        vo.setReceivers(receiverInfos);

        return vo;
    }

    /**
     * 获取报警类型名称
     */
    private String getAlarmTypeName(String alarmType) {
        try {
            AlarmLog.AlarmType type = AlarmLog.AlarmType.valueOf(alarmType.toUpperCase());
            return type.getName();
        } catch (IllegalArgumentException e) {
            return alarmType;
        }
    }

    /**
     * 获取报警级别名称
     */
    private String getAlarmLevelName(String alarmLevel) {
        try {
            AlarmLog.AlarmLevel level = AlarmLog.AlarmLevel.valueOf(alarmLevel.toUpperCase());
            return level.getName();
        } catch (IllegalArgumentException e) {
            return alarmLevel;
        }
    }

    /**
     * 获取角色名称
     */
    private String getRoleName(String role) {
        if (role == null) {
            return "";
        }
        switch (role.toUpperCase()) {
            case "ADMIN":
                return "管理员";
            case "OPERATOR":
                return "操作员";
            case "USER":
                return "普通用户";
            default:
                return role;
        }
    }

    /**
     * 验证策略请求
     */
    private void validateStrategyRequest(CreateAlarmStrategyRequest request) {
        if (!StringUtils.hasText(request.getAlarmType())) {
            throw new ServerException("报警类型不能为空");
        }
        if (!StringUtils.hasText(request.getAlarmLevel())) {
            throw new ServerException("报警级别不能为空");
        }
        if (request.getTriggerCondition() == null) {
            throw new ServerException("触发条件不能为空");
        }
        if (request.getThresholdValue() == null) {
            throw new ServerException("阈值1不能为空");
        }
        if (request.getTriggerTiming() == null) {
            throw new ServerException("触发时机不能为空");
        }
        if (request.getEnableNotification() == null) {
            throw new ServerException("是否发送消息不能为空");
        }
        if (!StringUtils.hasText(request.getTitleTemplate())) {
            throw new ServerException("标题模板不能为空");
        }
        if (!StringUtils.hasText(request.getContentTemplate())) {
            throw new ServerException("内容模板不能为空");
        }
    }

    /**
     * 清除策略缓存
     */
    private void clearStrategyCache(Long shipId, String alarmType) {
        String cacheKey = STRATEGY_CACHE_KEY + shipId + ":" + alarmType;
        redisTemplate.delete(cacheKey);
    }
}
