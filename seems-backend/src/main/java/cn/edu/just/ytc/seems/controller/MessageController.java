package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.annotation.HasPermission;
import cn.edu.just.ytc.seems.pojo.dto.QueryMessageListRequest;
import cn.edu.just.ytc.seems.pojo.dto.SendMessageRequest;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.MessageService;
import cn.edu.just.ytc.seems.utils.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/message")
public class MessageController extends BaseController {

    @Resource
    private MessageService messageService;

    /**
     * 发送消息
     * 需要ADMIN权限
     */
    @HasPermission(role = UserShipRole.Role.ADMIN)
    @PostMapping("/send")
    public R sendMessage(@RequestBody SendMessageRequest request) {
        messageService.sendMessage(request);
        return R.ok("消息发送成功");
    }

    /**
     * 查询消息列表
     */
    @PostMapping("/list")
    public R queryMessageList(@RequestBody QueryMessageListRequest request) {
        return R.ok(messageService.queryMessageList(request));
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read")
    public R markMessagesAsRead(@RequestBody List<Long> messageIds) {
        messageService.markMessagesAsRead(messageIds);
        return R.ok("标记成功");
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public R getUnreadCount() {
        Integer count = messageService.getUnreadCount();
        return R.ok(count);
    }
}
