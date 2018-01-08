package cn.ac.iie.tools.kw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeywordFilterHandler {
    private final static Logger log = LoggerFactory.getLogger(KeywordFilterHandler.class);

    private static Map<Long, LogicSyntaxTree> LST = new HashMap<Long, LogicSyntaxTree>();
    private static Map<Long, LogicSyntaxTree> tmp = null;

    private static Map<Long, Map<Long, byte[]>> ruleIdToZhutiid = new HashMap<Long, Map<Long, byte[]>>();
    private static Map<Long, Map<Long, byte[]>> ruleIdToZhuantiid = new HashMap<Long, Map<Long, byte[]>>();

    private static Map<Long, Map<Long, byte[]>> tmpRIDToZhutiid = null;
    private static Map<Long, Map<Long, byte[]>> tmpRIDToZhuantiid = null;

    private static AtomicBoolean updatedMap = new AtomicBoolean(false);
    private static final byte[] lockOb = new byte[0];

    public static KeyWordList kwFilter(String text) {
        KeyWordList kwList = new KeyWordList();
        try {
            if (text != null ) {
                synchronized (lockOb) {
                    if (updatedMap.get()) {
                        LST.clear();
                        LST = tmp;

                        ruleIdToZhutiid.clear();
                        ruleIdToZhutiid = tmpRIDToZhutiid;
                        ruleIdToZhuantiid.clear();
                        ruleIdToZhuantiid = tmpRIDToZhuantiid;

                        tmp = null;
                        tmpRIDToZhuantiid = null;
                        tmpRIDToZhutiid = null;
                        updatedMap.set(false);
                    }
                }
                List<Long> u_list = new ArrayList<Long>();
                Map<Long, Byte> zhutiMap = new HashMap<Long, Byte>();
                Map<Long, Byte> zhuantiMap = new HashMap<Long, Byte>();
                for (Map.Entry<Long, LogicSyntaxTree> en : LST.entrySet()) {
                    if (en.getValue().containsVerify(text)) {
                        u_list.add(en.getKey());
                        kwList.setTarget(true);
                        if (ruleIdToZhuantiid.containsKey(en.getKey())) {
                            for (Map.Entry<Long, byte[]> ent : ruleIdToZhuantiid.get(en.getKey()).entrySet()) {
                                zhuantiMap.put(ent.getKey(), (byte) 0);
                            }
                        }
                        if (ruleIdToZhutiid.containsKey(en.getKey())) {
                            for (Map.Entry<Long, byte[]> ent : ruleIdToZhutiid.get(en.getKey()).entrySet()) {
                                zhutiMap.put(ent.getKey(), (byte) 0);
                            }
                        }
                    }
                }

                if (u_list.size() > 0) {
                    kwList.addRulesList(u_list);

                    List<Long> tpList = new ArrayList<Long>();
                    for (Map.Entry<Long, Byte> en : zhuantiMap.entrySet()) {
                        tpList.add(en.getKey());
                    }
                    kwList.addTopicsList(tpList);

                    List<Long> tList = new ArrayList<Long>();
                    for (Map.Entry<Long, Byte> en : zhutiMap.entrySet()) {
                        tList.add(en.getKey());
                    }
                    kwList.addThemesList(tList);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return kwList;
    }

    public static void setLSTMap(Map<Long, LogicSyntaxTree> tmpMap, Map<Long, Map<Long, byte[]>> ruleIdToZhutiid,
                                 Map<Long, Map<Long, byte[]>> ruleIdToZhuantiid) {
        synchronized (lockOb) {
            if (tmpRIDToZhutiid == null) {
                tmpRIDToZhutiid = new HashMap<Long, Map<Long, byte[]>>();
            }

            for (Map.Entry<Long, Map<Long, byte[]>> en : ruleIdToZhutiid.entrySet()) {
                Map<Long, byte[]> tmpLongTobyte = new HashMap<Long, byte[]>();
                for (Map.Entry<Long, byte[]> ent : en.getValue().entrySet()) {
                    tmpLongTobyte.put(ent.getKey(), new byte[0]);
                }
                tmpRIDToZhutiid.put(en.getKey(), tmpLongTobyte);
            }

            if (tmpRIDToZhuantiid == null) {
                tmpRIDToZhuantiid = new HashMap<Long, Map<Long, byte[]>>();
            }

            for (Map.Entry<Long, Map<Long, byte[]>> en : ruleIdToZhuantiid.entrySet()) {
                Map<Long, byte[]> tmpLongTobyte = new HashMap<Long, byte[]>();
                for (Map.Entry<Long, byte[]> ent : en.getValue().entrySet()) {
                    tmpLongTobyte.put(ent.getKey(), new byte[0]);
                }
                tmpRIDToZhuantiid.put(en.getKey(), tmpLongTobyte);
            }

            if (tmp == null) {
                tmp = new HashMap<Long, LogicSyntaxTree>();
            }
            for (Map.Entry<Long, LogicSyntaxTree> en : tmpMap.entrySet()) {
                tmp.put(en.getKey(), en.getValue().cloneObject());
            }
            updatedMap.set(true);
        }
    }
}
