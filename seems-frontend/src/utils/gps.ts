export class CoordinateBean {
  china: boolean;

  latitude: number;

  longitude: number;

  constructor() {
    this.china = true;
    this.latitude = 0;
    this.longitude = 0;
  }

  setChina(china: boolean) {
    this.china = china;
  }

  setLatitude(latitude: number) {
    this.latitude = latitude;
  }

  setLongitude(longitude: number) {
    this.longitude = longitude;
  }
}
function transformLat(lng: number, lat: number): number {
  let ret: number =
    -100.0 +
    2.0 * lng +
    3.0 * lat +
    0.2 * lat * lat +
    0.1 * lng * lat +
    0.2 * Math.sqrt(Math.abs(lng));
  ret +=
    ((20.0 * Math.sin(6.0 * lng * Math.PI) +
      20.0 * Math.sin(2.0 * lng * Math.PI)) *
      2.0) /
    3.0;
  ret +=
    ((20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin((lat / 3.0) * Math.PI)) *
      2.0) /
    3.0;
  ret +=
    ((160.0 * Math.sin((lat / 12.0) * Math.PI) +
      320 * Math.sin((lat * Math.PI) / 30.0)) *
      2.0) /
    3.0;
  return ret;
}
function transformLon(x: number, y: number): number {
  let ret: number =
    300.0 +
    x +
    2.0 * y +
    0.1 * x * x +
    0.1 * x * y +
    0.1 * Math.sqrt(Math.abs(x));
  ret +=
    ((20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) *
      2.0) /
    3.0;
  ret +=
    ((20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin((x / 3.0) * Math.PI)) *
      2.0) /
    3.0;
  ret +=
    ((150.0 * Math.sin((x / 12.0) * Math.PI) +
      300.0 * Math.sin((x / 30.0) * Math.PI)) *
      2.0) /
    3.0;
  return ret;
}
function outOfChina(lat: number, lon: number): boolean {
  return lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271;
}
const A = 6378245.0;
const EE = 0.006693421622965943;
export function wgs84ToGcj02(lat: number, lon: number): CoordinateBean {
  const info = new CoordinateBean();
  if (outOfChina(lat, lon)) {
    info.setChina(false);
    info.setLatitude(lat);
    info.setLongitude(lon);
  } else {
    let dLat = transformLat(lon - 105.0, lat - 35.0);
    let dLon = transformLon(lon - 105.0, lat - 35.0);
    const radLat = (lat / 180.0) * Math.PI;
    let magic = Math.sin(radLat);
    magic = 1 - EE * magic * magic;
    const sqrtMagic = Math.sqrt(magic);
    dLat = (dLat * 180.0) / (((A * (1 - EE)) / (magic * sqrtMagic)) * Math.PI);
    dLon = (dLon * 180.0) / ((A / sqrtMagic) * Math.cos(radLat) * Math.PI);
    const mgLat = lat + dLat;
    const mgLon = lon + dLon;
    info.setChina(true);
    info.setLatitude(mgLat);
    info.setLongitude(mgLon);
  }
  return info;
}
