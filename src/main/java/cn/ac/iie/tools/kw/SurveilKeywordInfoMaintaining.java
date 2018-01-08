package cn.ac.iie.tools.kw;

import cn.ac.iie.tools.OracleJdbcPool_dbcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class SurveilKeywordInfoMaintaining implements Runnable {
    private static Logger log = LoggerFactory.getLogger(SurveilKeywordInfoMaintaining.class);

    private long lastMaxTimeStamp = 0L;
    private long lastRecordCounter = 0L;

    private String getAll = "select t.r_id as rid,t.rule as trule,t.r_update_time as updatetime,t.tp_id as tpid, t_th.t_id as tid"
            + " from t_rule t, t_topic t_p, t_theme t_th where t.rule_type=0 and t.tp_id=t_p.tp_id and t_p.tp_t_id=t_th.t_id and t_p.tp_status>0 and t.r_status>0 and t_th.t_status>0";

    private String checkUpdate = "select count(*) as counter, max(t.r_update_time) as maxdate "
            + " from t_rule t, t_topic t_p, t_theme t_th where t.rule_type=0 and t.tp_id=t_p.tp_id and t_p.tp_t_id=t_th.t_id and t_p.tp_status>0 and t.r_status>0 and t_th.t_status>0";

    @Override
    public void run() {
        Connection conn = null;
        Statement stmt = null;

        do {
            try {
                conn = OracleJdbcPool_dbcp.getConnection();
                stmt = conn.createStatement();

                boolean update = false;
                if (lastMaxTimeStamp != 0) {
                    ResultSet result = stmt.executeQuery(checkUpdate);
                    while (result.next()) {
                        if (result.getLong("counter") != lastRecordCounter
                                || result.getDate("maxdate").getTime() != lastMaxTimeStamp) {
                            update = true;
                        }
                    }
                }

                if (lastMaxTimeStamp == 0 || update) {
                    ResultSet result = stmt.executeQuery(getAll);
                    Map<Long, LogicSyntaxTree> map = new HashMap<Long, LogicSyntaxTree>();
                    Map<Long, Map<Long, byte[]>> ruleIdToZhutiid = new HashMap<Long, Map<Long, byte[]>>();
                    Map<Long, Map<Long, byte[]>> ruleIdToZhuantiid = new HashMap<Long, Map<Long, byte[]>>();
                    lastMaxTimeStamp = 0;
                    lastRecordCounter = 0;
                    while (result.next()) {
                        if (result.getDate("updatetime").getTime() > lastMaxTimeStamp) {
                            lastMaxTimeStamp = result.getDate("updatetime").getTime();
                        }
                        try {
                            map.put(result.getLong("rid"), LogicSyntaxTree.parse(result.getString("trule")));
                            Map<Long, byte[]> tmpRidToZhuantiid = ruleIdToZhuantiid.get(result.getLong("rid"));
                            Map<Long, byte[]> tmpRidToZhutiid = ruleIdToZhutiid.get(result.getLong("rid"));

                            if (tmpRidToZhuantiid == null) {
                                tmpRidToZhuantiid = new HashMap<Long, byte[]>();
                                ruleIdToZhuantiid.put(result.getLong("rid"), tmpRidToZhuantiid);
                                tmpRidToZhutiid = new HashMap<Long, byte[]>();
                                ruleIdToZhutiid.put(result.getLong("rid"), tmpRidToZhutiid);
                            }

                            tmpRidToZhuantiid.put(result.getLong("tpid"), new byte[0]);
                            tmpRidToZhutiid.put(result.getLong("tid"), new byte[0]);
                            log.info("refresh keyword, rid:{}/zhuantiid:{}/zhutiid:{}/rule:{}",
                                    result.getLong("rid"), result.getLong("tpid"), result.getLong("tid"),
                                    result.getString("trule"));
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                        lastRecordCounter++;
                    }
                    KeywordFilterHandler.setLSTMap(map, ruleIdToZhutiid, ruleIdToZhuantiid);
                }


            } catch (SQLException e) {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException ex) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    OracleJdbcPool_dbcp.closeConn(conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(2 * 10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (true);

    }

}
