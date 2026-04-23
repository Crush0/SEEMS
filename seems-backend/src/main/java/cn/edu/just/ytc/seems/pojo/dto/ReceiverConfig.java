package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.enums.ReceiverType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 接收人配置
 */
@Data
@Schema(description = "接收人配置")
public class ReceiverConfig {

    @Schema(description = "接收人类型", required = true)
    private ReceiverType receiverType;

    @Schema(description = "接收人ID（USER类型时使用）")
    private Long receiverId;

    @Schema(description = "接收人角色（ROLE类型时使用）")
    private String receiverRole;
}
