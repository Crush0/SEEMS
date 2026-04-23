package cn.edu.just.ytc.seems.pojo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 接收人类型枚举
 */
@Getter
@Schema(description = "接收人类型")
public enum ReceiverType {

    ROLE("role", "角色组"),
    USER("user", "具体用户");

    @EnumValue
    @Schema(description = "接收人类型编码")
    private final String code;

    @Schema(description = "接收人类型名称")
    private final String name;

    ReceiverType(String code, String name) {
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
