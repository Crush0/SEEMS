package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.entity.BatteryLog;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 电池SOC异常值检测与修复服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
public class BatterySOCServiceTest {

    @Resource
    private BatterySOCService batterySOCService;

    @Test
    public void testDetectAnomalies() {
        // 创建测试数据
        List<BatteryLog> testData = createTestBatteryLogs();
        
        // 检测异常值
        List<Integer> anomalies = batterySOCService.detectAnomalies(testData);
        
        // 验证结果 - 应该检测到索引2（SOC从80骤降至0）和索引3（SOC从0骤升至79）为异常
        assertEquals(2, anomalies.size());
        assertTrue(anomalies.contains(2));
        assertTrue(anomalies.contains(3));
    }

    @Test
    public void testFixAnomaliesWithLinearInterpolation() {
        // 创建测试数据
        List<BatteryLog> testData = createTestBatteryLogs();
        List<Integer> anomalies = new ArrayList<>();
        anomalies.add(2); // 异常值索引
        anomalies.add(3); // 异常值索引
        
        // 修复异常值
        List<BatteryLog> fixedData = batterySOCService.fixAnomaliesWithLinearInterpolation(testData, anomalies);
        
        // 验证修复结果
        assertEquals(6, fixedData.size());
        
        // 验证异常值已被修复
        BigDecimal fixedSoc2 = fixedData.get(2).getSoc();
        BigDecimal fixedSoc3 = fixedData.get(3).getSoc();
        
        // 检查修复后的值是否在合理范围内（应该在80和79之间的线性插值）
        assertTrue(fixedSoc2.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(fixedSoc2.compareTo(BigDecimal.valueOf(80)) < 0);
        assertTrue(fixedSoc3.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(fixedSoc3.compareTo(BigDecimal.valueOf(80)) < 0);
        
        // 检查修复后的值是否呈现合理的渐变
        assertTrue(fixedSoc3.compareTo(fixedSoc2) <= 0); // 应该是递减或相等
    }

    @Test
    public void testSetSOCChangeThreshold() {
        // 设置自定义阈值
        double customThreshold = 20.0;
        batterySOCService.setSOCChangeThreshold(customThreshold);
        
        // 创建测试数据 - 包含25%的变化（高于新阈值）
        List<BatteryLog> testData = new ArrayList<>();
        
        BatteryLog log1 = createBatteryLog(LocalDateTime.now(), new BigDecimal(80));
        BatteryLog log2 = createBatteryLog(LocalDateTime.now().plusMinutes(1), new BigDecimal(55)); // 25%的变化
        
        testData.add(log1);
        testData.add(log2);
        
        // 检测异常值
        List<Integer> anomalies = batterySOCService.detectAnomalies(testData);
        
        // 验证结果 - 应该检测到索引1为异常（因为25% > 20%）
        assertEquals(1, anomalies.size());
        assertTrue(anomalies.contains(1));
        
        // 恢复默认阈值
        batterySOCService.setSOCChangeThreshold(30.0);
    }

    /**
     * 创建测试用的电池日志数据，包含异常值
     */
    private List<BatteryLog> createTestBatteryLogs() {
        List<BatteryLog> logs = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now();
        
        // 创建一系列电池日志，包含异常值
        logs.add(createBatteryLog(baseTime, new BigDecimal(80))); // 正常值
        logs.add(createBatteryLog(baseTime.plusMinutes(1), new BigDecimal(79))); // 正常值
        logs.add(createBatteryLog(baseTime.plusMinutes(2), new BigDecimal(0))); // 异常值：骤降至0
        logs.add(createBatteryLog(baseTime.plusMinutes(3), new BigDecimal(79))); // 异常值：骤升至79
        logs.add(createBatteryLog(baseTime.plusMinutes(4), new BigDecimal(78))); // 正常值
        logs.add(createBatteryLog(baseTime.plusMinutes(5), new BigDecimal(77))); // 正常值
        
        return logs;
    }

    /**
     * 创建单个电池日志对象
     */
    private BatteryLog createBatteryLog(LocalDateTime time, BigDecimal soc) {
        BatteryLog log = new BatteryLog();
        log.setShipId(BigInteger.ONE); // 测试用船舶ID
        log.setTime(java.sql.Timestamp.valueOf(time));
        log.setSoc(soc);
        log.setPosition("l_0");
        return log;
    }
}