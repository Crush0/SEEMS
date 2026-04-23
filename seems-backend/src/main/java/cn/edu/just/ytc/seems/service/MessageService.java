package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.dto.MessageVO;
import cn.edu.just.ytc.seems.pojo.dto.QueryMessageListRequest;
import cn.edu.just.ytc.seems.pojo.dto.QueryMessageListResp;
import cn.edu.just.ytc.seems.pojo.dto.SendMessageRequest;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 发送消息
     *
     * @param request 发送消息请求
     */
    void sendMessage(SendMessageRequest request);

    /**
     * 查询消息列表
     *
     * @param request 查询请求
     * @return 消息分页结果
     */
    QueryMessageListResp queryMessageList(QueryMessageListRequest request);

    /**
     * 标记消息为已读
     *
     * @param messageIds 消息ID列表
     */
    void markMessagesAsRead(List<Long> messageIds);

    /**
     * 获取未读消息数量
     *
     * @return 未读消息数量
     */
    Integer getUnreadCount();
}
