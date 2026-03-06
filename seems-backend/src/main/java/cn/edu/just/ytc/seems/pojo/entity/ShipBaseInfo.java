package cn.edu.just.ytc.seems.pojo.entity;

import com.tangzc.mpe.autotable.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.math.BigDecimal;

@Setter
@Getter
@Schema(description = "船舶实体类")
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "ship_info")
public class ShipBaseInfo extends BaseEntity{
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "船舶名称")
    private String name;
    @Schema(description = "船舶类型(种类)")
    private String type;

    @Schema(description = "船舶型号")
    private String model;

    @Schema(description = "船舶隶属公司")
    private String ownerCompany;

    @Schema(description = "船舶制造商")
    private String manufacturer;

    @Schema(description = "船舶初始登记号")
    private String shipOriginNumber;

    @Schema(description = "MMSI号")
    private String mmsi;

    @Schema(description = "IMO号")
    private String imoNumber;

    @Schema(description = "船舶总长")
    private Float length;

    @Schema(description = "垂线间长")
    private Float lbpLength;

    @Schema(description = "设计水线长")
    private Float designLwl;

    @Schema(description = "型宽")
    private Float moldedWidth;

    @Schema(description = "船舶宽度")
    private Float width;

    @Schema(description = "型深")
    private Float moldedDepth;

    @Schema(description = "设计吃水")
    private Float designDraft;

    @Schema(description = "最大吃水")
    private Float maxDraft;

    @Schema(description = "空载押水量")
    private Float noLoadDraft;

    @Schema(description = "满载押水量")
    private Float maxLoadDraft;

    @Schema(description = "船舶吨位 （DWT）")
    private BigDecimal progress;

    @Schema(description = "船舶拖力 （ton）")
    private BigDecimal towingForce;

    @Schema(description = "船舶最大电池容量 （kwh）")
    private BigDecimal maxBatteryCapacity;

    @Schema(description = "已有航程")
    private BigDecimal sailedDistance = BigDecimal.ZERO;
}
