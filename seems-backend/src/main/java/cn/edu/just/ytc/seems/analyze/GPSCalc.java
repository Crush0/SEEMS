package cn.edu.just.ytc.seems.analyze;

import cn.edu.just.ytc.seems.pojo.entity.GPSLog;

import java.util.Date;
import java.util.List;

public class GPSCalc {

    private static final double EARTH_RADIUS = 6371393.0; // 地球半径，单位为米

    public static Long calculateTotalSailingTime(List<GPSLog> gpsLogs) {
        long totalTime = 0; // 总航行时间（毫秒）
        Date segmentStartTime = null; // 当前航段开始时间

        for (GPSLog log : gpsLogs) {
            if (log.getSpeed() != null && log.getSpeed() > 0) {
                // 航速大于0，记录开始时间
                if (segmentStartTime == null) {
                    segmentStartTime = log.getTime();
                }
            } else {
                // 航速为0，结束当前航段
                if (segmentStartTime != null) {
                    totalTime += log.getTime().getTime() - segmentStartTime.getTime(); // 计算航段时间
                    segmentStartTime = null; // 重置开始时间
                }
            }
        }

        // 处理最后一个航段（如果结束时仍然在航行）
        if (segmentStartTime != null) {
            // 这里可以选择是否考虑到当前时间的结束点
            // 假设当前时间是now
            Date now = new Date(); // 或者使用其他方式获取当前时间
            totalTime += now.getTime() - segmentStartTime.getTime();
        }

        return totalTime / 1000L; // 返回总航行时间（秒）
    }

    public static SpeedStats[] calculateSpeedStats(List<GPSLog> gpsLogs) {
        SpeedStats hoveringStats = new SpeedStats();
        SpeedStats draggingStats = new SpeedStats();

        for (GPSLog log : gpsLogs) {
            double speed = log.getSpeed();
            GPSLog.WorkStatus status = log.getWorkStatus();

            if (status == GPSLog.WorkStatus.HOVERING) {
                hoveringStats.update(speed);
            } else if (status == GPSLog.WorkStatus.DRAGGING) {
                draggingStats.update(speed);
            }
        }
        hoveringStats.getAverageSpeed();
        draggingStats.getAverageSpeed();

        return new SpeedStats[]{hoveringStats, draggingStats};
    }



    public static Double calculateTotalSailingDistance(List<GPSLog> gpsLogs) {
        double totalDistance = 0.0; // 总航行距离（公里）

        // 前一个 GPS 点
        GPSLog previousLog = null;

        for (GPSLog log : gpsLogs) {
            if (log.getSpeed() != null && log.getSpeed() > 0) {
                if (previousLog != null) {
                    // 计算当前点与前一个点之间的距离
                    totalDistance += haversine(
                            previousLog.getLatitude(),
                            previousLog.getLongitude(),
                            log.getLatitude(),
                            log.getLongitude()
                    );
                }
                // 更新前一个点
                previousLog = log;
            } else {
                // 航速为0，不更新前一个点
                previousLog = null; // 可以选择重置前一个点
            }
        }

        return totalDistance / 1000.0; // 返回总航行距离（公里）
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c; // 返回距离（米）
    }
}
