package cn.edu.just.ytc.seems.pojo.entity;

import cn.edu.just.ytc.seems.pojo.entity.UserShipRole.Role;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.mpe.autotable.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

/**
 * 消息实体类
 */
@Table
@Schema(description = "消息")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message extends BaseEntity {

    @Schema(description = "发送者用户ID")
    @TableField(value = "sender_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger senderId;

    @Schema(description = "接收者用户ID（为空表示群发）")
    @TableField(value = "receiver_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger receiverId;

    @Schema(description = "目标角色组")
    private Role role;

    @Schema(description = "消息标题")
    @TableField(value = "title")
    private String title;

    @Schema(description = "消息内容")
    @TableField(value = "content")
    private String content;

    @Schema(description = "消息类型")
    @TableField(value = "type")
    @ColumnDefault("'message'")
    private MessageType type;

    @Schema(description = "状态：0-未读，1-已读")
    @TableField(value = "status")
    @ColumnDefault("0")
    private Integer status;

    /**
     * 消息类型枚举
     */
    @Getter
    public enum MessageType {
        MESSAGE("message", "消息"),
        NOTICE("notice", "通知"),
        TODO("todo", "待办");

        @EnumValue
        @JsonValue
        private final String code;
        private final String name;

        MessageType(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
}
