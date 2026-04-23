package cn.edu.just.ytc.seems.pojo.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.mpe.autotable.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Table
@Schema(description = "用户船舶角色")
@Getter
@Setter
public class UserShipRole extends BaseEntity {
    @Schema(description = "ID")
    @TableField(value = "user_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger userId;
    @Schema(description = "船舶ID")
    @TableField(value = "ship_id")
    @JsonIgnore
    private BigInteger shipId;
    @Schema(description = "角色")
    @ColumnDefault("USER")
    private Role role;
    @Schema(description = "状态")
    @ColumnDefault("DISABLED")
    private Status status;

    @Getter
    public enum Status {
        NORMAL("NORMAL", "正常"),
        PENDING("PENDING", "待审核"),
        DISABLED("DISABLED", "禁用"),
        WAITJOIN("WAITJOIN", "待加入");
        @EnumValue
        @JsonValue
        private final String code;
        private final String name;
        Status(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum Role {
        // 角色值越小，权限越高(管理员 > 操作员 > 普通用户) 注意顺序！！！
        ADMIN("ADMIN", "管理员"),
        OPERATOR("OPERATOR", "操作员"),
        USER("USER", "普通用户");
        @EnumValue // 指定存储到数据库的枚举值为 code
        @JsonValue
        private final String code;
        private final String name;

        Role(String code, String name) {
            this.code = code;
            this.name = name;
        }

        /**
         * 判断当前角色是否比指定角色更高(角色值越小，权限越高)
         * @param role 指定角色
         * @return true：当前角色比指定角色更高；false：当前角色不比指定角色更高
         */
        public boolean moreThan(Role role) {
            return this.ordinal() <= role.ordinal();
        }
    }
}
