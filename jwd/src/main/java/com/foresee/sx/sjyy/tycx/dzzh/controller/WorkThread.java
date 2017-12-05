package com.foresee.sx.sjyy.tycx.dzzh.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;

import com.foresee.fbrp.util.xml.VtdXmlOperateDelegate;
import com.foresee.sx.sjyy.utils.WebSpider;

/**
 * 
 * @ClassName: WorkThread
 * @author: Hoboson
 * @date: 2017年11月1日 下午2:17:38
 */
public class WorkThread implements Runnable {
	
	private String city;
	private String zgswjgdm;

	public WorkThread(String city, String zgswjgdm) {
		this.city = city;
		this.zgswjgdm = zgswjgdm;
	}

	public WorkThread(String city) {
		this.city = city;
	}

	@Override
	public void run() {
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		try {
			String threadName = city + "-" + Thread.currentThread().getName();
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@10.10.8.94:1521:nfzcdb", "SJYY", "SJYY");
			stmt = conn.createStatement();
			boolean flag = true;
			while (flag) {
				String SqlSer = "  select rownum,\r\n" + "         DZZHUUID,\r\n" + "         LNG,\r\n"
						+ "         LAT,\r\n" + "         ADDRESS,\r\n" + "         ADDRESS_1,\r\n"
						+ "         NSRID,\r\n" + "         CZSJ,\r\n" + "         YXBZ,\r\n" + "         STATUS,\r\n"
						+ "         PRECISE,\r\n" + "         CONFIDENCE,\r\n" + "         LEV,\r\n"
						+ "         CITY\r\n" + "    from SJYY.SJ_DZZH_Test01\r\n" + "   where 1=1  \r\n"
						+ "     and city = '" + this.city + "'\r\n" /*+ "     and zgswj_dm = '" + this.zgswjgdm + "'\r\n"*/
/*						+ "      and (STATUS  not  in('0','1','2','3') or STATUS is null ) \r\n"
*/						+ "      and STATUS  ='99' \r\n"
						+ "     and rownum   < 10000 \r\n";
				// System.out.println("SqlSer=="+SqlSer);
				rs = stmt.executeQuery(SqlSer);
				int count = 0;
				while (rs.next()) {
					++count;
					// 1每次一万条数据 2 先根据注册地址转换 失败 根据生产经营地址转 失败 则认为其数据有问题将数据修改为 N
					final WebSpider sp = new WebSpider();
					final String addressname = rs.getString("address");// &city=西安市 地址过滤
					if (StringUtils.isEmpty(addressname))
						continue;
					final String djxh = rs.getString("dzzhuuid");
					final String city = rs.getString("city");
					final String addressname1 = rs.getString("address_1");// &city=西安市 地址过滤
					String content = sp.getLngandLatInfo(WebSpider.StringFilter(addressname), city);
					VtdXmlOperateDelegate vtd = new VtdXmlOperateDelegate(content);
					String precise = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/precise");
					String status = vtd.getNodeDataByXpath("//GeocoderSearchResponse/status");
					if ("0".equals(status)) {
						String updateSql = null;
						if ("1".equals(precise) || !getisNullOrEmpty(addressname1)) {// 成功或者生产经营地址为空则直接用注册地址结果
							String lng = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/location/lng");
							String lat = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/location/lat");
							String level = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/level");
							String confidence = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/confidence");
							updateSql = "update SJYY.SJ_DZZH_Test01 set" + "	LNG = '" + lat + "'" + " , LAT = '"
									+ lng + "'" + "	, STATUS = '" + status + "'" + "	, PRECISE = '" + precise + "'"
									+ "	, CONFIDENCE ='" + confidence + "'" + "	, LEV ='" + level + "'" + ",  YXBZ ='' "
									+ "	, INSERTDATE = sysdate " + "	where DZZHUUID ='" + djxh + "'";
							// System.out.println(threadName+":"+"注册地址SUCCESS");
						} else {// 用生产经营地址转换
							WebSpider sp1 = new WebSpider();
							String content12 = sp1.getLngandLatInfo(WebSpider.StringFilter(addressname1), city);
							VtdXmlOperateDelegate vtd1 = new VtdXmlOperateDelegate(content12);
							String precise1 = vtd1.getNodeDataByXpath("//GeocoderSearchResponse/result/precise");
							String status1 = vtd1.getNodeDataByXpath("//GeocoderSearchResponse/status");
							String lng1 = vtd1.getNodeDataByXpath("//GeocoderSearchResponse/result/location/lng");
							String lat1 = vtd1.getNodeDataByXpath("//GeocoderSearchResponse/result/location/lat");
							String level1 = vtd1.getNodeDataByXpath("//GeocoderSearchResponse/result/level");
							String confidence11 = vtd1.getNodeDataByXpath("//GeocoderSearchResponse/result/confidence");
							if ("0".equals(status1)) {// 生产经营地址成功
								updateSql = "update SJYY.SJ_DZZH_Test01 set" + "	LNG = '" + lat1 + "'"
										+ ",  YXBZ ='' " + " , LAT = '" + lng1 + "'" + "	, STATUS = '" + status1
										+ "'" + "	, PRECISE = '" + precise1 + "'" + "	, CONFIDENCE ='" + confidence11
										+ "'" + "	, LEV ='" + level1 + "'" + "	, INSERTDATE = sysdate "
										+ "	where DZZHUUID ='" + djxh + "'";
								// System.out.println(threadName+":"+"生产经营地址SUCCESS");
							} else {// 生产经营地址失败
								SwitchCase(stmt, status1, djxh);
							}
						}
						try {
							System.out.println("线程名称" + threadName + ":" + "updateSql SUCCESS====" + updateSql);
							if (null != updateSql) {
								stmt.executeUpdate(updateSql);
								Thread.sleep(100);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {// 注册地址失败
						SwitchCase(stmt, status, djxh);
					} // while end
				}
				if (count == 0) {
					flag = false;
					System.out.println(threadName + ":" + this.city + "(：" + this.zgswjgdm + ")数据跑完了。。。");
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void SwitchCase(Statement stmt, String status, String djxh) throws SQLException {
		if ("1".equals(status)) {
			System.out.println("服务器内部错误");
		} else if ("2".equals(status)) {
			System.out.println("请求参数非法");
		} else if ("3".equals(status)) {
			System.out.println("权限校验失败");
		} else if ("4".equals(status)) {
			System.out.println("配额校验失败");return;
		} else if ("302".equals(status)) {
			System.out.println("你的调用次数已用完请申请更大次数");	return;
		} else if ("5".equals(status)) {
			System.out.println("ak不存在或者非法");return;
		} else if ("101".equals(status)) {
			System.out.println("服务禁用");return;
		} else if ("102".equals(status)) {
			System.out.println("不通过白名单或者安全码不对");return;
		} else {
			System.out.println("其他错误");return;
		}
		String updateSqlOthers = "update SJYY.SJ_DZZH_Test01 set" + "	 YXBZ ='N' " + "	, STATUS = '" + status + "'"
				+ "	, INSERTDATE = sysdate " + "	where DZZHUUID ='" + djxh + "'";
		stmt.executeUpdate(updateSqlOthers);
	}
	
	
	
	public static boolean getisNullOrEmpty(String o) {
		if ("".equals(o) || null == o) {
			return false;
		}
		return true;
	}
}
