package com.yld.hx.newyearparty.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class CommonUtils {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
	
	/**
	 * 计算两点之间的距离
	 * @param firstLongitude
	 * @param firstLatitude
	 * @param secondLongitude
	 * @param secondLatitude
	 * @return
	 */
	public static Double calcDistance(double firstLongitude, double firstLatitude, double secondLongitude, double secondLatitude) {
		double DISTANCE_EQUAL = 1e-7; // 距离常量
		double RC = 6378137; // 赤道半径
		double PI_RADIAN = Math.PI / 180d;
		if (Math.abs(firstLongitude - secondLongitude) <= DISTANCE_EQUAL
				&& Math.abs(firstLatitude - secondLatitude) <= DISTANCE_EQUAL) {
			return 0d;
		}
		double x1 = firstLatitude * PI_RADIAN;
		double x2 = secondLatitude * PI_RADIAN;
		double y1 = firstLongitude * PI_RADIAN;
		double y2 = secondLongitude * PI_RADIAN;
		double d = RC * Math.acos(Math.sin(x1) * Math.sin(x2) + Math.cos(x1) * Math.cos(x2) * Math.cos(y1 - y2));
		return d;
	}
	
	/**
	 * 计算是否在指定距离附近
	 * @param radius 距离（单位/米）
	 * @param lat1 纬度 
	 * @param lng1 经度
	 * @param lat21 纬度 
	 * @param lng21 经度
	 * @return 是否在
	 */
	public static boolean isInCircle(double radius, String lat1, String lng1, String lat2, String lng2) {
		Double calcDistance = CommonUtils.calcDistance(Double.parseDouble(lng1), Double.parseDouble(lat1), Double.parseDouble(lng2), Double.parseDouble(lat2));
		if (calcDistance > radius) {// 不在附近
			return false;
		} else {
			return true;
		}
	}

	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}
	
	/**
	 * 获取时间（yyyy-MM-dd HH:mm:ss）
	 * @return
	 */
	public static String getDate() {
		return sdf.format(new Date());
	}
	
	public static void main(String[] args) {
		System.out.println(CommonUtils.isInCircle(500, "40.0780262760", "116.2277394533", "40.0747793943", "116.2299442291"));
	}
}
