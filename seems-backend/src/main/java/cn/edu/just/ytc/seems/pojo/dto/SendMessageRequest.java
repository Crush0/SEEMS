package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.entity.Message.MessageType;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * 发送消息请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "发送消息请求")
public class SendMessageRequest {

    @Schema(description = "接收者用户ID（与role二选一）")
    private BigInteger receiverId;

    @Schema(description = "目标角色组（与receiverId二选一）")
    private Role role;

    @Schema(description = "消息标题", required = true)
    private String title;

    @Schema(description = "消息内容", required = true)
    private String content;

    @Schema(description = "消息类型")
    private MessageType type;
}
