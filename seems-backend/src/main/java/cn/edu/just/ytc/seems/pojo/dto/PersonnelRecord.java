package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonnelRecord {
    private BigInteger id;
    private String name;
    private String email;
    private String phone;
    private UserShipRole.Role role;
    private UserShipRole.Status status;
}
