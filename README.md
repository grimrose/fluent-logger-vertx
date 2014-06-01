# Fluentd Logger Module for Vert.x

[![Build Status](https://travis-ci.org/grimrose/fluent-logger-vertx.svg?branch=master)](https://travis-ci.org/grimrose/fluent-logger-vertx)
[ ![Download](https://api.bintray.com/packages/grimrose/vertx-mods/fluent-logger-vertx/images/download.png) ](https://bintray.com/grimrose/vertx-mods/fluent-logger-vertx/_latestVersion)

This module is wrapper of [fluent / fluent-logger-java](https://github.com/fluent/fluent-logger-java) for [Vert.x](http://vertx.io).


#### To use this module you must have a Fluentd instance running on your network.

## Dependencies

This module requires a [Fluentd](http://fluentd.org) to be available on the network.

## Name

The module name is `fluent-logger-vertx`.

## Configuration

The fluent-logger module takes the following configuration:

    {
        "address": <address>,
        "tagPrefix": <tag_prefix>,
        "host": <host>,
        "port": <port>,
        "timeout": <timeout>,
        "bufferCapacity": <buffer_capacity>
    }

For example:

    {
        "address": "fluent.logger",
        "tagPrefix": "debug",
        "host": "localhost",
        "port": 24224,
        "timeout": 3000,
        "bufferCapacity": 1048576
    }

Let's take a look at each field in turn:

* `address` The main address for the module. Every module has a main address. It is mandatory.
* `tagPrefix` is prefix of Fluentd’s tag. Dafaults to `vertx`.
* `host` Host name or ip address of the Fluentd instance. Defaults to `localhost`.
* `port` Port at which the Fluentd instance is listening. Defaults to `24224`.
* `timeout` the timeout value to be used in milliseconds. Defaults to `3000`.
* `bufferCapacity` The new buffer's capacity, in bytes. Default is 1048576(= 1024 * 1024).

### Logging

To send a JSON message to the module address:

    {
        "tag": <tag>,
        "data": {
            <data>
        },
        "timestamp": <timestamp>
    }

Where:

* `tag` is suffix of Fluentd’s tag. Defaults to `app`.
* `data` is the JSON document that you wish to log. Default is empty JsonObject.
* `timestamp` is the event occurrence time. Default is 0.

An example would be:

    {
        "tag": "request",
        "data": {
            "id": "user1",
            "action": "get",
            "domain": "cart",
            "param": ["value1", "value2"]
        }
    }

and:

```bash
2014-01-17 21:47:28 +0900 debug.request: {"id":"user1","action":"get","domain":"cart","param":["param1","param2"]}
```
