package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.pojo.dto.TugMonthlyReportDTO;
import cn.edu.just.ytc.seems.pojo.dto.TugMonthlyReportQuery;

/**
 * 拖轮月度报表服务接口
 */
public interface TugMonthlyReportService {

    /**
     * 生成月度报表
     * @param query 查询参数
     * @return 月度报表数据
     */
    TugMonthlyReportDTO generateMonthlyReport(TugMonthlyReportQuery query);
}
