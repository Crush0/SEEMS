package cn.edu.just.ytc.seems.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tangzc.mpe.autotable.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Date;

/**
 * @description: 航次实体类
 */
@Setter
@Getter
@Table(value = "voyage_log")
@AllArgsConstructor
@NoArgsConstructor
public class VoyageLog extends BaseEntity {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger shipId;
    @Schema(description = "出发地")
    private String departure;
    @Schema(description = "目的地")
    private String arrival;
    @Schema(description = "开始时间")
    private Date startTime;
    @Schema(description = "结束时间")
    private Date endTime;

    @Schema(description = "航次/航段里程")
    private Double voyageDistance;
    @Schema(description = "航次/航段时间")
    private Long sailingTime;

    @Schema(description = "航次/航段电能消耗")
    private Double voyagePowerConsumption;
}
