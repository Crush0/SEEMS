package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.analyze.EnergyConsumptionCalc;
import cn.edu.just.ytc.seems.analyze.GPSCalc;
import cn.edu.just.ytc.seems.analyze.SpeedStats;
import cn.edu.just.ytc.seems.mapper.*;
import cn.edu.just.ytc.seems.pojo.dto.NoonReportParams;
import cn.edu.just.ytc.seems.pojo.dto.NoonReportResponse;
import cn.edu.just.ytc.seems.pojo.dto.ReportParams;
import cn.edu.just.ytc.seems.pojo.entity.*;
import cn.edu.just.ytc.seems.service.AnalyzeService;
import cn.edu.just.ytc.seems.service.ReportService;
import cn.edu.just.ytc.seems.utils.DateUtils;
import cn.edu.just.ytc.seems.utils.RedisUtils;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ReportServiceImpl extends BaseService implements ReportService {

    @Resource
    private NoonReportMapper noonReportMapper;

    @Resource
    private ShipMapper shipMapper;

    @Resource
    private AnalyzeService analyzeService;

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private WindLogMapper windLogMapper;

    @Resource
    private BatteryLogMapper batteryLogMapper;

    @Resource
    private GPSLogMapper gpsLogMapper;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private AnalyzeDataMapper analyzeDataMapper;


    @Override
    public NoonReport generateNoonReport() {
        UserShipRole userShipRole = getUserShipRole();
        ShipBaseInfo shipBaseInfo = shipMapper.selectById(userShipRole.getShipId());
        AnalyzeData analyzeData = analyzeService.analyzeByShipId(userShipRole.getShipId());
        GPSLog leastGPSLog = gpsLogMapper.selectLeastGPSLogByShipId(userShipRole.getShipId());
        WindLog windLog = windLogMapper.selectLeastWindLogByShipId(userShipRole.getShipId());
        NoonReport noonReport = new NoonReport();
        noonReport.setShipId(userShipRole.getShipId());
        if (leastGPSLog != null) {
            noonReport.setTheorySpeed(leastGPSLog.getSpeed());
            noonReport.setDirection(leastGPSLog.getDirection());
            noonReport.setLongitude(leastGPSLog.getLongitude());
            noonReport.setLatitude(leastGPSLog.getLatitude());
        }

        if (windLog != null){
            noonReport.setWindSpeed(windLog.getWindSpeed());
            noonReport.setWindDirection(windLog.getWindDirection());
        }
        if (analyzeData != null) {
            noonReport.setTotalVoyage(analyzeData.getSailingDistance().doubleValue() + shipBaseInfo.getSailedDistance().doubleValue());
        }
        return noonReport;
    }

    @Override
    public void updateOrInsert(NoonReport noonReport) {
        UserShipRole userRole = getUserShipRole();
        noonReport.setShipId(userRole.getShipId());
        noonReportMapper.insertOrUpdate(noonReport);
    }

    @Override
    public NoonReportResponse queryNoonReport(NoonReportParams noonReportParams) {
        QueryWrapper<NoonReport> queryWrapper = getNoonReportQueryWrapper(noonReportParams);
        IPage<NoonReport> page = noonReportMapper.selectPage(new Page<>(noonReportParams.getCurrent(), noonReportParams.getPageSize()), queryWrapper);
        NoonReportResponse noonReportResponse = new NoonReportResponse();
        noonReportResponse.setTotal(page.getTotal());
        noonReportResponse.setList(page.getRecords());
        return noonReportResponse;
    }

    @Override
    public ReportData queryReportData(ReportParams reportParams) {
        LocalDate endDate = reportParams.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        ShipBaseInfo shipBaseInfo = getShipBaseInfo(null);
        BigInteger shipId = shipBaseInfo.getId();
        // 优化：增强Redis缓存键并设置不同的过期时间
        String redisKey = "reportData:" + shipId + ":" + reportParams.getReportType().toString() + "|" + 
                         sdf.format(reportParams.getStartDate()) + "-" + sdf.format(reportParams.getEndDate());
        
        if (redisUtils.hasKey(redisKey)) {   //如果缓存中有数据，则直接返回缓存数据
            return redisUtils.get(redisKey, ReportData.class);
        }
        
        ReportData reportData = null;
        // 设置船舶ID到reportParams中，以便查询方法使用
        reportParams.setShipId(shipId);
        
        if (reportParams.getReportType().equals(ReportParams.ReportType.DAILY)) {
            //如果endDate大于今天23:59:59
            if(endDate.isAfter(LocalDate.now().plusDays(1))) {
                return new ReportData().setTime(reportParams.getEndDate());
            }
            reportData = queryDailyAnalyzeData(reportParams);
        } else if (reportParams.getReportType().equals(ReportParams.ReportType.WEEKLY)) {
            //如果endDate大于下周一 00:00
            if(endDate.isAfter(LocalDate.now().plusDays(7).with(DayOfWeek.MONDAY))) {
                return new ReportData().setTime(reportParams.getEndDate());
            }
            reportData = queryWeeklyAnalyzeData(reportParams);
        } else if (reportParams.getReportType().equals(ReportParams.ReportType.MONTHLY)) {
            //如果endDate大于下月1号 00:00
            if(endDate.isAfter(LocalDate.now().plusMonths(1).withDayOfMonth(1))) {
                return new ReportData().setTime(reportParams.getEndDate());
            }
            reportData = queryMonthlyAnalyzeData(reportParams);
        } else if (reportParams.getReportType().equals(ReportParams.ReportType.YEARLY)) {
            //如果endDate大于明年1月1日 00:00
            if(endDate.isAfter(LocalDate.now().plusYears(1).withDayOfYear(1))) {
                return new ReportData().setTime(reportParams.getEndDate());
            }
            reportData = queryYearlyAnalyzeData(reportParams);
        } else if (reportParams.getReportType().equals(ReportParams.ReportType.QUARTERLY)) {
            //如果endDate大于下季度1月1日 00:00
            if(endDate.isAfter(getFirstDayOfNextQuarter(LocalDate.now()))) {
                return new ReportData().setTime(reportParams.getEndDate());
            }
            reportData = queryQuarterlyAnalyzeData(reportParams);
        }
        
        if (reportData != null) {
            // 根据报表类型设置不同的缓存过期时间
            long expireTimeMinutes = getExpireTimeByReportType(reportParams.getReportType());
            redisUtils.set(redisKey, JSONObject.toJSONString(reportData), expireTimeMinutes, TimeUnit.MINUTES);
        }
        return reportData;
    }
    
    /**
     * 根据报表类型返回合适的缓存过期时间
     */
    private long getExpireTimeByReportType(ReportParams.ReportType reportType) {
        switch (reportType) {
            case DAILY: return 30; // 日报缓存30分钟
            case WEEKLY: return 60; // 周报缓存1小时
            case MONTHLY: return 120; // 月报缓存2小时
            case QUARTERLY: return 1440; // 季报缓存24小时
            case YEARLY: return 4320; // 年报缓存3天
            default: return 30;
        }
    }

    @Override
    public ReportData[] queryReportData(ReportParams reportParams, int limit) {
        List<ReportData> result = new ArrayList<>();

        // 获取当前报告的开始日期（即 reportParams 中的 startDate）
        Date startDate = reportParams.getStartDate();
//        Date endDate = reportParams.getEndDate();

        // 如果是 DAILY 类型，生成前 limit 天的数据
        if (reportParams.getReportType() == ReportParams.ReportType.DAILY) {
            for (int i = 0; i < limit; i++) {
                // 生成每天的 ReportParams
                Date currentDate = DateUtils.getDateDaysBefore(startDate, i);
                LocalDateTime endOfDay = currentDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .atTime(23, 59, 59);
                // 创建新的 ReportParams，每次传入不同的日期
                ReportParams dailyReportParams = new ReportParams();
                dailyReportParams.setReportType(ReportParams.ReportType.DAILY);
                dailyReportParams.setStartDate(currentDate);
                dailyReportParams.setEndDate(Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant())); // 同一天23 59 59
                // 调用原始查询方法
                ReportData reportData = queryReportData(dailyReportParams);
                if (reportData != null) {
                    // 将查询结果添加到返回的结果集
                    result.add(reportData);
                }
            }
        }
        // 如果是 WEEKLY 类型，生成前 limit 周的数据
        if (reportParams.getReportType() == ReportParams.ReportType.WEEKLY) {
            for (int i = 0; i < limit; i++) {
                // 从startDate开始往前推 limit - 1 周
                LocalDate currentDate = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusWeeks(i);
                // 创建新的 ReportParams，每次传入不同的日期
                ReportParams weeklyReportParams = new ReportParams();
                weeklyReportParams.setReportType(ReportParams.ReportType.WEEKLY);
                weeklyReportParams.setStartDate(Date.from(currentDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
                weeklyReportParams.setEndDate(Date.from(currentDate.plusDays(6).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
                ReportData reportData = queryReportData(weeklyReportParams);
                if (reportData != null) {
                    // 将查询结果添加到返回的结果集
                    result.add(reportData);
                }
            }
        }
        // 如果是 MONTHLY 类型，生成前 limit 个月的数据
        if (reportParams.getReportType() == ReportParams.ReportType.MONTHLY) {
            for (int i = 0; i < limit; i++) {
                // 从startDate开始往前推 limit - 1 个月
                LocalDate currentDate = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusMonths(i);
                // 创建新的 ReportParams，每次传入不同的日期
                ReportParams monthlyReportParams = new ReportParams();
                monthlyReportParams.setReportType(ReportParams.ReportType.MONTHLY);
                monthlyReportParams.setStartDate(Date.from(currentDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
                monthlyReportParams.setEndDate(Date.from(currentDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
                ReportData reportData = queryReportData(monthlyReportParams);
                if (reportData != null) {
                    // 将查询结果添加到返回的结果集
                    result.add(reportData);
                }
            }
        }

        // 如果是 QUARTERLY 类型，生成前 limit 季度的数据
        if (reportParams.getReportType() == ReportParams.ReportType.QUARTERLY) {
            for (int i = 0; i < limit; i++) {
                // 从startDate开始往前推 limit - 1 个季度
                LocalDate currentDate = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusMonths(i * 3L);
                // 创建新的 ReportParams，每次传入不同的日期
                ReportParams quarterlyReportParams = new ReportParams();
                quarterlyReportParams.setReportType(ReportParams.ReportType.QUARTERLY);
                LocalDate firstDayOfLastQuarter = getFirstDayOfLastQuarter(currentDate);
                quarterlyReportParams.setStartDate(Date.from(firstDayOfLastQuarter.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
                quarterlyReportParams.setEndDate(Date.from(getFirstDayOfNextQuarter(firstDayOfLastQuarter).minusDays(1).with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
                ReportData reportData = queryReportData(quarterlyReportParams);
                if (reportData != null) {
                    // 将查询结果添加到返回的结果集
                    result.add(reportData);
                }
            }
        }

        // 如果是 YEARLY 类型，生成前 limit 年的数据
        if (reportParams.getReportType() == ReportParams.ReportType.YEARLY) {
            for (int i = 0; i < limit; i++) {
                // 从startDate开始往前推 limit - 1 年
                LocalDate currentDate = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusYears(i);
                // 创建新的 ReportParams，每次传入不同的日期
                ReportParams yearlyReportParams = new ReportParams();
                yearlyReportParams.setReportType(ReportParams.ReportType.YEARLY);
                yearlyReportParams.setStartDate(Date.from(currentDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
                yearlyReportParams.setEndDate(Date.from(currentDate.with(TemporalAdjusters.lastDayOfYear()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
                ReportData reportData = queryReportData(yearlyReportParams);
                if (reportData != null) {
                    // 将查询结果添加到返回的结果集
                    result.add(reportData);
                }
            }
        }

        // 按照startDate升序排列
        result.sort(Comparator.comparing(ReportData::getTime));


        // 将结果转换为数组并返回
        return result.toArray(new ReportData[0]);
    }

    @Scheduled(cron = "0 10 0 ? * ?")
    @Override
    public void generateDailyReport() {
        List<BigInteger> shipIdList = shipMapper.selectAllShipId();
        shipIdList.forEach(this::generateDailyReportByShipId);
    }

    @Scheduled(cron = "0 35 0 ? * 1")
    @Override
    public void generateWeeklyReportByShipId() {
        List<BigInteger> shipIdList = shipMapper.selectAllShipId();
        shipIdList.forEach(this::generateWeeklyReportByShipId);
    }



    @Scheduled(cron = "0 20 0 1 1/1 ?")
    @Override
    public void generateMonthlyReport() {
        List<BigInteger> shipIdList = shipMapper.selectAllShipId();
        shipIdList.forEach(this::generateMonthlyReportByShipId);
    }

    @Scheduled(cron = "0 50 0 1 1,4,7,10 *")
    @Override
    public void generateQuarterlyReport() {
        List<BigInteger> shipIdList = shipMapper.selectAllShipId();
        shipIdList.forEach(this::generateQuarterlyReportByShipId);
    }

    @Scheduled(cron = "0 0 1 1 1 *")
    @Override
    public void generateYearlyReport() {
        List<BigInteger> shipIdList = shipMapper.selectAllShipId();
        shipIdList.forEach(this::generateYearlyReportByShipId);
    }



    private void generateDailyReportByShipId(BigInteger shipId) {
        ReportParams reportParams = new ReportParams();
        reportParams.setReportType(ReportParams.ReportType.DAILY);
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 昨天 00:00
        LocalDateTime startOfYesterday = yesterday.atStartOfDay();

        // 昨天 23:59:59
        LocalDateTime endOfYesterday = yesterday.atTime(23, 59, 59);

        // 转换为 java.util.Date
        Date startDate = Date.from(startOfYesterday.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endOfYesterday.atZone(ZoneId.systemDefault()).toInstant());
        reportParams.setStartDate(startDate);
        reportParams.setEndDate(endDate);
        ReportData dailyReportData = generateBasicData(shipId, reportParams);
        AnalyzeData analyzeData = analyzeDataMapper.getLastHourDataByDate(shipId, Timestamp.valueOf(endOfYesterday));
        dailyReportData.setAnalyzeData(analyzeData);
        dailyReportData.setReportType(ReportParams.ReportType.DAILY);
        dailyReportData.setTime(Date.from(endOfYesterday.atZone(ZoneId.systemDefault()).toInstant()));
        reportMapper.insert(dailyReportData);
    }

    private void generateWeeklyReportByShipId(BigInteger shipId) {
        // 获取上周第一天
        LocalDate lastWeekFirstDay = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        // 获取上周最后一天
        LocalDate lastWeekLastDay = lastWeekFirstDay.plusWeeks(1).minusDays(1);
        List<ReportData> reportDataList = new ArrayList<>();
        for (LocalDate date = lastWeekFirstDay; date.isBefore(lastWeekLastDay); date = date.plusDays(1)) {
            ReportParams reportParams = new ReportParams();
            reportParams.setReportType(ReportParams.ReportType.DAILY);
            // 转换为 java.util.Date
            Date startDate = Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
            reportParams.setStartDate(startDate);
            reportParams.setEndDate(endDate);
            reportDataList.add(queryDailyAnalyzeData(reportParams));
        }
        ReportData weeklyReportData = aggregateReportData(reportDataList);
        if (weeklyReportData != null) {
            weeklyReportData.setReportType(ReportParams.ReportType.WEEKLY);
            weeklyReportData.setTime(Date.from(lastWeekLastDay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
            reportMapper.insert(weeklyReportData);
        }
    }

    private void generateMonthlyReportByShipId(BigInteger shipId) {
        // 获取上个月的第一天
        LocalDate lastMonthFirstDay = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        // 获取上个月的最后一天
        LocalDate lastMonthLastDay = lastMonthFirstDay.plusMonths(1).minusDays(1);
        List<ReportData> reportDataList = new ArrayList<>();
        // 遍历每一天
        for (LocalDate date = lastMonthFirstDay; date.isBefore(lastMonthLastDay); date = date.plusDays(1)) {
            ReportParams reportParams = new ReportParams();
            reportParams.setReportType(ReportParams.ReportType.DAILY);
            // 转换为 java.util.Date
            Date startDate = Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
            reportParams.setStartDate(startDate);
            reportParams.setEndDate(endDate);
            reportDataList.add(queryDailyAnalyzeData(reportParams));
        }
        ReportData monthlyReportData = aggregateReportData(reportDataList);
        if (monthlyReportData != null) {
            monthlyReportData.setReportType(ReportParams.ReportType.MONTHLY);
            monthlyReportData.setTime(Date.from(lastMonthLastDay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
            reportMapper.insert(monthlyReportData);
        }
    }

    public static LocalDate getFirstDayOfLastQuarter(LocalDate date) {
        int currentQuarter = (date.getMonthValue() - 1) / 3 + 1; // 当前季度
        int lastQuarter = currentQuarter - 1;
        int year = date.getYear();

        if (lastQuarter == 0) {
            lastQuarter = 4; // 上一年第四季度
            year -= 1;
        }

        Month startMonth = Month.of((lastQuarter - 1) * 3 + 1); // 上个季度的开始月份
        return LocalDate.of(year, startMonth, 1); // 上个季度第一天
    }

    public static LocalDate getFirstDayOfNextQuarter(LocalDate date) {
        int currentQuarter = (date.getMonthValue() - 1) / 3 + 1; // 当前季度
        int nextQuarter = currentQuarter + 1;
        int year = date.getYear();

        if (nextQuarter > 4) {
            nextQuarter = 1; // 下一年第一季度
            year += 1;
        }

        Month startMonth = Month.of((nextQuarter - 1) * 3 + 1); // 下个季度开始月份
        return LocalDate.of(year, startMonth, 1); // 下个季度第一天
    }

    public static LocalDate getLastDayOfLastQuarter(LocalDate date) {
        LocalDate firstDayOfLastQuarter = getFirstDayOfLastQuarter(date);
        return firstDayOfLastQuarter.plusMonths(3).minusDays(1); // 上个季度最后一天
    }

    public static LocalDate getFirstDayOfCurrentQuarter(LocalDate date) {
        int currentQuarter = (date.getMonthValue() - 1) / 3 + 1; // 当前季度
        Month startMonth = Month.of((currentQuarter - 1) * 3 + 1); // 当前季度开始月份
        return LocalDate.of(date.getYear(), startMonth, 1); // 当前季度第一天
    }

    public static LocalDate getFirstDayOfQuarter(int year, int quarter) {
        Month startMonth = Month.of((quarter - 1) * 3 + 1); // 当前季度的开始月份
        return LocalDate.of(year, startMonth, 1); // 当前季度的第一天
    }

    public static LocalDate getLastDayOfQuarter(int year, int quarter) {
        LocalDate firstDayOfQuarter = getFirstDayOfQuarter(year, quarter);
        return firstDayOfQuarter.plusMonths(3).minusDays(1); // 当前季度的最后一天
    }

    public static LocalDate getLastDayOfCurrentQuarter(LocalDate date) {
        LocalDate firstDayOfCurrentQuarter = getFirstDayOfCurrentQuarter(date);
        return firstDayOfCurrentQuarter.plusMonths(3).minusDays(1); // 当前季度最后一天
    }

    private void generateQuarterlyReportByShipId(BigInteger shipId) {
        // 获取上个季度第一天
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfLastQuarter = getFirstDayOfLastQuarter(today);
        LocalDate lastDayOfLastQuarter = getLastDayOfLastQuarter(today);
        YearMonth startMonth = YearMonth.from(firstDayOfLastQuarter);
        YearMonth endMonth = YearMonth.from(lastDayOfLastQuarter);
        List<ReportData> reportDataList = new ArrayList<>();
        // 遍历每一月
        while (!startMonth.isAfter(endMonth)) {
            ReportParams reportParams = new ReportParams();
            reportParams.setReportType(ReportParams.ReportType.MONTHLY);
            Date startDate = Date.from(startMonth.atDay(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(startMonth.atEndOfMonth().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            reportParams.setStartDate(startDate);
            reportParams.setEndDate(endDate);
            reportDataList.add(queryMonthlyAnalyzeData(reportParams));
            startMonth = startMonth.plusMonths(1); // 下一月
        }
        ReportData quarterlyReportData = aggregateReportData(reportDataList);
        if (quarterlyReportData != null) {
            quarterlyReportData.setReportType(ReportParams.ReportType.QUARTERLY);
            quarterlyReportData.setTime(Date.from(lastDayOfLastQuarter.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
            reportMapper.insert(quarterlyReportData);
        }
    }

    private void generateYearlyReportByShipId(BigInteger shipId) {
        // 获取去年第一天
        int lastYear = LocalDate.now().getYear() - 1; // 去年的年份
        List<ReportData> reportDataList = new ArrayList<>();
        // 遍历去年的四个季度
        for (int quarter = 1; quarter <= 4; quarter++) {
            LocalDate firstDayOfQuarter = getFirstDayOfQuarter(lastYear, quarter);
            LocalDate lastDayOfQuarter = getLastDayOfQuarter(lastYear, quarter);
            ReportParams reportParams = new ReportParams();
            reportParams.setReportType(ReportParams.ReportType.QUARTERLY);
            Date startDate = Date.from(firstDayOfQuarter.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(lastDayOfQuarter.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
            reportParams.setStartDate(startDate);
            reportParams.setEndDate(endDate);
            ReportData quarterlyReportData = queryQuarterlyAnalyzeData(reportParams);
            reportDataList.add(quarterlyReportData);
        }
        ReportData yaerlyReportData = aggregateReportData(reportDataList);
        if (yaerlyReportData != null) {
            yaerlyReportData.setReportType(ReportParams.ReportType.YEARLY);
            yaerlyReportData.setTime(Date.from(LocalDate.of(lastYear, 12, 31).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
            reportMapper.insert(yaerlyReportData);
        }
    }

    public ReportData aggregateReportData(List<ReportData> reportDataList) {
        if (reportDataList == null || reportDataList.isEmpty()) {
            return null; // 或抛出异常
        }

        BigInteger shipId = null; // 假设多个 shipId 不冲突
        BigDecimal totalEnergyConsumption = BigDecimal.ZERO;
        BigDecimal totalCarbonEmission = BigDecimal.ZERO;
        BigDecimal totalSailDuration = BigDecimal.ZERO;
        BigDecimal totalSailDistance = BigDecimal.ZERO;

        SpeedStats hoveringStats = new SpeedStats();
        SpeedStats draggingStats = new SpeedStats();

        BigDecimal totalDailyEnergyConsumption = BigDecimal.ZERO;
        BigDecimal totalDailyCarbonEmission = BigDecimal.ZERO;
        BigDecimal totalPreHourEnergyConsumption = BigDecimal.ZERO;
        BigDecimal totalPreUnitWorkEnergyConsumption = BigDecimal.ZERO;
        BigDecimal totalPreDistanceEnergyConsumption = BigDecimal.ZERO;
        double maxHoveringSpeed = 0;
        double maxDraggingSpeed = 0;
        int analyzeDataCount = 0; // 用于计算 AnalyzeData 的平均值

        ReportData result = new ReportData();

        for (ReportData data : reportDataList) {
            if (data == null) {
                continue;
            }
            if (shipId == null) {
                shipId = data.getShipId();
            }

            if (data.getEnergyConsumption() != null) {
                totalEnergyConsumption = totalEnergyConsumption.add(data.getEnergyConsumption());
            }
            if (data.getCarbonEmission() != null) {
                totalCarbonEmission = totalCarbonEmission.add(data.getCarbonEmission());
            }
            if (data.getSailDuration() != null) {
                totalSailDuration = totalSailDuration.add(data.getSailDuration());
            }
            if (data.getSailDistance() != null) {
                totalSailDistance = totalSailDistance.add(data.getSailDistance());
            }


            if (data.getHoveringSpeedStats() != null) {
                if (data.getHoveringSpeedStats().getMaxSpeed() > maxHoveringSpeed) {
                    maxHoveringSpeed = data.getHoveringSpeedStats().getMaxSpeed();
                }
                hoveringStats.update(data.getHoveringSpeedStats().getAvgSpeed());
                hoveringStats.count += data.getHoveringSpeedStats().getCount();
            }

            if (data.getDraggingSpeedStats() != null) {
                if (data.getDraggingSpeedStats().getMaxSpeed() > maxDraggingSpeed) {
                    maxDraggingSpeed = data.getDraggingSpeedStats().getMaxSpeed();
                }
                draggingStats.update(data.getDraggingSpeedStats().getAvgSpeed());
                draggingStats.count += data.getDraggingSpeedStats().getCount();
            }

            // 处理 AnalyzeData
            if (data.getAnalyzeData() != null) {
                AnalyzeData analyzeData = data.getAnalyzeData();
                if (analyzeData.getDailyEnergyConsumption() != null) {
                    totalDailyEnergyConsumption = totalDailyEnergyConsumption.add(analyzeData.getDailyEnergyConsumption());
                }
                if (analyzeData.getDailyCarbonEmission() != null) {
                    totalDailyCarbonEmission = totalDailyCarbonEmission.add(analyzeData.getDailyCarbonEmission());
                }
                if (analyzeData.getPreHourEnergyConsumption() != null) {
                    totalPreHourEnergyConsumption = totalPreHourEnergyConsumption.add(analyzeData.getPreHourEnergyConsumption());
                }
                if (analyzeData.getPreUnitWorkEnergyConsumption() != null) {
                    totalPreUnitWorkEnergyConsumption = totalPreUnitWorkEnergyConsumption.add(analyzeData.getPreUnitWorkEnergyConsumption());
                }
                if (analyzeData.getPreDistanceEnergyConsumption() != null) {
                    totalPreDistanceEnergyConsumption = totalPreDistanceEnergyConsumption.add(analyzeData.getPreDistanceEnergyConsumption());
                }
                analyzeDataCount++;
            }
        }

        // 更新结果
        result.setShipId(shipId);
        result.setEnergyConsumption(totalEnergyConsumption);
        result.setCarbonEmission(totalCarbonEmission);
        result.setSailDuration(totalSailDuration);
        result.setSailDistance(totalSailDistance);

        // 更新 SpeedStats 的平均值
        hoveringStats.getAverageSpeed();
        draggingStats.getAverageSpeed();
        hoveringStats.setMaxSpeed(maxHoveringSpeed);
        draggingStats.setMaxSpeed(maxDraggingSpeed);
        result.setHoveringSpeedStats(hoveringStats);
        result.setDraggingSpeedStats(draggingStats);

        // 生成 AnalyzeData 的平均值
        if (analyzeDataCount > 0) {
            AnalyzeData avgAnalyzeData = new AnalyzeData();
            avgAnalyzeData.setDailyEnergyConsumption(totalDailyEnergyConsumption.divide(BigDecimal.valueOf(analyzeDataCount), 2, RoundingMode.HALF_UP));
            avgAnalyzeData.setDailyCarbonEmission(totalDailyCarbonEmission.divide(BigDecimal.valueOf(analyzeDataCount), 2, RoundingMode.HALF_UP));
            avgAnalyzeData.setPreHourEnergyConsumption(totalPreHourEnergyConsumption.divide(BigDecimal.valueOf(analyzeDataCount), 2, RoundingMode.HALF_UP));
            avgAnalyzeData.setPreUnitWorkEnergyConsumption(totalPreUnitWorkEnergyConsumption.divide(BigDecimal.valueOf(analyzeDataCount), 2, RoundingMode.HALF_UP));
            avgAnalyzeData.setPreDistanceEnergyConsumption(totalPreDistanceEnergyConsumption.divide(BigDecimal.valueOf(analyzeDataCount), 2, RoundingMode.HALF_UP));
            result.setAnalyzeData(avgAnalyzeData);
        }

        return result;
    }



    private ReportData generateBasicData(BigInteger shipId, ReportParams reportParams) {
        ShipBaseInfo shipBaseInfo = getShipBaseInfo(shipId);
        Date startDate = reportParams.getStartDate();
        Date endDate = reportParams.getEndDate();
        List<List<BatteryLog>> batteryLogs = EnergyConsumptionCalc.fetchBatteryLogsBetween(batteryLogMapper, startDate, endDate, shipBaseInfo.getId());
        Timestamp startTime = new Timestamp(startDate.getTime());
        Timestamp endTime = new Timestamp(endDate.getTime());
        List<GPSLog> gpsLogs = gpsLogMapper.getShipGPSLogByShipIdAndDateTimeBetween(shipBaseInfo.getId(),startTime, endTime);
        BigDecimal energyConsumption = EnergyConsumptionCalc.calculateTotalEnergyConsumption(batteryLogs, shipBaseInfo.getMaxBatteryCapacity());
        Double sailingDistance = GPSCalc.calculateTotalSailingDistance(gpsLogs);
        Long sailingTime = GPSCalc.calculateTotalSailingTime(gpsLogs);
        SpeedStats[] speedResult = GPSCalc.calculateSpeedStats(gpsLogs);
        ReportData reportData = new ReportData();
        reportData.setReportType(ReportParams.ReportType.DAILY);
        reportData.setShipId(shipBaseInfo.getId());
        reportData.setEnergyConsumption(energyConsumption);
        reportData.setCarbonEmission(EnergyConsumptionCalc.calculateTotalCarbonEmission(energyConsumption));
        reportData.setSailDistance(BigDecimal.valueOf(sailingDistance));
        reportData.setSailDuration(BigDecimal.valueOf(sailingTime));
        reportData.setHoveringSpeedStats(speedResult[0]);
        reportData.setDraggingSpeedStats(speedResult[1]);
        reportData.setCII(EnergyConsumptionCalc.calculateCII(reportData.getCarbonEmission(), shipBaseInfo.getProgress(), sailingDistance));
        return reportData;
    }

    private ReportData queryQuarterlyAnalyzeData(ReportParams reportParams) {
        BigInteger shipId = reportParams.getShipId();
        
        LambdaQueryWrapper<ReportData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportData::getShipId, shipId); // 优化：添加船舶ID过滤
        queryWrapper.eq(ReportData::getReportType, ReportParams.ReportType.QUARTERLY);
        queryWrapper.between(ReportData::getTime, reportParams.getStartDate(), reportParams.getEndDate());
        queryWrapper.orderByDesc(ReportData::getCreateDate);
        queryWrapper.last("limit 1");
        ReportData reportData = reportMapper.selectOne(queryWrapper);
        
        if (reportData == null) {
            // 优化：直接从数据库查询季度数据，避免递归调用月度报表
            LocalDate today = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate firstDayOfQuarter = getFirstDayOfCurrentQuarter(today);
            LocalDate lastDayOfQuarter = getLastDayOfCurrentQuarter(today);
            
            // 设置船舶ID并直接生成基础数据
            reportData = generateBasicData(shipId, reportParams);
            
            if (reportData != null) {
                LocalDate startDate = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                reportData.setReportType(ReportParams.ReportType.QUARTERLY);
                reportData.setTime(new Timestamp(reportParams.getStartDate().getTime()));
                if(!startDate.isAfter(getFirstDayOfCurrentQuarter(LocalDate.now()))) {
                    reportMapper.insert(reportData);
                }
            }
        }
        return reportData;
    }

    @Override
    public ReportData queryWeeklyAnalyzeData(ReportParams reportParams) {
        BigInteger shipId = reportParams.getShipId();
        
        LambdaQueryWrapper<ReportData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportData::getShipId, shipId); // 优化：添加船舶ID过滤
        queryWrapper.eq(ReportData::getReportType, ReportParams.ReportType.WEEKLY);
        queryWrapper.between(ReportData::getTime, reportParams.getStartDate(), reportParams.getEndDate());
        queryWrapper.orderByDesc(ReportData::getCreateDate);
        queryWrapper.last("limit 1");
        ReportData reportData = reportMapper.selectOne(queryWrapper);
        
        if (reportData == null) {
            LocalDate weekFirstDay = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate weekLastDay = reportParams.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            List<ReportData> reportDataList = new ArrayList<>();
            
            // 优化：使用并行流处理每日数据查询
            List<LocalDate> dateList = new ArrayList<>();
            for (LocalDate date = weekFirstDay; date.isBefore(weekLastDay); date = date.plusDays(1)) {
                dateList.add(date);
            }
            
            // 并行处理每日报表生成
            List<ReportData> parallelResults = dateList.parallelStream().map(date -> {
                ReportParams queryReportParams = new ReportParams();
                queryReportParams.setShipId(shipId); // 设置船舶ID
                queryReportParams.setReportType(ReportParams.ReportType.DAILY);
                Date startDate = Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                Date endDate = Date.from(date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                queryReportParams.setStartDate(startDate);
                queryReportParams.setEndDate(endDate);
                return queryDailyAnalyzeData(queryReportParams);
            }).filter(data -> data != null).toList();
            
            reportDataList.addAll(parallelResults);
            reportData = aggregateReportData(reportDataList);
            
            if (reportData != null) {
                LocalDate startDate = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                reportData.setReportType(ReportParams.ReportType.WEEKLY);
                reportData.setTime(new Timestamp(reportParams.getStartDate().getTime()));
                reportData.setShipId(shipId); // 设置船舶ID
                if(!startDate.isAfter(LocalDate.now().minusDays(7))) {
                    reportMapper.insert(reportData);
                }
            }
        }
        return reportData;
    }

    @Override
    public ReportData queryMonthlyAnalyzeData(ReportParams reportParams) {
        BigInteger shipId = reportParams.getShipId();
        
        LambdaQueryWrapper<ReportData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportData::getShipId, shipId); // 优化：添加船舶ID过滤
        queryWrapper.eq(ReportData::getReportType, ReportParams.ReportType.MONTHLY);
        queryWrapper.between(ReportData::getTime, reportParams.getStartDate(), reportParams.getEndDate());
        queryWrapper.orderByDesc(ReportData::getCreateDate);
        queryWrapper.last("limit 1");
        ReportData reportData = reportMapper.selectOne(queryWrapper);
        
        if (reportData == null) {
            List<ReportData> reportDataList = new ArrayList<>();
            LocalDate monthFirstDay = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate monthLastDay = reportParams.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
            // 优化：使用并行流处理每日数据查询
            List<LocalDate> dateList = new ArrayList<>();
            for (LocalDate date = monthFirstDay; date.isBefore(monthLastDay); date = date.plusDays(1)) {
                dateList.add(date);
            }
            
            // 并行处理每日报表生成
            List<ReportData> parallelResults = dateList.parallelStream().map(date -> {
                ReportParams queryReportParams = new ReportParams();
                queryReportParams.setShipId(shipId); // 设置船舶ID
                queryReportParams.setReportType(ReportParams.ReportType.DAILY);
                Date startDate = Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                Date endDate = Date.from(date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                queryReportParams.setStartDate(startDate);
                queryReportParams.setEndDate(endDate);
                return queryDailyAnalyzeData(queryReportParams);
            }).filter(data -> data != null).toList();
            
            reportDataList.addAll(parallelResults);
            reportData = aggregateReportData(reportDataList);
            
            if (reportData != null) {
                LocalDate startDate = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                reportData.setReportType(ReportParams.ReportType.MONTHLY);
                reportData.setTime(new Timestamp(reportParams.getStartDate().getTime()));
                reportData.setShipId(shipId); // 设置船舶ID
                if(!startDate.isAfter(LocalDate.now().minusMonths(1))) {
                    reportMapper.insert(reportData);
                }
            }
        }
        return reportData;
    }

    @Override
    public ReportData queryYearlyAnalyzeData(ReportParams reportParams) {
        BigInteger shipId = reportParams.getShipId();
        
        LambdaQueryWrapper<ReportData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportData::getShipId, shipId); // 优化：添加船舶ID过滤
        queryWrapper.eq(ReportData::getReportType, ReportParams.ReportType.YEARLY);
        queryWrapper.between(ReportData::getTime, reportParams.getStartDate(), reportParams.getEndDate());
        queryWrapper.orderByDesc(ReportData::getCreateDate);
        queryWrapper.last("limit 1");
        ReportData reportData = reportMapper.selectOne(queryWrapper);
        
        if (reportData == null) {
            int year = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
            List<ReportData> reportDataList = new ArrayList<>();
            
            // 优化：使用并行流处理季度数据查询
            List<Integer> quarters = List.of(1, 2, 3, 4);
            List<ReportData> parallelResults = quarters.parallelStream().map(quarter -> {
                LocalDate firstDayOfQuarter = getFirstDayOfQuarter(year, quarter);
                LocalDate lastDayOfQuarter = getLastDayOfQuarter(year, quarter);
                ReportParams queryReportParams = new ReportParams();
                queryReportParams.setShipId(shipId); // 设置船舶ID
                queryReportParams.setReportType(ReportParams.ReportType.QUARTERLY);
                Date startDate = Date.from(firstDayOfQuarter.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                Date endDate = Date.from(lastDayOfQuarter.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                queryReportParams.setStartDate(startDate);
                queryReportParams.setEndDate(endDate);
                return queryQuarterlyAnalyzeData(queryReportParams);
            }).filter(data -> data != null).toList();
            
            reportDataList.addAll(parallelResults);
            reportData = aggregateReportData(reportDataList);
            
            if (reportData != null) {
                int startYear = reportParams.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
                reportData.setReportType(ReportParams.ReportType.YEARLY);
                reportData.setTime(new Timestamp(reportParams.getStartDate().getTime()));
                reportData.setShipId(shipId); // 设置船舶ID
                if(startYear < year) {
                    reportMapper.insert(reportData);
                }
            }
        }
        return reportData;
    }

    @Override
    public ReportData queryDailyAnalyzeData(ReportParams reportParams) {
        BigInteger shipId = reportParams.getShipId();
        
        LambdaQueryWrapper<ReportData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportData::getShipId, shipId); // 优化：添加船舶ID过滤
        queryWrapper.eq(ReportData::getReportType, ReportParams.ReportType.DAILY);
        queryWrapper.between(ReportData::getTime, reportParams.getStartDate(), reportParams.getEndDate());
        queryWrapper.orderByDesc(ReportData::getCreateDate);
        queryWrapper.last("limit 1");
        ReportData reportData = reportMapper.selectOne(queryWrapper);
        
        if (reportData == null) {
            Date date = reportParams.getEndDate();
            reportData = generateBasicData(shipId, reportParams);
            if (reportData != null) {
                AnalyzeData analyzeData = analyzeDataMapper.getLastHourDataByDate(shipId, new Timestamp(date.getTime()));
                reportData.setAnalyzeData(analyzeData);
                reportData.setReportType(ReportParams.ReportType.DAILY);
                reportData.setTime(new Timestamp(date.getTime()));
                reportData.setShipId(shipId); // 设置船舶ID
                
                LocalDate fromEndDate = LocalDate.from(reportParams.getEndDate().toInstant().atZone(ZoneId.systemDefault()));
                LocalDate today = LocalDate.now();
                if (fromEndDate.isBefore(today)) {
                    reportMapper.insert(reportData);
                }
            }
        }
        return reportData;
    }



    @NotNull
    private QueryWrapper<NoonReport> getNoonReportQueryWrapper(NoonReportParams noonReportParams) {
        UserShipRole userShipRole = getUserShipRole();
        QueryWrapper<NoonReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ship_id", userShipRole.getShipId());
        queryWrapper.orderByDesc("time");
        if (noonReportParams.getCreatedTime() != null && noonReportParams.getCreatedTime().length == 2) {
            Date start = noonReportParams.getCreatedTime()[0];
            Date end = noonReportParams.getCreatedTime()[1];
            queryWrapper.between("time", start, end);
        }
        return queryWrapper;
    }
}
