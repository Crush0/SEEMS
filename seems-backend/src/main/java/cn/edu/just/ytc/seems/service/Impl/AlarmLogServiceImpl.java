package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.AlarmLogMapper;
import cn.edu.just.ytc.seems.mapper.ShipMapper;
import cn.edu.just.ytc.seems.mapper.UserMapper;
import cn.edu.just.ytc.seems.mapper.UserRoleMapper;
import cn.edu.just.ytc.seems.pojo.dto.*;
import cn.edu.just.ytc.seems.pojo.entity.*;
import cn.edu.just.ytc.seems.service.AlarmLogService;
import cn.edu.just.ytc.seems.service.MessageService;
import cn.edu.just.ytc.seems.utils.SocketUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.edu.just.ytc.seems.utils.UserHolder.getUser;

/**
 * 报警日志服务实现类
 */
@Service
@Slf4j
public class AlarmLogServiceImpl implements AlarmLogService {

    @Resource
    private AlarmLogMapper alarmLogMapper;

    @Resource
    private ShipMapper shipMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private MessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAlarm(CreateAlarmLogRequest request) {
        // 参数验证
        if (request.getShipId() == null) {
            throw new ServerException("船舶ID不能为空");
        }
        if (!org.springframework.util.StringUtils.hasText(request.getAlarmType())) {
            throw new ServerException("报警类型不能为空");
        }
        if (!org.springframework.util.StringUtils.hasText(request.getAlarmLevel())) {
            throw new ServerException("报警级别不能为空");
        }
        if (!org.springframework.util.StringUtils.hasText(request.getTitle())) {
            throw new ServerException("报警标题不能为空");
        }
        if (!org.springframework.util.StringUtils.hasText(request.getContent())) {
            throw new ServerException("报警内容不能为空");
        }

        // 验证船舶存在
        ShipBaseInfo ship = shipMapper.selectById(request.getShipId());
        if (ship == null) {
            throw new ServerException("船舶不存在");
        }

        // 创建报警日志
        AlarmLog alarm = new AlarmLog();
        alarm.setShipId(request.getShipId());
        alarm.setAlarmType(AlarmLog.AlarmType.valueOf(request.getAlarmType().toUpperCase()));
        alarm.setAlarmLevel(AlarmLog.AlarmLevel.valueOf(request.getAlarmLevel().toUpperCase()));
        alarm.setTitle(request.getTitle());
        alarm.setContent(request.getContent());
        alarm.setIsHandled(false);
        alarm.setRelatedDataId(request.getRelatedDataId());

        alarmLogMapper.insert(alarm);

        log.info("创建报警日志成功: 船舶ID={}, 类型={}, 级别={}, 标题={}",
                request.getShipId(), request.getAlarmType(), request.getAlarmLevel(), request.getTitle());

        // 如果是严重级别，发送实时通知
        if (alarm.getAlarmLevel() == AlarmLog.AlarmLevel.CRITICAL ||
            alarm.getAlarmLevel() == AlarmLog.AlarmLevel.ERROR) {
            sendAlarmNotification(alarm, ship);
        }
    }

    @Override
    public QueryAlarmLogResp queryAlarmList(QueryAlarmLogRequest request) {
        User currentUser = getUser();

        // 构建分页对象
        Page<AlarmLog> page = Page.of(request.getCurrent(), request.getPageSize());

        // 构建查询条件
        QueryWrapper<AlarmLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);

        // 如果不是管理员，只能查看自己船舶的报警
        UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(currentUser.getId());
        if (userShipRole != null && !userShipRole.getRole().equals(UserShipRole.Role.ADMIN)) {
            queryWrapper.eq("ship_id", userShipRole.getShipId());
        }

        // 船舶ID筛选
        if (request.getShipId() != null) {
            queryWrapper.eq("ship_id", request.getShipId());
        }

        // 报警类型筛选
        if (org.springframework.util.StringUtils.hasText(request.getAlarmType())) {
            queryWrapper.eq("alarm_type", request.getAlarmType());
        }

        // 报警级别筛选
        if (org.springframework.util.StringUtils.hasText(request.getAlarmLevel())) {
            queryWrapper.eq("alarm_level", request.getAlarmLevel());
        }

        // 处理状态筛选
        if (request.getIsHandled() != null) {
            queryWrapper.eq("is_handled", request.getIsHandled());
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc("create_date");

        // 执行查询
        IPage<AlarmLog> alarmPage = alarmLogMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<AlarmLogVO> voList = alarmPage.getRecords().stream().map(alarm -> {
            AlarmLogVO vo = convertToVO(alarm);
            // 查询船舶名称
            ShipBaseInfo ship = shipMapper.selectById(alarm.getShipId());
            if (ship != null) {
                vo.setShipName(ship.getName());
            }
            // 查询处理人信息
            if (alarm.getHandlerId() != null) {
                User handler = userMapper.selectById(alarm.getHandlerId());
                if (handler != null) {
                    vo.setHandlerUsername(handler.getUsername());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        // 构建响应
        QueryAlarmLogResp resp = new QueryAlarmLogResp();
        resp.setList(voList);
        resp.setTotal(BigInteger.valueOf(alarmPage.getTotal()));

        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsHandled(Long alarmId) {
        if (alarmId == null) {
            throw new ServerException("报警ID不能为空");
        }

        AlarmLog alarm = alarmLogMapper.selectById(alarmId);
        if (alarm == null) {
            throw new ServerException("报警不存在");
        }

        User currentUser = getUser();

        // 验证权限
        UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(currentUser.getId());
        if (userShipRole == null || !userShipRole.getRole().equals(UserShipRole.Role.ADMIN)) {
            if (userShipRole == null || !Objects.equals(userShipRole.getShipId(), alarm.getShipId())) {
                throw new ServerException("无权操作此报警");
            }
        }

        // 更新状态
        alarm.setIsHandled(true);
        alarm.setHandleTime(LocalDateTime.now());
        alarm.setHandlerId(currentUser.getId().longValue());

        alarmLogMapper.updateById(alarm);

        log.info("报警已标记为处理: ID={}, 处理人={}", alarmId, currentUser.getUsername());
    }

    @Override
    public Integer getUnhandledCount(Long shipId) {
        if (shipId == null) {
            throw new ServerException("船舶ID不能为空");
        }
        return alarmLogMapper.countUnhandledAlarms(shipId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createLowSocAlarm(Long shipId, Double socValue, Long relatedDataId) {
        // 检查是否最近已经创建过相同的低电量报警（避免重复报警）
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<AlarmLog> recentAlarms = alarmLogMapper.selectAlarmsByTimeRange(
                shipId,
                tenMinutesAgo,
                LocalDateTime.now()
        );

        // 检查是否有未处理的低电量报警
        boolean hasUnhandledLowSocAlarm = recentAlarms.stream()
                .anyMatch(alarm ->
                        alarm.getAlarmType() == AlarmLog.AlarmType.LOW_BATTERY &&
                        !alarm.getIsHandled()
                );

        if (hasUnhandledLowSocAlarm) {
            log.info("存在未处理的低电量报警，跳过创建: 船舶ID={}, SOC={}%", shipId, socValue);
            return;
        }

        // 查询船舶信息
        ShipBaseInfo ship = shipMapper.selectById(shipId);
        if (ship == null) {
            log.error("船舶不存在，无法创建报警: 船舶ID={}", shipId);
            return;
        }

        // 创建报警日志
        AlarmLog alarm = new AlarmLog();
        alarm.setShipId(shipId);
        alarm.setAlarmType(AlarmLog.AlarmType.LOW_BATTERY);
        alarm.setAlarmLevel(AlarmLog.AlarmLevel.CRITICAL);
        alarm.setTitle(String.format("【%s】电量严重不足", ship.getName()));
        alarm.setContent(String.format(
                "船舶 %s 当前电池SOC仅为 %.1f%%，低于安全阈值15%%，请立即充电！\n" +
                        "报警时间: %s\n" +
                        "建议措施: 立即返回充电站或寻找充电设施",
                ship.getName(),
                socValue,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
        alarm.setIsHandled(false);
        alarm.setRelatedDataId(relatedDataId);

        alarmLogMapper.insert(alarm);

        log.warn("创建低电量报警: 船舶ID={}, SOC={}%, 报警ID={}", shipId, socValue, alarm.getId());

        // 发送报警通知
        sendAlarmNotification(alarm, ship);

        // 发送系统消息给所有用户
        sendAlarmMessageToAll(alarm, ship);
    }

    /**
     * 发送实时报警通知
     */
    private void sendAlarmNotification(AlarmLog alarm, ShipBaseInfo ship) {
        try {
            // 查询该船舶的所有用户
            QueryWrapper<UserShipRole> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ship_id", ship.getId());
            queryWrapper.eq("status", UserShipRole.Status.NORMAL);
            List<UserShipRole> userRoles = userRoleMapper.selectList(queryWrapper);

            // 通过Socket.IO实时推送
            for (UserShipRole userRole : userRoles) {
                SocketUtil.sendToOne(userRole.getUserId().toString(), alarm);
            }

            log.info("实时报警通知已发送: 船舶={}, 接收用户数={}", ship.getName(), userRoles.size());
        } catch (Exception e) {
            log.error("发送实时报警通知失败: {}", e.getMessage());
        }
    }

    /**
     * 发送报警消息给所有用户
     */
    private void sendAlarmMessageToAll(AlarmLog alarm, ShipBaseInfo ship) {
        try {
            SendMessageRequest messageRequest = new SendMessageRequest();
            messageRequest.setRole(UserShipRole.Role.USER); // 发送给所有角色
            messageRequest.setTitle(alarm.getTitle());
            messageRequest.setContent(alarm.getContent());
            messageRequest.setType(Message.MessageType.NOTICE);

            messageService.sendMessage(messageRequest);

            log.info("系统消息已发送给所有用户: 标题={}", alarm.getTitle());
        } catch (Exception e) {
            log.error("发送系统消息失败: {}", e.getMessage());
        }
    }

    /**
     * 转换为VO对象
     */
    private AlarmLogVO convertToVO(AlarmLog alarm) {
        AlarmLogVO vo = new AlarmLogVO();
        vo.setId(alarm.getId());
        vo.setShipId(alarm.getShipId());
        vo.setAlarmType(alarm.getAlarmType().getCode());
        vo.setAlarmTypeName(alarm.getAlarmType().getName());
        vo.setAlarmLevel(alarm.getAlarmLevel().getCode());
        vo.setAlarmLevelName(alarm.getAlarmLevel().getName());
        vo.setTitle(alarm.getTitle());
        vo.setContent(alarm.getContent());
        vo.setIsHandled(alarm.getIsHandled());
        vo.setHandleTime(alarm.getHandleTime());
        vo.setHandlerId(alarm.getHandlerId());
        vo.setRelatedDataId(alarm.getRelatedDataId());
        vo.setCreateDate(alarm.getCreateDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        vo.setUpdateDate(alarm.getUpdateDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        return vo;
    }
}
