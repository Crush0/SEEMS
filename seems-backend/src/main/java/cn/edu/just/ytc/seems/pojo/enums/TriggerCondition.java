package cn.edu.just.ytc.seems.pojo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 报警触发条件枚举
 */
@Getter
@Schema(description = "报警触发条件")
public enum TriggerCondition {

    LESS_THAN("less_than", "低于"),
    GREATER_THAN("greater_than", "高于"),
    BETWEEN("between", "区间"),
    EQUAL("equal", "等于");

    @EnumValue
    @Schema(description = "触发条件编码")
    private final String code;

    @Schema(description = "触发条件名称")
    private final String name;

    TriggerCondition(String code, String name) {
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
