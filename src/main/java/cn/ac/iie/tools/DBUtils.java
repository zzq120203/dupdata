package cn.ac.iie.tools;

import cn.ac.iie.tools.kw.KeyWordList;
import cn.ac.iie.tools.kw.KeywordFilterHandler;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtils {

    private static Logger log = LoggerFactory.getLogger(DBUtils.class);
    private static Gson gson = new Gson();
    private static String formatting(String str) {
        return str.replaceAll("['′`\"”″“\\\\]", "");
    }

    public static boolean updateMPPTextFromKey(String text, long u_ch_id, long m_chat_room, String m_ch_id) {
        text = formatting(text);
        String sql = null;
        Connection dbConn = null;
        Statement statement = null;


        try {
            dbConn = MppJdbcPool_dbcp.getConnection();
            long simHash = SimHashTools.genSimHashCode(text);
            sql = "update tp_wxq_media" +
                    " set m_mm_audio_txt = '" + text + "',m_simhash = " + simHash +
                    " where m_chat_room = " + m_chat_room + " and m_ch_id = '" + m_ch_id + "' and u_ch_id = " + u_ch_id +
                    " and m_mm_audio_txt is null";
            statement = dbConn.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            log.error("SQL:{}; ", sql);
            mppClose(dbConn, statement);
            return false;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("SQL:{}; ", sql);
            return false;
        } finally {
            mppExit(dbConn, statement);
        }

        updateMmsKeyWord(u_ch_id, m_chat_room, m_ch_id, text);

        return true;
    }

    public static boolean updateMppFaceFromKey(String faces, int faceSize, long u_ch_id, long m_chat_room, String m_ch_id) {
        String sql = null;
        Connection dbConn = null;
        PreparedStatement pStmt = null;

        try {
            dbConn = MppJdbcPool_dbcp.getConnection();

            sql = "update tp_wxq_media" +
                    " set m_mm_imgface = ?, m_mm_imgface_size = ? " +
                    " where m_chat_room = ? and m_ch_id = ? and u_ch_id = ?";
            pStmt = dbConn.prepareStatement(sql);
            pStmt.setString(1, faces);
            pStmt.setLong(2, faceSize);
            pStmt.setLong(3, m_chat_room);
            pStmt.setString(4, m_ch_id);
            pStmt.setLong(5, u_ch_id);
            pStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            log.error("SQL:{}; ", sql);
            mppClose(dbConn, pStmt);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("SQL:{}; ", sql);
        } finally {
            mppExit(dbConn, pStmt);
        }
        return true;
    }



    public static boolean updateMppFeatureFromKey(String feature, long u_ch_id, long m_chat_room, String m_ch_id) {
        String sql = "";
        Connection dbConn = null;
        Statement statement = null;
        try {
            dbConn = MppJdbcPool_dbcp.getConnection();
            sql = "update tp_wxq_media" +
                    " set m_mm_feature = ? " +
                    " where m_chat_room = ? and m_ch_id = ? and u_ch_id = ?";
            statement = dbConn.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            log.error("SQL:{}; ", sql);
            mppClose(dbConn, statement);
        } catch (Exception e) {
            log.error("SQL:{}; j", sql);
            log.error(e.getMessage(), e);
            return false;
        } finally {
            mppExit(dbConn, statement);
        }
        return true;
    }

    private static void updateMmsKeyWord(long u_ch_id, long m_chat_room, String m_ch_id, String text) {
        KeyWordList kwList = KeywordFilterHandler.kwFilter(text);

        if (!kwList.isTarget()) {
            return;
        }

        String sql = "";
        Connection dbConn = null;
        PreparedStatement pStmt = null;
        try {
            dbConn = MppJdbcPool_dbcp.getConnection();
            sql = "update tp_wxq_media" +
                    " set m_themes_list = ?, m_topics_list = ?, m_rules_list = ?" +
                    " where m_chat_room = ? and m_ch_id = ? and u_ch_id = ?";
            pStmt = dbConn.prepareStatement(sql);
            pStmt.setString(1, gson.toJson(kwList.getThemesList()));
            pStmt.setString(2, gson.toJson(kwList.getTopicsList()));
            pStmt.setString(3, gson.toJson(kwList.getRulesList()));
            pStmt.setLong(4, m_chat_room);
            pStmt.setString(5, m_ch_id);
            pStmt.setLong(6, u_ch_id);
            pStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            log.error("SQL:{}; kwList:{}; ", sql, kwList.toString());
            mppClose(dbConn, pStmt);
        } catch (Exception e) {
            log.error("SQL:{}; kwList:{}; ", sql, kwList.toString());
            log.error(e.getMessage(), e);
        } finally {
            mppExit(dbConn, pStmt);
        }

    }


    private static void mppExit(Connection dbConn, Statement pStmt) {
        try {
            if (pStmt != null) {
                pStmt.close();
            }
            MppJdbcPool_dbcp.closeConn(dbConn);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void mppClose(Connection dbConn, Statement pStmt) {
        try {
            if (pStmt != null)
                pStmt.close();
            if (dbConn != null)
                dbConn.close();
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) throws SQLException {
    }
}

