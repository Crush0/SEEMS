package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.dto.PersonnelRecord;
import cn.edu.just.ytc.seems.pojo.dto.QueryPersonnelListRequest;
import cn.edu.just.ytc.seems.pojo.dto.QueryPersonnelListResp;


public interface PersonnelService extends IBaseUserInfo{
    QueryPersonnelListResp queryUserRole(QueryPersonnelListRequest queryRequest);

    void savePersonnel(PersonnelRecord personnelRecord);
}
