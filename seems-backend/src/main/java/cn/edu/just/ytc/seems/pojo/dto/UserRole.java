package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import lombok.Data;

@Data
public class UserRole {
    private String id;
    private String name;
    private String email;
    private String phone;
    private UserShipRole.Role role;
    private UserShipRole.Status status;
}
