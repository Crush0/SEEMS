package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.dto.NoonReportParams;
import cn.edu.just.ytc.seems.pojo.dto.NoonReportResponse;
import cn.edu.just.ytc.seems.pojo.dto.ReportParams;
import cn.edu.just.ytc.seems.pojo.entity.NoonReport;
import cn.edu.just.ytc.seems.pojo.entity.ReportData;
import org.springframework.scheduling.annotation.Scheduled;

public interface ReportService extends IBaseUserInfo{
    NoonReport generateNoonReport();

    void updateOrInsert(NoonReport noonReport);

    NoonReportResponse queryNoonReport(NoonReportParams noonReportParams);

    ReportData queryReportData(ReportParams reportParams);

    ReportData[] queryReportData(ReportParams reportParams, int limit);

    void generateDailyReport();

    @Scheduled(cron = "0 30 0 ? * 1")
    void generateWeeklyReportByShipId();

    @Scheduled(cron = "0 20 0 1 1/1 ?")
    void generateMonthlyReport();

    @Scheduled(cron = "0 50 0 1 1,4,7,10 *")
    void generateQuarterlyReport();

    @Scheduled(cron = "0 0 1 1 1 *")
    void generateYearlyReport();

    ReportData queryWeeklyAnalyzeData(ReportParams reportParams);

    ReportData queryMonthlyAnalyzeData(ReportParams reportParams);

    ReportData queryYearlyAnalyzeData(ReportParams reportParams);

    ReportData queryDailyAnalyzeData(ReportParams reportParams);
}
