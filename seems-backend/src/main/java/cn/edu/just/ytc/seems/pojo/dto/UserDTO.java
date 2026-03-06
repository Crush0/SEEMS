package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.entity.User;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String email;
    private String phone;
    private UserShipRole.Role role;
    private UserShipRole.Status status;
    private String avatar;


    public static UserDTO of(User user, UserShipRole.Role role, UserShipRole.Status status) {
        return new UserDTO(user.getUsername(), user.getEmail(), user.getPhone(),role, status, user.getAvatar());
    }
}
