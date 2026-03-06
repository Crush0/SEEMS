package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.dto.ShipInfoForm;
import cn.edu.just.ytc.seems.pojo.entity.ShipBaseInfo;

public interface ShipService extends IBaseUserInfo {
    ShipBaseInfo getShipInfo();
    void createNewShip(ShipInfoForm shipInfoForm);

    void applyForShip(String shipNumber);

    void updateShipInfo(ShipInfoForm shipInfoForm);
}
