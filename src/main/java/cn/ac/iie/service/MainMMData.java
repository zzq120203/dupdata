package cn.ac.iie.service;

import cn.ac.iie.common.RPoolProxy;
import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainMMData implements Runnable {

    private static Logger log = LoggerFactory.getLogger(MainMMData.class);
    private RingBuffer<DupData> ringBuffer;

    public static AtomicBoolean startGetMainMMData = new AtomicBoolean(true);

    private RPoolProxy rpp = null;

    public MainMMData(RingBuffer<DupData> ringBuffer, RPoolProxy rpp) {
        this.ringBuffer = ringBuffer;
        this.rpp = rpp;
    }

    public static String getTimeLong(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String str = sdf.format(date);
        str += " 00:00:01";
        return str;
    }

    @Override
    public void run() {
        Jedis jedis = null;
        long seq = 0;
        DupData dupData;
        String[] tags = {"i", "v"};
        do {
            try {
                jedis = rpp.rpL1.getResource();
                if (jedis != null) {
                    Date date = new Date();
                    long ts = Timestamp.valueOf(getTimeLong(date)).getTime() / 1000;
                    String info = null;
                    for (int i = 0; i > -86401; i -= 86400) {
                        ts += i;
                        do {
                            for (String tag : tags) {
                                info = jedis.lpop(tag + ts);
                                if (info != null) {
                                    String[] infos = info.split(",");
                                    try {
                                        seq = ringBuffer.next();
                                        dupData = ringBuffer.get(seq);
                                        dupData.resetAllFields();
                                        dupData.setSet(tag + ts);
                                        dupData.setAesKey(infos[0]);
                                        dupData.setU_ch_id(Long.parseLong(infos[1]));
                                        dupData.setM_chat_room(Long.parseLong(infos[2]));
                                        dupData.setM_ch_id(infos[3]);
                                    } finally {
                                        ringBuffer.publish(seq);
                                    }
                                    log.info(dupData.toString());
                                }
                            }
                        } while (info != null);
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                rpp.rpL1.putInstance(jedis);
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } while (startGetMainMMData.get());
    }

    public static void main(String[] args) {
        Date date = new Date();
        long ts = Timestamp.valueOf(getTimeLong(date)).getTime() / 1000;
        System.out.println(ts);
    }
}
