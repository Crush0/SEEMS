package cn.edu.just.ytc.seems;

import cn.edu.just.ytc.seems.service.ReportService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SeemsApplicationTests {
    @Resource
    ReportService reportService;

    @Test
    void contextLoads() {
        reportService.generateDailyReport();
    }
}
