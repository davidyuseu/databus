package sy.databus.process.dev;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.common.util.SInetUtil;
import sy.databus.entity.message.EfficientMessage;
import sy.databus.entity.message.metadata.Metadata;
import sy.databus.entity.signal.DATA_TASK_BEGIN;
import sy.databus.entity.signal.DATA_TASK_END;
import sy.databus.global.ProcessorType;
import sy.databus.global.WorkMode;
import sy.databus.organize.ExecutorManager;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.organize.monitor.DevInfoReporter;
import sy.databus.organize.monitor.MonitorGroup;
import sy.databus.process.AbstractMessageProcessor;
import sy.databus.process.Console;
import sy.databus.process.Processor;
import sy.databus.process.fsm.producer.*;
import sy.databus.view.watch.MessageSeriesProcessorWatchPane;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

import static sy.databus.process.Console.Config.STATIC;

@Log4j2
@Processor(
        type = ProcessorType.UDP_MULTICAST,
        pane = MessageSeriesProcessorWatchPane.class
)
public class UDPGroupReceiver extends AbstractMessageProcessor<ByteBuf> {


    @Setter @Getter
    @Console(config = STATIC, display = "IP地址")
    private String host = "192.168.31.1";


    @Setter @Getter
    @Console(config = STATIC, display = "组地址")
    private String groupAddr = "226.0.0.86";


    @Setter @Getter
    @Console(config = STATIC, display = "端口号")
    private int port = 19002;

    @Setter @Getter
    @Console(config = STATIC, display = "直接启动")
    private boolean bootDirectly = false;

    public UDPGroupReceiver() {}

    public UDPGroupReceiver(String host, String groupAddr, int port) {
        this.host = host;
        this.groupAddr = groupAddr;
        this.port = port;
    }

    @Getter
    private NioDatagramChannel channel;

    private InetSocketAddress groupAddress;

    private NetworkInterface ni;

    @Getter
    private final ProducerCommonCondition condition = new ProducerCommonCondition(
            new RUNNABLE(
                    () -> { // toOpen
                        joinGroup();
                    },
                    () -> { // toShutdown
                        // do nothing
                        setClosingFuture(channel.close());
                    }),
            new RUNNING(
                    () -> { // toPark
                        leaveGroup();
                    },
                    () -> { // toClose
                        leaveGroup();
                    },
                    () -> { // toShutdown
                        setClosingFuture(channel.close());
                    }
            ),
            new SUSPENDED(
                    () -> { // toOpen
                        joinGroup();
                    },
                    () -> { // toClose
                        leaveGroup();
                    },
                    () -> { // toShutdown
                        setClosingFuture(channel.close());
                    }
            ),
            new TERMINATED(
                    () -> { // toOpen
                        // 模式切换时由ExecutorManager统一分配，已分配过的不再分配
                        bootDev();
                    }
            )
    );

    private void joinGroup() {
        try {
            var future = channel.joinGroup(groupAddress, ni).sync();
            condition.setFuture(future);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private void leaveGroup() {
        try {
            var future = channel.leaveGroup(groupAddress, ni).sync();
            condition.setFuture(future);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        /** 在produce()方法中使用fireNext方法即可将生产出的消息往后转发*/
        // 生产者默认异步处理，响应START信号，将任务提交到适配的生产者执行器中
        appendSlot(DATA_TASK_BEGIN.class, signal -> {
            condition.open();
            return true;
        });
        pileUpSlot(DATA_TASK_END.class, signal -> {
            condition.close();
            return true;
        });
    }

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {
        condition.shutdown();
    }

    private void bootDev() {
        try {
            // 组播地址
            groupAddress = new InetSocketAddress(groupAddr, port);
            SInetUtil.InetInfo inetInfo = SInetUtil.getAddressByName(host);
            InetAddress localAddress = inetInfo.inetAddress();
            ni = inetInfo.ni();
            Bootstrap bootstrap = new Bootstrap();
            //设置NioDatagramChannel
            bootstrap.group(ExecutorManager.allocateEventLoopGroup())//.channel(NioDatagramChannel.class)
                    .channelFactory(new ChannelFactory<Channel>() {
                        @Override
                        public Channel newChannel() {
                            return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                        }
                    })
                    .localAddress(localAddress, groupAddress.getPort())
                    //设置Option 组播
                    .option(ChannelOption.IP_MULTICAST_IF, ni)
                    //设置Option 地址
                    .option(ChannelOption.IP_MULTICAST_ADDR, localAddress)
                    //设置地址
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(NioDatagramChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    if (msg instanceof DatagramPacket packet) {
                                        handlePacket(packet);
                                    }
                                }
                            });
                        }
                    });
            //获取NioDatagramChannel
            channel = (NioDatagramChannel) bootstrap.bind(groupAddress.getPort()).sync().channel();
            //加入组
            if (bootDirectly)
                joinGroup();
            condition.switchToRunnable();
        } catch (Exception e) {
            log.error(e);
        }
    }

    protected void handlePacket(DatagramPacket packet) throws Exception {
        EfficientMessage message = new EfficientMessage(new Metadata(CachedClock.instance().currentTimeMillis()),
                packet.content());
        inBoundedStatistic(message);
        fireNext(message);
    }

    @Override
    public void boot() throws Exception {
        super.boot();
        bootDev();
    }

    @Override
    protected AbstractInfoReporter createInfoReporter() {
        return new DevInfoReporter(this, MonitorGroup.IN_DEV) {
            @Override
            public String updateDevInfo() {
                return "ip:" + host + " 组:" + groupAddr + " port:" + port;
            }
        };
    }
}
