package cn.ac.iie.handler;

import cn.ac.iie.common.RPoolProxy;
import cn.ac.iie.configs.Config;
import cn.ac.iie.service.DupData;
import cn.ac.iie.tools.DBUtils;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.concurrent.atomic.AtomicInteger;

public class TextUpData implements WorkHandler<DupData>, LifecycleAware {
    private static Logger log = LoggerFactory.getLogger(TextUpData.class);

    private String oldName;
    private final String name = "TextUpData-";
    private static AtomicInteger threadId = new AtomicInteger(0);

    private RPoolProxy rpp = null;

    public TextUpData(RPoolProxy rpp) {
        this.rpp = rpp;
    }

    @Override
    public void onEvent(DupData data) throws Exception {
        Jedis jedis = null;
        try {
            if (Config.textUpData) {
                jedis = rpp.rpL1.getResource();
                if (jedis != null) {
                    String text = jedis.hget("T" + data.getSet(), data.getAesKey());
                    if (text != null) {
                        if (!"0".equals(text)) {
                            DBUtils.updateMPPTextFromKey(text, data.getU_ch_id(), data.getM_chat_room(), data.getM_ch_id());
                        }
                        log.info("Text ====>> text:{}; key:{}@{}", text, data.getSet(), data.getAesKey());
                        data.setOK(true);
                    } else {
                        log.debug("Text ====>> text:{}; key:{}@{}", null, data.getSet(), data.getAesKey());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            rpp.rpL1.putInstance(jedis);
        }
    }
    @Override
    public void onStart() {
        final Thread currentThread = Thread.currentThread();
        oldName = currentThread.getName();
        currentThread.setName(name + threadId.addAndGet(1));
    }

    @Override
    public void onShutdown() {
        Thread.currentThread().setName(oldName);
    }
}
