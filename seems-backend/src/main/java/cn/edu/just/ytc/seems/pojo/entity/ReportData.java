package cn.edu.just.ytc.seems.pojo.entity;

import cn.edu.just.ytc.seems.analyze.SpeedStats;
import cn.edu.just.ytc.seems.pojo.dto.ReportParams;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
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
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static com.tangzc.autotable.annotation.mysql.MysqlTypeConstant.TEXT;

@Setter
@Getter
@Schema(description = "报告信息")
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "report_data")
@TableName(autoResultMap = true)
@Accessors(chain = true)
public class ReportData extends BaseEntity {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger shipId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal energyConsumption;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal sailDuration;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal sailDistance;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal carbonEmission;

    @Column(type = TEXT)
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private SpeedStats hoveringSpeedStats;
    @Column(type = TEXT)
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private SpeedStats draggingSpeedStats;

    @TableField(typeHandler = FastjsonTypeHandler.class)
    @Column(type = TEXT)
    private AnalyzeData analyzeData;

    @Column(type = MysqlTypeConstant.DECIMAL, length = 24, decimalLength = 2)
    private BigDecimal CII;

    @Index
    private Date time;

    private ReportParams.ReportType reportType;


}

