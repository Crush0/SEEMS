const durationTime2Str = (durationTime: number) => {
  const hours = Math.floor(durationTime / 3600);
  const minutes = Math.floor((durationTime % 3600) / 60);
  const seconds = durationTime % 60;
  return `${hours}时 ${minutes}分 ${seconds.toFixed(0)}秒`;
};

const timeformat = (time: string) => {
  const date = new Date(time);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hour = date.getHours();
  const minute = date.getMinutes();
  // const second = date.getSeconds();
  return `${month < 10 ? `0${month}` : month}-${day < 10 ? `0${day}` : day} ${
    hour < 10 ? `0${hour}` : hour
  }:${minute < 10 ? `0${minute}` : minute}`;
};

/**
 * 根据年份计算总周数
 * @param year
 * @returns {number}
 */
function getNumOfWeeks(year) {
  // 设置为这一年开始日期
  const startDateOfYear = new Date(year, 0, 1);
  // 计算这一年有多少天
  const daysOfYear =
    (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0 ? 366 : 365;
  // 366（365）/7=52.2(52.1)，所以一般一年有52周余1天或者2天，当这一年有366天时且第一天是周日，那么他的最后一天则是周一，这一年就有54周。
  let weekNum = 53;
  // 当年份是闰年且第一天是周日时有54周
  if (startDateOfYear.getDay() === 0 && daysOfYear === 366) {
    weekNum = 54;
  }
  return weekNum;
}

/**
 * 时间转换成字符串
 * @param date
 * @returns {string}
 */
function getNowFormatDate(date) {
  let Month = 0;
  let Day = 0;
  let CurrentStr = '';
  // 初始化时间
  Month = date.getMonth() + 1;
  Day = date.getDate();
  if (Month >= 10) {
    CurrentStr += `${Month}月`;
  } else {
    CurrentStr += `0${Month}月`;
  }
  if (Day >= 10) {
    CurrentStr += `${Day}日`;
  } else {
    CurrentStr += `0${Day}日`;
  }
  return CurrentStr;
}

/**
 * 根据上周日获取这周日的日期范围
 * @param lastSunday
 * @returns {string}
 */
function getDateRange(lastSunday) {
  if (lastSunday == null || lastSunday === '') {
    return '';
  }
  const beginDate = new Date(lastSunday.setDate(lastSunday.getDate() + 1));
  const endDate = new Date(lastSunday.setDate(lastSunday.getDate() + 6));
  return `${getNowFormatDate(beginDate)}-${getNowFormatDate(endDate)}`;
}

function getWeekNumber(date) {
  // 获取年份
  const year = date.getFullYear();
  // 创建该年1月1日的日期对象，假设1月1日为周一（实际可能需要根据具体情况调整起始周的计算）
  const startDate = new Date(year, 0, 1);
  // 计算输入日期与1月1日的时间差（单位为毫秒）
  const diff = date.getTime() - startDate.getTime();
  // 将时间差转换为天数
  const diffDays = Math.ceil(diff / (1000 * 60 * 60 * 24));
  // 计算周数，向上取整
  return Math.ceil(diffDays / 7) - 1;
}

export {
  durationTime2Str,
  timeformat,
  getNumOfWeeks,
  getDateRange,
  getNowFormatDate,
  getWeekNumber,
};
