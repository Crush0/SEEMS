package cn.edu.just.ytc.seems.pojo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 报警触发时机枚举
 */
@Getter
@Schema(description = "报警触发时机")
public enum TriggerTiming {

    IMMEDIATE("immediate", "立即触发"),
    DURATION("duration", "持续一段时间后触发");

    @EnumValue
    @Schema(description = "触发时机编码")
    private final String code;

    @Schema(description = "触发时机名称")
    private final String name;

    TriggerTiming(String code, String name) {
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
