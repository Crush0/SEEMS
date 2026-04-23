package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.entity.Message.MessageType;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

/**
 * 消息视图对象VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "消息视图对象")
public class MessageVO {

    @Schema(description = "消息ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger id;

    @Schema(description = "发送者用户ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger senderId;

    @Schema(description = "发送者用户名")
    private String senderUsername;

    @Schema(description = "发送者头像")
    private String senderAvatar;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型")
    private MessageType type;

    @Schema(description = "消息状态：0-未读，1-已读")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createDate;

    @Schema(description = "是否为角色组消息")
    private Boolean isRoleMessage;

    @Schema(description = "目标角色组")
    private Role role;
}
