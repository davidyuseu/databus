package sy.common.util;

import lombok.extern.log4j.Log4j2;

import java.net.*;
import java.util.Optional;

@Log4j2
public class SInetUtil {

    public record InetInfo (InetAddress inetAddress, NetworkInterface ni){}

    public static InetInfo getAddressByName(String host) throws Exception {
        NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(host));

        if (ni == null)
            throw new Exception("未找到IP或网卡状态异常！");

        Optional<InterfaceAddress> infOptional = ni.getInterfaceAddresses().stream().filter(interfaceAddress -> {
            InetAddress address = interfaceAddress.getAddress();
            return (address instanceof Inet4Address && address.getHostAddress().equals(host));
        }).findFirst();

        if (infOptional.isPresent()) {
            return new InetInfo(infOptional.get().getAddress(), ni);
        } else {
            throw new Exception("未找到IP地址，请检查网卡配置！");
        }
    }
}
