package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.entity.BatteryLog;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 电池SOC数据查询服务测试类
 * 测试直接返回原始真实数据的功能
 */
@SpringBootTest
@ActiveProfiles("test")
public class BatterySOCServiceTest {

    @Resource
    private BatterySOCService batterySOCService;

    @Test
    public void testGetProcessedSOCData() {
        // 测试获取指定时间范围内的SOC真实数据
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();

        // 获取真实的SOC数据（不进行任何插值或拟合处理）
        List<BatteryLog> realData = batterySOCService.getProcessedSOCData(
                BigInteger.ONE, startTime, endTime);

        // 验证返回结果不为null
        assertNotNull(realData);

        // 验证数据是原始真实值，未被修改
        // 如果有数据，验证SOC值的真实性（范围在0-100之间）
        for (BatteryLog log : realData) {
            if (log.getSoc() != null) {
                BigDecimal soc = log.getSoc();
                assertTrue(soc.compareTo(BigDecimal.ZERO) >= 0,
                        "SOC值应该大于等于0");
                assertTrue(soc.compareTo(BigDecimal.valueOf(100)) <= 0,
                        "SOC值应该小于等于100");
            }
        }
    }

    @Test
    public void testGetProcessedSOCDataWithEmptyTimeRange() {
        // 测试查询不存在数据的时间范围
        LocalDateTime futureStartTime = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEndTime = LocalDateTime.now().plusDays(2);

        // 查询未来时间的数据（应该为空）
        List<BatteryLog> emptyData = batterySOCService.getProcessedSOCData(
                BigInteger.ONE, futureStartTime, futureEndTime);

        // 验证返回空列表而不是null
        assertNotNull(emptyData);
        assertTrue(emptyData.isEmpty(), "未来时间范围应该返回空列表");
    }

    @Test
    public void testGetProcessedSOCDataWithInvalidTimeRange() {
        // 测试无效的时间范围（开始时间晚于结束时间）
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().minusHours(1);

        // 查询无效时间范围
        List<BatteryLog> invalidData = batterySOCService.getProcessedSOCData(
                BigInteger.ONE, startTime, endTime);

        // 验证返回空列表而不是null
        assertNotNull(invalidData);
        assertTrue(invalidData.isEmpty(), "无效时间范围应该返回空列表");
    }
}