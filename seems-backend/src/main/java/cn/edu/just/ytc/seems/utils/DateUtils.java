package cn.edu.just.ytc.seems.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static Date getDateDaysBefore(Date date, int daysBefore) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // 将 Calendar 的时间设置为传入的日期
        calendar.add(Calendar.DATE, -daysBefore); // 减去 daysBefore 天
        return calendar.getTime(); // 返回新的日期对象
    }
    /**
     * 根据年份计算总周数
     * @param year
     * @return {int}
     */
    public static int getNumOfWeeks(int year) {
        // 获取该年1月1日的日期
        LocalDate startDateOfYear = LocalDate.of(year, 1, 1);

        // 获取该年12月31日的日期
        LocalDate endDateOfYear = LocalDate.of(year, 12, 31);

        // 计算两者之间的周数
        return (int) ChronoUnit.WEEKS.between(startDateOfYear, endDateOfYear) + 1;
    }

    /**
     * 时间转换成字符串
     * @param date
     * @return {String}
     */
    public static String getNowFormatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日");
        return date.format(formatter);
    }

    /**
     * 根据上周日获取这周日的日期范围
     * @param lastSunday
     * @return {String}
     */
    public static String getDateRange(LocalDate lastSunday) {
        if (lastSunday == null) {
            return "";
        }

        // 获取下周一的日期
        LocalDate beginDate = lastSunday.plusDays(1);
        // 获取周日的日期
        LocalDate endDate = lastSunday.plusDays(7);

        return getNowFormatDate(beginDate) + "-" + getNowFormatDate(endDate);
    }

    /**
     * 获取该日期所在的周数
     * @param date
     * @return {int}
     */
    public static int getWeekNumber(LocalDate date) {
        // 获取当前日期所在年的1月1日
        LocalDate startDate = LocalDate.of(date.getYear(), 1, 1);

        // 计算日期与1月1日之间的天数
        long daysBetween = ChronoUnit.DAYS.between(startDate, date);

        // 计算并返回周数
        return (int) (daysBetween / 7) + 1;
    }
}
