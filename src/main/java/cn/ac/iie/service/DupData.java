package cn.ac.iie.service;

public class DupData {

    private String set;
    private String aesKey;
    private long u_ch_id;
    private long m_chat_room;
    private String m_ch_id;

    private boolean OK;

    public DupData() {
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public long getU_ch_id() {
        return u_ch_id;
    }

    public void setU_ch_id(long u_ch_id) {
        this.u_ch_id = u_ch_id;
    }

    public long getM_chat_room() {
        return m_chat_room;
    }

    public void setM_chat_room(long m_chat_room) {
        this.m_chat_room = m_chat_room;
    }

    public String getM_ch_id() {
        return m_ch_id;
    }

    public void setM_ch_id(String m_ch_id) {
        this.m_ch_id = m_ch_id;
    }

    public boolean isOK() {
        return OK;
    }

    public void setOK(boolean OK) {
        this.OK = OK;
    }

    @Override
    public String toString() {
        return "DupData{" +
                "set='" + set + '\'' +
                ", aesKey='" + aesKey + '\'' +
                ", u_ch_id=" + u_ch_id +
                ", m_chat_room=" + m_chat_room +
                ", m_ch_id='" + m_ch_id + '\'' +
                ", OK=" + OK +
                '}';
    }

    public void resetAllFields() {
        this.set = null;
        this.aesKey = null;
        this.u_ch_id = 0;
        this.m_chat_room = 0;
        this.m_ch_id = null;
        this.OK = false;
    }
}
