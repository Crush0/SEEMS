package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.MessageMapper;
import cn.edu.just.ytc.seems.mapper.UserMapper;
import cn.edu.just.ytc.seems.mapper.UserRoleMapper;
import cn.edu.just.ytc.seems.pojo.dto.MessageVO;
import cn.edu.just.ytc.seems.pojo.dto.QueryMessageListRequest;
import cn.edu.just.ytc.seems.pojo.dto.QueryMessageListResp;
import cn.edu.just.ytc.seems.pojo.dto.SendMessageRequest;
import cn.edu.just.ytc.seems.pojo.entity.Message;
import cn.edu.just.ytc.seems.pojo.entity.User;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.MessageService;
import cn.edu.just.ytc.seems.utils.SocketUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.edu.just.ytc.seems.utils.UserHolder.getUser;

/**
 * 消息服务实现类
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(SendMessageRequest request) {
        // 参数验证：receiverId和role必须有且仅有一个
        if (request.getReceiverId() == null && request.getRole() == null) {
            throw new ServerException("接收者ID和角色组必须指定其中一个");
        }
        if (request.getReceiverId() != null && request.getRole() != null) {
            throw new ServerException("接收者ID和角色组不能同时指定");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw new ServerException("消息标题不能为空");
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new ServerException("消息内容不能为空");
        }

        User sender = getUser();
        BigInteger senderId = sender.getId();

        // 发送给单个用户
        if (request.getReceiverId() != null) {
            sendToSingleUser(senderId, request);
        }
        // 发送给角色组
        else if (request.getRole() != null) {
            sendToRoleGroup(senderId, request);
        }
    }

    /**
     * 发送消息给单个用户
     */
    private void sendToSingleUser(BigInteger senderId, SendMessageRequest request) {
        // 验证接收者存在
        User receiver = userMapper.selectById(request.getReceiverId());
        if (receiver == null) {
            throw new ServerException("接收者不存在");
        }

        // 创建消息记录
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setTitle(request.getTitle());
        message.setContent(request.getContent());
        message.setType(request.getType() != null ? request.getType() : Message.MessageType.MESSAGE);
        message.setStatus(0); // 未读

        messageMapper.insert(message);

        // 实时推送
        SocketUtil.sendToOne(request.getReceiverId().toString(), message);
        log.info("消息已发送给用户 {}，标题：{}", receiver.getUsername(), request.getTitle());
    }

    /**
     * 发送消息给角色组
     */
    private void sendToRoleGroup(BigInteger senderId, SendMessageRequest request) {
        // 获取发送者的船舶信息
        UserShipRole senderRole = userRoleMapper.getUserShipRoleByUserId(senderId);
        if (senderRole == null) {
            throw new ServerException("发送者没有船舶权限");
        }

        // 查询同船舶下指定角色的所有用户
        QueryWrapper<UserShipRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ship_id", senderRole.getShipId());
        queryWrapper.eq("role", request.getRole());
        queryWrapper.eq("status", UserShipRole.Status.NORMAL);
        List<UserShipRole> userRoles = userRoleMapper.selectList(queryWrapper);

        if (userRoles.isEmpty()) {
            throw new ServerException("没有找到符合条件的用户");
        }

        // 批量创建消息记录
        List<Message> messages = new ArrayList<>();
        for (UserShipRole userRole : userRoles) {
            Message message = new Message();
            message.setSenderId(senderId);
            message.setReceiverId(userRole.getUserId());
            message.setRole(request.getRole());
            message.setTitle(request.getTitle());
            message.setContent(request.getContent());
            message.setType(request.getType() != null ? request.getType() : Message.MessageType.MESSAGE);
            message.setStatus(0); // 未读
            messages.add(message);
        }

        // 批量插入
        for (Message message : messages) {
            messageMapper.insert(message);
        }

        // 循环推送
        for (Message message : messages) {
            SocketUtil.sendToOne(message.getReceiverId().toString(), message);
        }

        log.info("消息已发送给角色组 {} 的 {} 个用户，标题：{}",
                request.getRole(), userRoles.size(), request.getTitle());
    }

    @Override
    public QueryMessageListResp queryMessageList(QueryMessageListRequest request) {
        User user = getUser();
        BigInteger receiverId = user.getId();

        // 使用 MyBatis Plus 的分页查询
        Page<Message> page = Page.of(request.getCurrent(), request.getPageSize());

        // 构建查询条件
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", receiverId);

        // 添加类型筛选
        if (request.getType() != null) {
            queryWrapper.eq("type", request.getType());
        }

        // 添加状态筛选
        if (request.getStatus() != null) {
            queryWrapper.eq("status", request.getStatus());
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc("create_date");

        // 执行分页查询
        IPage<Message> messagePage = messageMapper.selectPage(page, queryWrapper);

        // 转换为 MessageVO（包含发送者信息）
        List<MessageVO> messageVOList = messagePage.getRecords().stream().map(message -> {
            MessageVO vo = new MessageVO();
            vo.setId(message.getId());
            vo.setSenderId(message.getSenderId());
            vo.setTitle(message.getTitle());
            vo.setContent(message.getContent());
            vo.setType(message.getType());
            vo.setStatus(message.getStatus());
            vo.setCreateDate(message.getCreateDate());
            vo.setRole(message.getRole());
            vo.setIsRoleMessage(message.getReceiverId() == null);

            // 查询发送者信息
            User sender = userMapper.selectById(message.getSenderId());
            if (sender != null) {
                vo.setSenderUsername(sender.getUsername());
                vo.setSenderAvatar(sender.getAvatar());
            }

            return vo;
        }).collect(Collectors.toList());

        // 构建响应对象
        QueryMessageListResp resp = new QueryMessageListResp();
        resp.setList(messageVOList);
        resp.setTotal(BigInteger.valueOf(messagePage.getTotal()));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markMessagesAsRead(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            throw new ServerException("消息ID列表不能为空");
        }

        User user = getUser();
        BigInteger currentUserId = user.getId();

        // 验证所有权并更新状态
        for (Long messageId : messageIds) {
            Message message = messageMapper.selectById(messageId);
            if (message == null) {
                throw new ServerException("消息不存在，ID：" + messageId);
            }

            // 验证消息所有权
            if (!Objects.equals(message.getReceiverId(), currentUserId)) {
                throw new ServerException("无权操作此消息");
            }

            // 更新状态为已读
            message.setStatus(1);
            messageMapper.updateById(message);
        }

        log.info("用户 {} 标记了 {} 条消息为已读", user.getUsername(), messageIds.size());
    }

    @Override
    public Integer getUnreadCount() {
        User user = getUser();
        BigInteger receiverId = user.getId();
        return messageMapper.countUnreadMessages(receiverId);
    }
}
