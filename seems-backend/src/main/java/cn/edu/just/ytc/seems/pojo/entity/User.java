package cn.edu.just.ytc.seems.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import com.tangzc.mpe.autotable.annotation.UniqueIndex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;

import static com.tangzc.autotable.annotation.mysql.MysqlTypeConstant.TEXT;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table
public class User extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    @JsonIgnore
    private String password;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "头像")
    @Column(type = TEXT)
    private String avatar = "/SEEMSAPI/api/static/avatar.png";
}
