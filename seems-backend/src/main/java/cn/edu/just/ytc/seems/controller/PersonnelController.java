package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.annotation.HasPermission;
import cn.edu.just.ytc.seems.pojo.dto.PersonnelRecord;
import cn.edu.just.ytc.seems.pojo.dto.QueryPersonnelListRequest;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.PersonnelService;
import cn.edu.just.ytc.seems.utils.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/personnel")
public class PersonnelController extends BaseController {

    @Resource
    private PersonnelService personnelService;

    @HasPermission(role = UserShipRole.Role.ADMIN)
    @RequestMapping("/query")
    public R queryPersonnelList(@RequestBody QueryPersonnelListRequest request) {
        return R.ok(personnelService.queryUserRole(request));
    }

    @HasPermission(role = UserShipRole.Role.ADMIN)
    @RequestMapping("/save")
    public R savePersonnel(@RequestBody PersonnelRecord personnelRecord) {
        personnelService.savePersonnel(personnelRecord);
        return R.ok();
    }
}
