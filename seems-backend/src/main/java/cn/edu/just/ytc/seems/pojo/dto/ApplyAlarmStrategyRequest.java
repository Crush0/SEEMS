package cn.edu.just.ytc.seems.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 应用报警策略请求（Python脚本调用）
 */
@Data
@Schema(description = "应用报警策略请求")
public class ApplyAlarmStrategyRequest {

    @Schema(description = "船舶ID", required = true)
    @NotNull(message = "船舶ID不能为空")
    private Long shipId;

    @Schema(description = "报警类型", required = true)
    @NotBlank(message = "报警类型不能为空")
    private String alarmType;

    @Schema(description = "当前值", required = true)
    @NotNull(message = "当前值不能为空")
    private Double value;

    @Schema(description = "关联数据ID")
    private Long relatedDataId;

    @Schema(description = "模板变量（如：battery_position, suggestion等）")
    private Map<String, Object> templateVariables;
}
