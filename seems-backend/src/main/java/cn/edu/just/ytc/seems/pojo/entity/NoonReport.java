package cn.edu.just.ytc.seems.pojo.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.mpe.autotable.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Date;

@Setter
@Getter
@Table(value = "noon_report")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "船舶正午报告")
public class NoonReport extends BaseEntity{
    @Schema(description = "船舶ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger shipId;

    @Schema(description = "生成时间")
    @Index
    private Date time;

    @Schema(description = "理论航速")
    @ColumnType(decimalLength = 8)
    private Double theorySpeed;

    @Schema(description = "实际航速")
    @ColumnType(decimalLength = 8)
    private Double actualSpeed;

    @Schema(description = "航向")
    @ColumnType(decimalLength = 8)
    private Double direction;

    @Schema(description = "经度")
    @ColumnType(decimalLength = 8)
    private Double longitude;
    @ColumnType(decimalLength = 8)
    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "风向")
    private Double windDirection;
    @Schema(description = "风速")
    private Double windSpeed;

    @Schema(description = "实际航程")
    @ColumnType(length = 24, decimalLength = 2)
    private Double actualVoyage;

    @Schema(description = "航程累计")
    @ColumnType(length = 24, decimalLength = 2)
    private Double totalVoyage;

    @Schema(description = "滑失率")
    private Double slidingRate;
}
