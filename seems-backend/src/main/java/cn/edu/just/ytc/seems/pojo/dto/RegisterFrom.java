package cn.edu.just.ytc.seems.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterFrom {
    private String username;
    private String password;
    private String repeatPassword;
}
