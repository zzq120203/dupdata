package cn.ac.iie.service;

import cn.ac.iie.common.RPoolProxy;
import cn.ac.iie.configs.ConfLoading;
import cn.ac.iie.configs.Config;
import cn.ac.iie.handler.End;
import cn.ac.iie.handler.FaceUpData;
import cn.ac.iie.handler.TextUpData;
import cn.ac.iie.tools.kw.SurveilKeywordInfoMaintaining;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DupDataService {
    private static Logger log = LoggerFactory.getLogger(DupDataService.class);

    private static RPoolProxy rpp = new RPoolProxy(null);
    private static Disruptor<DupData> disruptor = null;

    public static void main(String[] args) {

        try {
            PropertyConfigurator.configure(System.getProperty("log4j.configuration"));//启动脚本，jvm启动参数，-Dargument=XXXXX
            ConfLoading.init(Config.class, System.getProperty("config"));//Config类成员变量赋值

            Thread surveilKW = new Thread(new SurveilKeywordInfoMaintaining(), "SurveilKeywordInfoMaintaining");
            surveilKW.start();

            while (!SurveilKeywordInfoMaintaining.isInit()) {
                Thread.sleep(1000);
            }

            try {
                if (rpp.init(Config.redisUrl, Config.redisAuthToken) != 0) {
                    log.error("Redis init failed URL:{} ", Config.redisUrl);
                    System.exit(0);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            disruptor = new Disruptor<DupData>(DupData::new, 1024, DaemonThreadFactory.INSTANCE,
                    ProducerType.MULTI, new BlockingWaitStrategy());

            disruptor.handleEventsWithWorkerPool(new TextUpData(rpp))
                    .thenHandleEventsWithWorkerPool(new FaceUpData(rpp))
                    .thenHandleEventsWithWorkerPool(new End(rpp));

            RingBuffer<DupData> ringBuffer = disruptor.start();

            Thread thread = new Thread(new MainMMData(ringBuffer, rpp), "MainMMKeyThread");
            thread.start();

            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                log.info("Execute shutdown Hook.....");
                MainMMData.startGetMainMMData.set(false);
                disruptor.shutdown();
                rpp.quit();
                log.info("DupDataService exiting......");
            }));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
}
