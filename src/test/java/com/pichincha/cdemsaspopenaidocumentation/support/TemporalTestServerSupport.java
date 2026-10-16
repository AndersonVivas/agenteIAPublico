package com.pichincha.cdemsaspopenaidocumentation.support;

import io.temporal.testserver.TestServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ActiveProfiles("it")
public abstract class TemporalTestServerSupport {

  private static final int PORT = freePort();

  static {
    TestServer.PortBoundTestServer server = TestServer.createPortBoundServer(PORT);
    Runtime.getRuntime().addShutdownHook(new Thread(server::close));
  }

  @DynamicPropertySource
  static void temporalServer(DynamicPropertyRegistry registry) {
    registry.add("app.temporal.server-target", () -> "localhost:" + PORT);
  }

  private static int freePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
