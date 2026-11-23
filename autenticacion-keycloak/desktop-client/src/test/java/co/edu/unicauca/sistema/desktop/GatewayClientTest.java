package co.edu.unicauca.sistema.desktop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests how the client calls the gateway, using a fake gateway. */
class GatewayClientTest {

    private HttpServer fakeGateway;
    private final AtomicReference<String> receivedAuthorization = new AtomicReference<>();
    private final AtomicReference<String> receivedPath = new AtomicReference<>();
    private int replyStatus = 200;
    private String replyBody = "ok";

    @BeforeEach
    void startFakeGateway() throws IOException {
        fakeGateway = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        fakeGateway.createContext("/", exchange -> {
            receivedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            receivedPath.set(exchange.getRequestURI().getPath());
            byte[] body = replyBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(replyStatus, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        fakeGateway.start();
    }

    @AfterEach
    void stopFakeGateway() {
        fakeGateway.stop(0);
    }

    private GatewayClient client() {
        return new GatewayClient(new ClientConfig("unused",
                "http://127.0.0.1:" + fakeGateway.getAddress().getPort(), "sistema-desktop"));
    }

    @Test
    @DisplayName("The token travels as 'Authorization: Bearer <token>'")
    void sendsBearerToken() throws IOException {
        replyBody = "Hello student from microservice-a";

        ApiResponse response = client().get("my.jwt.token", "/a/hello_student");

        assertThat(receivedAuthorization.get()).isEqualTo("Bearer my.jwt.token");
        assertThat(receivedPath.get()).isEqualTo("/a/hello_student");
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Hello student from microservice-a");
    }

    @Test
    @DisplayName("Without a token no Authorization header is sent")
    void omitsHeaderWithoutToken() throws IOException {
        replyStatus = 401;
        replyBody = "";

        ApiResponse response = client().get(null, "/a/hello_all");

        assertThat(receivedAuthorization.get()).isNull();
        assertThat(response.status()).isEqualTo(401);
    }

    @Test
    @DisplayName("403 is returned to the caller, not thrown")
    void forbiddenIsReturned() throws IOException {
        replyStatus = 403;
        replyBody = "";

        assertThat(client().get("t.t.t", "/a/hello_coordinator").status()).isEqualTo(403);
    }

    @Test
    @DisplayName("Gateway down raises IOException")
    void unreachableGatewayThrows() {
        GatewayClient unreachable = new GatewayClient(new ClientConfig("unused", "http://127.0.0.1:1", "sistema-desktop"));

        assertThatThrownBy(() -> unreachable.get("t.t.t", "/a/hello_all")).isInstanceOf(IOException.class);
    }
}
