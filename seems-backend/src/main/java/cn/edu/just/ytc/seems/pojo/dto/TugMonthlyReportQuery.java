package cn.edu.just.ytc.seems.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 拖轮月度报表查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "拖轮月度报表查询参数")
public class TugMonthlyReportQuery {

    @Schema(description = "船舶ID")
    private Long shipId;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;
}
