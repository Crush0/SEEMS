package cn.edu.just.ytc.seems.pojo.dto;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.Getter;

import java.math.BigInteger;
import java.util.Date;

@Data
public class ReportParams {
    private ReportType reportType;
    private Date startDate;
    private Date endDate;
    private BigInteger shipId;

    @Getter
    public enum ReportType {
        DAILY("daily"),
        WEEKLY("weekly"),
        MONTHLY("monthly"),
        YEARLY("yearly"),
        QUARTERLY("quarterly");

        @JsonValue
        @EnumValue
        private final String value;

        ReportType(String value) {
            this.value = value;
        }

    }
}
