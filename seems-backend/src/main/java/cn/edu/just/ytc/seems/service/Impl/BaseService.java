package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.ShipMapper;
import cn.edu.just.ytc.seems.mapper.UserRoleMapper;
import cn.edu.just.ytc.seems.pojo.entity.ShipBaseInfo;
import cn.edu.just.ytc.seems.pojo.entity.User;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

import static cn.edu.just.ytc.seems.utils.UserHolder.getUser;

@Service
public class BaseService {
    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private ShipMapper shipMapper;

    public UserShipRole getUserShipRole() {
        User user = getUser();

        UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(user.getId());
        if (userShipRole == null) {
            throw new ServerException("用户没有船舶权限");
        }
        return userShipRole;
    }

    public ShipBaseInfo getShipBaseInfo(BigInteger shipId) {
        if (shipId == null) {
            return shipMapper.selectById(getUserShipRole().getShipId());
        } else {
            return shipMapper.selectById(shipId);
        }
    }
}
