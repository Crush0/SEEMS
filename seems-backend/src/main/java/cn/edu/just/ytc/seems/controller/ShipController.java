package cn.edu.just.ytc.seems.controller;


import cn.edu.just.ytc.seems.pojo.dto.ShipInfoForm;
import cn.edu.just.ytc.seems.service.ShipService;
import cn.edu.just.ytc.seems.utils.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ship")
public class ShipController extends BaseController{
    @Resource
    private ShipService shipService;

    @PostMapping("/create")
    public R create(@RequestBody ShipInfoForm shipInfoForm) {
        shipService.createNewShip(shipInfoForm);
        return R.ok();
    }

    @PostMapping("/update")
    public R update(@RequestBody ShipInfoForm shipInfoForm) {
        shipService.updateShipInfo(shipInfoForm);
        return R.ok();
    }

    @GetMapping("/info")
    public R info() {
        return R.ok(shipService.getShipInfo());
    }

    @PostMapping("/apply")
    public R apply(@RequestBody Map<String, Object> params) {
        String shipNumber = (String) params.get("shipNumber");
        assert shipNumber!= null;
        shipService.applyForShip(shipNumber);
        return R.ok();
    }
}
