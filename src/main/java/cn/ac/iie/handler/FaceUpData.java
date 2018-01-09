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

public class FaceUpData implements WorkHandler<DupData>, LifecycleAware {
    private static Logger log = LoggerFactory.getLogger(FaceUpData.class);

    private String oldName;
    private final String name = "FaceUpData-";
    private static AtomicInteger threadId = new AtomicInteger(0);

    private RPoolProxy rpp = null;

    public FaceUpData(RPoolProxy rpp) {
        this.rpp = rpp;
    }

    @Override
    public void onEvent(DupData data) throws Exception {
        Jedis jedis = null;
        try {
            if (Config.faceUpData) {
                jedis = rpp.rpL1.getResource();
                if (jedis != null) {
                    String face = jedis.hget("F" + data.getSet(), data.getAesKey());
                    if (face != null) {
                        String[] faces = face.split("@");
                        DBUtils.updateMppFaceFromKey(faces[0], Integer.parseInt(faces[1]), data.getU_ch_id(), data.getM_chat_room(), data.getM_ch_id());
                        log.info("Face ====>> face:{}; key:{}@{}", face, data.getSet(), data.getAesKey());
                    } else {
                        log.debug("Face ====>> face:{}; key:{}@{}", null, data.getSet(), data.getAesKey());
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
