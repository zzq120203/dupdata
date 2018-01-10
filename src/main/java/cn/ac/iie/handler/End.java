package cn.ac.iie.handler;

import cn.ac.iie.common.RPoolProxy;
import cn.ac.iie.configs.Config;
import cn.ac.iie.service.DupData;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.concurrent.atomic.AtomicInteger;

public class End implements WorkHandler<DupData>, LifecycleAware {
    private static Logger log = LoggerFactory.getLogger(End.class);

    private String oldName;
    private final String name = "End-";
    private static AtomicInteger threadId = new AtomicInteger(0);

    private RPoolProxy rpp = null;

    public End(RPoolProxy rpp) {
        this.rpp = rpp;
    }

    @Override
    public void onEvent(DupData data) throws Exception {
        Jedis jedis = null;
        try {
            if (Config.endHandler) {
                if (!data.isOK()) {
                    jedis = rpp.rpL1.getResource();
                    if (jedis != null) {
                        jedis.rpush(data.getSet(), data.getAesKey()
                                + "," + data.getU_ch_id()
                                + "," + data.getM_chat_room()
                                + "," + data.getM_ch_id());
                    }
                    log.debug("End ====>> key:{}@{} is not insert mms", data.getSet(), data.getAesKey());
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
