package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.ShipMapper;
import cn.edu.just.ytc.seems.mapper.UserRoleMapper;
import cn.edu.just.ytc.seems.pojo.dto.ShipInfoForm;
import cn.edu.just.ytc.seems.pojo.entity.ShipBaseInfo;
import cn.edu.just.ytc.seems.pojo.entity.User;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.ShipService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class ShipServiceImpl extends BaseService implements ShipService {
    @Resource
    private ShipMapper shipMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public ShipBaseInfo getShipInfo() {
        UserShipRole userShipRole = getUserShipRole();
        return shipMapper.selectById(userShipRole.getShipId());
    }

    @Override
    public void createNewShip(ShipInfoForm shipInfoForm) {
        ShipBaseInfo shipBaseInfo = shipInfoForm.toShipBaseInfo();
        shipMapper.insert(shipBaseInfo);
        User user = getUser();
        UserShipRole userShipRole = new UserShipRole();
        userShipRole.setUserId(user.getId());
        userShipRole.setShipId(shipBaseInfo.getId());
        userShipRole.setRole(UserShipRole.Role.ADMIN);
        userShipRole.setStatus(UserShipRole.Status.NORMAL);
        userRoleMapper.insert(userShipRole);
    }

    @Override
    public void applyForShip(String shipNumber) {
        User user = getUser();
        UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(user.getId());
        if (userShipRole!= null) {
            throw new ServerException("用户已有船舶权限");
        } else {
            UserShipRole newUserShipRole = new UserShipRole();
            newUserShipRole.setStatus(UserShipRole.Status.PENDING);
            newUserShipRole.setUserId(user.getId());
            ShipBaseInfo shipBaseInfo = shipMapper.selectByShipNumber(shipNumber);
            if (shipBaseInfo == null) {
                throw new ServerException("船舶不存在");
            }
            newUserShipRole.setShipId(shipBaseInfo.getId());
            newUserShipRole.setRole(UserShipRole.Role.USER);
            userRoleMapper.insert(newUserShipRole);
        }
    }

    @Override
    public void updateShipInfo(ShipInfoForm shipInfoForm) {
        UserShipRole userShipRole = getUserShipRole();
        BigInteger shipId = userShipRole.getShipId();
        ShipBaseInfo newShipBaseInfo = shipInfoForm.toShipBaseInfo();
        newShipBaseInfo.setId(shipId);
        shipMapper.updateById(newShipBaseInfo);
    }
}
