package cn.edu.just.ytc.seems.pojo.dto;

import lombok.Data;

@Data
public class LoginForm {
    private String username;
    private String password;
    private boolean rememberMe;
}
