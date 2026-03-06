package cn.edu.just.ytc.seems.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.mysql.MysqlTypeConstant;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Setter
@Getter
@Table(value = "analyze_data")
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzeData extends BaseEntity{
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger shipId;

    @Schema(description = "日电能消耗")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal dailyEnergyConsumption;

    @Schema(description = "日碳排放量")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal dailyCarbonEmission;

    @Schema(description = "单位小时电能消耗")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal preHourEnergyConsumption;

    @Schema(description = "单位运输功电能消耗")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal preUnitWorkEnergyConsumption;

    @Schema(description = "单位距离电能消耗")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal preDistanceEnergyConsumption;

    @Schema(description = "航行时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private Long dailySailingDuration;

    @Schema(description = "航行距离")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal dailySailingDistance;

    @Schema(description = "总航程")
//    长度24， 小数点2位
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal sailingDistance = new BigDecimal("0.0");

    @Schema(description = "分析时间")
    @Index
    private Date analyzeTime;

}
