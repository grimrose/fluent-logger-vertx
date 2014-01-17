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

import org.fluentd.logger.FluentLogger;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

public class FluentLoggerModule extends BusModBase implements Handler<Message<JsonObject>> {

    FluentLogger fluentLogger;

    @Override
    public void start() {
        super.start();

        container.logger().info("start fluent logger module.");

        String address = getMandatoryStringConfig("address");

        String tagPrefix = getOptionalStringConfig("tagPrefix", "vertx");
        String host = getOptionalStringConfig("host", "localhost");
        int port = getOptionalIntConfig("port", 24224);
        int timeout = getOptionalIntConfig("timeout", 3 * 1000);
        int bufferCapacity = getOptionalIntConfig("bufferCapacity", 1 * 1024 * 1024);

        fluentLogger = FluentLogger.getLogger(tagPrefix, host, port, timeout, bufferCapacity);

        eb.registerHandler(address, this);
    }

    @Override
    public void stop() {
        container.logger().info("stop fluent logger module.");
        FluentLogger.flushAll();
        FluentLogger.closeAll();
    }

    @Override
    public void handle(Message<JsonObject> message) {
        JsonObject json = message.body();

        String tag = json.getString("tag", "app");
        JsonObject data = json.getObject("data", new JsonObject());
        Long timestamp = json.getLong("timestamp", 0);

        Map<String,Object> map = data.toMap();
        if (fluentLogger.log(tag, map, timestamp)) {
            sendOK(message);
        } else {
            sendError(message, "error");
        }
    }

}
