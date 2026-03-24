package io.github.hzahnlei.infra.api;

import org.apache.coyote.AbstractProtocol;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

/**
 * Disables Tomcat's SO_LINGER socket option.
 * macOS + JDK 25 raises java.net.SocketException ("Invalid argument") when
 * Tomcat tries to set SO_LINGER on accepted NIO sockets.
 *
 * AbstractProtocol.getEndpoint() is protected, so SocketProperties is reached
 * via reflection. tomcat-embed-core is not a JDK module, so setAccessible(true)
 * requires no --add-opens flag.
 */
@Configuration
class TomcatConfig {

    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> soLingerCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            if (connector.getProtocolHandler() instanceof AbstractProtocol<?> proto) {
                try {
                    Field endpointField = AbstractProtocol.class.getDeclaredField("endpoint");
                    endpointField.setAccessible(true);
                    ((AbstractEndpoint<?, ?>) endpointField.get(proto))
                            .getSocketProperties()
                            .setSoLingerOn(false);
                } catch (ReflectiveOperationException ignored) {
                }
            }
        });
    }
}
