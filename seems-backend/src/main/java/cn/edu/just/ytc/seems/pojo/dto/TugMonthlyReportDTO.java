package cn.edu.just.ytc.seems.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 拖轮月度报表DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "拖轮月度报表")
public class TugMonthlyReportDTO {

    @Schema(description = "船舶ID")
    private Long shipId;

    @Schema(description = "船舶名称")
    private String shipName;

    @Schema(description = "报表年份")
    private Integer year;

    @Schema(description = "报表月份")
    private Integer month;

    @Schema(description = "总作业艘次")
    private Integer totalOperations;

    @Schema(description = "总作业小时")
    private Double totalOperationHours;

    @Schema(description = "单次平均作业时长(小时)")
    private Double averageOperationDuration;

    @Schema(description = "累计航行里程(海里)")
    private Double totalVoyageDistance;

    @Schema(description = "月度总耗电量(kWh)")
    private Double totalPowerConsumption;

    @Schema(description = "单船平均能耗(kWh/艘次)")
    private Double averagePowerConsumptionPerOperation;

    @Schema(description = "每海里能耗(kWh/海里)")
    private Double powerConsumptionPerNauticalMile;

    @Schema(description = "每日能耗数据列表")
    private List<DailyEnergyConsumption> dailyEnergyConsumptions;

    /**
     * 每日能耗数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyEnergyConsumption {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "能耗(kWh)")
        private Double energyConsumption;

        @Schema(description = "作业艘次")
        private Integer operations;

        @Schema(description = "航行里程(海里)")
        private Double voyageDistance;
    }
}
