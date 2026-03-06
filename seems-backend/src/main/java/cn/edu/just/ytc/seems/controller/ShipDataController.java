package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.annotation.HasPermission;
import cn.edu.just.ytc.seems.service.ShipLogService;
import cn.edu.just.ytc.seems.service.ShipService;
import cn.edu.just.ytc.seems.utils.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/ship-data")
public class ShipDataController extends BaseController{
    @Resource
    private ShipLogService shipLogService;

    @Resource
    private ShipService shipService;

    @HasPermission
    @GetMapping("/get-ship-info")
    public R info(){
        return R.ok(new HashMap<>(){{
            put("shipInfo", shipService.getShipInfo());
        }});
    }


    @HasPermission
    @GetMapping("/get-nearly-nav-data")
    public R getNearlyNavData(){
        return R.ok(new HashMap<>(){{
            put("navData", shipLogService.getNearlyShipGPSLog());
        }});
    }

    @HasPermission
    @GetMapping("/get-soc-data-by-time-range")
    public R getSocDataByTimeRange(@RequestParam("start") String startDate, @RequestParam("end") String endDate){
        return R.ok(new HashMap<>(){{
            put("socData", shipLogService.getShipSocByDateTimeBetween(
                    LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));
        }});
    }

    @HasPermission
    @GetMapping("/get-work-condition-duration-by-time-range")
    public R getWorkConditionDurationByTimeRange(@RequestParam("start") String startDate, @RequestParam("end") String endDate){
        return R.ok(new HashMap<>(){{
            put("workConditionDuration", shipLogService.getShipWorkDuration(
                    LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));
        }});
    }

    @HasPermission
    @GetMapping("/get-nav-data-by-time-range")
    public R getNavDataByDate(@RequestParam("start") String startDate, @RequestParam("end") String endDate){
        return R.ok(new HashMap<>(){{
            put("navData",
                    shipLogService.getShipNavByDateTimeBetween(
                            LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
            );
        }});
    }
}
