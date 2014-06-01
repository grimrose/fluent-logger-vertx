/**
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grimrose.vertx.mods;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import xerial.fluentd.FluentdConfig;
import xerial.fluentd.FluentdStandalone;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.vertx.testtools.VertxAssert.*;

public class FluentLoggerModuleIntegrationTest extends TestVerticle {

    static final String ADDRESS = FluentLoggerModuleIntegrationTest.class.getName();

    FluentdStandalone fluentd;

    @Test
    public void _string_message_sending() throws Exception {
        container.logger().info("start _string_message_sending()");

        // Setup
        JsonObject message = new JsonObject();
        message.putString("tag", "test");
        message.putObject("data", new JsonObject().putString("hoge", "fuga"));

        // Exercise
        vertx.eventBus().send(ADDRESS, message, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {

                // Verify
                assertEquals("ok", reply.body().getString("status"));

                testComplete();
            }
        });
    }

    @Test
    public void _hash_message_sending() throws Exception {
        container.logger().info("start _hash_message_sending()");

        // Setup
        JsonObject message = new JsonObject();
        message.putString("tag", "test.request");
        JsonObject data = new JsonObject().putString("id", "user1");
        data.putString("action", "get");
        data.putString("domain", "cart");
        data.putArray("param", new JsonArray().addString("param1").addString("param2"));
        message.putObject("data", data);

        // Exercise
        vertx.eventBus().send(ADDRESS, message, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {

                // Verify
                assertEquals("ok", reply.body().getString("status"));

                testComplete();
            }
        });
    }

    @Override
    public void start() {
        initialize();

        Path path = Paths.get(System.getProperty("user.dir"), ".fluentd.conf");

        int port = 24224;
        String configFile = path.toString();
        int initWait = 500;
        int maxWait = 10000;

        String workDir = null;
        try {
            workDir = Files.createTempDirectory("fluentd").toString();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        String configuration = FluentdStandalone.defaultConfig();
        final CountDownLatch latch = new CountDownLatch(1);

        FluentdConfig fluentdConfig = new FluentdConfig(port, configFile, initWait, maxWait, workDir, configuration);

        fluentd = new FluentdStandalone(fluentdConfig);

        fluentd.startAndAwait();

        latch.countDown();

        try {
            latch.await(maxWait, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }

        JsonObject config = new JsonObject();
        config.putString("address", ADDRESS);
        config.putString("tagPrefix", "debug");

        container.deployModule(System.getProperty("vertx.modulename"), config, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                if (asyncResult.failed()) {
                    container.logger().error(asyncResult.cause());
                }
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());
                startTests();
            }
        });

    }

    @Override
    public void stop() {
        super.stop();
        if (fluentd != null) {
            fluentd.stop();
        }
    }

}
