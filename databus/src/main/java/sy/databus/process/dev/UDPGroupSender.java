package sy.databus.process.dev;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.common.util.SInetUtil;
import sy.databus.entity.message.IMessage;
import sy.databus.global.ProcessorType;
import sy.databus.global.WorkMode;
import sy.databus.organize.ExecutorManager;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.organize.monitor.DevInfoReporter;
import sy.databus.organize.monitor.MonitorGroup;
import sy.databus.process.AbstractHandler;
import sy.databus.process.AbstractMessageProcessor;
import sy.databus.process.Console;
import sy.databus.process.Processor;
import sy.databus.process.fsm.producer.*;
import sy.databus.view.watch.OutputProcessorWatchPane;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

import static sy.databus.process.Console.Config.STATIC;

@Log4j2
@Processor(
        type = ProcessorType.UDP_MULTICAST,
        pane = OutputProcessorWatchPane.class
)
public class UDPGroupSender extends AbstractMessageProcessor<ByteBuf> {

    @Getter @Setter
    @Console(config = STATIC, display = "IP地址")
    private String host = "192.168.32.1";


    @Setter @Getter
    @Console(config = STATIC, display = "组地址")
    private String groupAddr = "226.0.0.86";


    @Setter @Getter
    @Console(config = STATIC, display = "端口号")
    private int port = 19001;

    public UDPGroupSender() {
    }

    public UDPGroupSender(String host, String groupAddr, int port) {
        this.host = host;
        this.groupAddr = groupAddr;
        this.port = port;
    }

    @Getter
    private Channel channel;

    private InetSocketAddress groupAddress;

    private NetworkInterface ni;

    @Getter
    private final ProducerCommonCondition condition = new ProducerCommonCondition(
            new RUNNABLE(
                    () -> { // toOpen

                    },
                    () -> { // toShutdown
                        // do nothing
                        setClosingFuture(channel.close());
                    }),
            new RUNNING(
                    () -> { // toPark

                    },
                    () -> { // toClose

                    },
                    () -> { // toShutdown
                        setClosingFuture(channel.close());
                    }
            ),
            new SUSPENDED(
                    () -> { // toOpen

                    },
                    () -> { // toClose

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


    @Override
    public void initialize() {
        super.initialize();

        setTailHandler(new AbstractHandler<>() {
            @Override
            public void initialize() {}

            @Override
            public void handle(IMessage<ByteBuf> msg) throws Exception {
                outBoundedStatistic(msg);
                channel.writeAndFlush(new DatagramPacket(msg.getData(), groupAddress));
            }
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
            bootstrap.group(ExecutorManager.allocateEventLoopGroup())
                    .channelFactory(new ChannelFactory<Channel>() {
                        @Override
                        public Channel newChannel() {
                            return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                        }
                    })
                    .localAddress(localAddress, groupAddress.getPort())
                    .option(ChannelOption.IP_MULTICAST_IF, ni)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .handler(new ChannelOutboundHandlerAdapter());
            var future = bootstrap.bind().sync();
            condition.setFuture(future);
            channel = future.channel();
            condition.switchToRunnable();
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public void boot() throws Exception {
        super.boot();
        bootDev();
    }

    @Override
    protected AbstractInfoReporter createInfoReporter() {
        return new DevInfoReporter(this, MonitorGroup.OUT_DEV) {
            @Override
            public String updateDevInfo() {
                return "ip:" + host + " 组:" + groupAddr + " port:" + port;
            }
        };
    }
}
