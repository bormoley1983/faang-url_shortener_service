package faang.school.urlshortenerservice.service;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class HostAddressResolver {

    public InetAddress[] resolve(String host) throws UnknownHostException {
        String addressHost = host;
        if (host.startsWith("[") && host.endsWith("]")) {
            addressHost = host.substring(1, host.length() - 1);
        }
        return InetAddress.getAllByName(addressHost);
    }
}
