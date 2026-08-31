package faang.school.urlshortenerservice.service;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HostAddressResolverTest {

    private final HostAddressResolver resolver = new HostAddressResolver();

    @Test
    void resolve_shouldStripBrackets_whenIpv6Literal() throws Exception {
        InetAddress[] addresses = resolver.resolve("[::1]");

        assertEquals("0:0:0:0:0:0:0:1", addresses[0].getHostAddress());
    }

    @Test
    void resolve_shouldResolvePlainIpv4Literal() throws Exception {
        InetAddress[] addresses = resolver.resolve("203.0.113.10");

        assertArrayEquals(new InetAddress[]{InetAddress.getByName("203.0.113.10")}, addresses);
    }

    @Test
    void resolve_shouldPropagateUnknownHost() throws Exception {
        // A syntactically valid but unresolvable IPv4 literal never resolves.
        assertThrows(UnknownHostException.class, () -> resolver.resolve("256.0.0.1"));
    }
}
