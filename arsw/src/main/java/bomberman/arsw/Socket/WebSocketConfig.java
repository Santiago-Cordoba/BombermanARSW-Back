package bomberman.arsw.Socket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Configuración del broker Redis
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("bombermanCache.redis.cache.windows.net")
                .setRelayPort(6380)
                .setClientLogin("default")
                .setClientPasscode("Mn5XVKNOBncGIYRpPpWldQ1vEEdDmxq2GAzCaH2I7zU=")
                .setSystemLogin("default")
                .setSystemPasscode("Mn5XVKNOBncGIYRpPpWldQ1vEEdDmxq2GAzCaH2I7zU=")
                .setAutoStartup(true);

        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new DefaultHandshakeHandler(new TomcatRequestUpgradeStrategy()))
                .withSockJS(); // Opcional: para compatibilidad con navegadores antiguos
    }
}