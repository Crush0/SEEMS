package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.entity.Message.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询消息列表请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "查询消息列表请求")
public class QueryMessageListRequest {

    @Schema(description = "消息类型（可选）")
    private MessageType type;

    @Schema(description = "消息状态：0-未读，1-已读（可选）")
    private Integer status;

    @Schema(description = "当前页码", required = true)
    private Integer current;

    @Schema(description = "每页大小", required = true)
    private Integer pageSize;
}
