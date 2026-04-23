package cn.edu.just.ytc.seems.pojo.entity;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tangzc.autotable.annotation.ColumnDefault;
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
@Table(value = "gps_log")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "GPS定位日志")
public class GPSLog extends BaseEntity{
    @Schema(description = "船舶ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger shipId;
    @Schema(description = "航段ID")
    private BigInteger voyageId;
    @Schema(description = "经度")
    @ColumnType(decimalLength = 8)
    private Double longitude;
    @ColumnType(decimalLength = 8)
    @Schema(description = "纬度")
    private Double latitude;
    @ColumnType(decimalLength = 8)
    @Schema(description = "海拔")
    private Double altitude;
    @ColumnType(decimalLength = 8)
    @Schema(description = "航速")
    private Double speed;
    @Schema(description = "航向")
    @ColumnType(decimalLength = 8)
    private Double direction;
    @Schema(description = "时间")
    @Index
    private Date time;

    @Index
    @Schema(description = "链接ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger linkId;

    @Index
    @Schema(description = "工作状态")
    @ColumnDefault("STOPPING_AT_PORT")
    private WorkStatus workStatus;

    @Getter
    public enum WorkStatus {
        // 航行，拖拽，停港
        HOVERING("HOVERING","航行"),
        DRAGGING("DRAGGING","拖拽/顶推 "),
        STOPPING_AT_PORT("STOPPING_AT_PORT","停港"),
        UNKNOWN("UNKNOWN","未知"),
        CHARGING("CHARGING","充电中"),
        IDLE("IDLE", "待命");
        @EnumValue
        @JsonValue
        private final String code;

        private final String value;
        WorkStatus(String code, String value) {
            this.code = code;
            this.value = value;
        }

    }
}
