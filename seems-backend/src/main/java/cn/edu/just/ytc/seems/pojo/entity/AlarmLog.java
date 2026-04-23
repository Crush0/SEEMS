package cn.edu.just.ytc.seems.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报警日志实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("alarm_log")
@Schema(description = "报警日志")
public class AlarmLog extends BaseEntity {

    @Schema(description = "船舶ID")
    private Long shipId;

    @Schema(description = "报警类型")
    private AlarmType alarmType;

    @Schema(description = "报警级别")
    private AlarmLevel alarmLevel;

    @Schema(description = "报警标题")
    private String title;

    @Schema(description = "报警内容")
    private String content;

    @Schema(description = "是否已处理")
    private Boolean isHandled;

    @Schema(description = "处理时间")
    private java.time.LocalDateTime handleTime;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "关联数据ID（如GPS日志ID）")
    private Long relatedDataId;

    /**
     * 报警类型枚举
     */
    public enum AlarmType {
        LOW_BATTERY("low_battery", "低电量报警"),
        HIGH_TEMPERATURE("high_temperature", "高温报警"),
        GPS_LOST("gps_lost", "GPS信号丢失"),
        PROPULSION_FAILURE("propulsion_failure", "推进器故障"),
        WEATHER_WARNING("weather_warning", "天气预警"),
        SYSTEM_ERROR("system_error", "系统错误");

        @com.baomidou.mybatisplus.annotation.EnumValue
        @io.swagger.v3.oas.annotations.media.Schema(description = "报警类型编码")
        private final String code;

        @io.swagger.v3.oas.annotations.media.Schema(description = "报警类型名称")
        private final String name;

        AlarmType(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 报警级别枚举
     */
    public enum AlarmLevel {
        INFO("info", "信息", 1),
        WARNING("warning", "警告", 2),
        ERROR("error", "错误", 3),
        CRITICAL("critical", "严重", 4);

        @com.baomidou.mybatisplus.annotation.EnumValue
        @io.swagger.v3.oas.annotations.media.Schema(description = "报警级别编码")
        private final String code;

        @io.swagger.v3.oas.annotations.media.Schema(description = "报警级别名称")
        private final String name;

        @io.swagger.v3.oas.annotations.media.Schema(description = "优先级")
        private final int priority;

        AlarmLevel(String code, String name, int priority) {
            this.code = code;
            this.name = name;
            this.priority = priority;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }
    }
}
