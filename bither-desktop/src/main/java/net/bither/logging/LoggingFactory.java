/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package net.bither.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.TimeZone;

/**
 * <p>Factory to provide the following to logging framework:</p>
 * <ul>
 * <li>Initial bootstrap and configuration</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class LoggingFactory {

    public static void bootstrap() {

        // Initially configure for WARN+ console logging
        final LoggingConfiguration.ConsoleConfiguration console = new LoggingConfiguration.ConsoleConfiguration();
        console.setEnabled(true);
        console.setTimeZone(TimeZone.getDefault());
        //console.setThreshold(Level.DEBUG);

        final Logger root = getCleanRoot();
        root.addAppender(LogbackFactory.buildConsoleAppender(console, root.getLoggerContext(), null));
    }

    private final LoggingConfiguration config;
    private final String name;

    public LoggingFactory(LoggingConfiguration config, String name) {
        this.config = config;
        this.name = name;
    }

    public void configure() {

        hijackJDKLogging();

        final Logger root = configureLevels();

        final LoggingConfiguration.ConsoleConfiguration console = config.getConsoleConfiguration();
        if (console.isEnabled()) {
            root.addAppender(AsyncAppender.wrap(
                    LogbackFactory.buildConsoleAppender(
                            console,
                            root.getLoggerContext(),
                            console.getLogFormat())));
        }

        final LoggingConfiguration.FileConfiguration file = config.getFileConfiguration();
        if (file.isEnabled()) {
            root.addAppender(AsyncAppender.wrap(
                    LogbackFactory.buildFileAppender(
                            file,
                            root.getLoggerContext(),
                            file.getLogFormat())));
        }

        final LoggingConfiguration.SyslogConfiguration syslog = config.getSyslogConfiguration();
        if (syslog.isEnabled()) {
            root.addAppender(AsyncAppender.wrap(
                    LogbackFactory.buildSyslogAppender(
                            syslog,
                            root.getLoggerContext(),
                            name,
                            syslog.getLogFormat())));
        }


        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName objectName = new ObjectName("com.ir:type=Logging");
            if (!server.isRegistered(objectName)) {
                server.registerMBean(new JMXConfigurator(root.getLoggerContext(),
                                server,
                                objectName),
                        objectName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void hijackJDKLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private Logger configureLevels() {

        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.getLoggerContext().reset();

        final LevelChangePropagator propagator = new LevelChangePropagator();
        propagator.setContext(root.getLoggerContext());
        propagator.setResetJUL(true);

        root.getLoggerContext().addListener(propagator);

        root.setLevel(config.getLevel());

        // Decode the packages and levels
        for (Map.Entry<String, Level> entry : config.getLoggers().entrySet()) {
            ((Logger) LoggerFactory.getLogger(entry.getKey())).setLevel(entry.getValue());
        }

        return root;
    }

    private static Logger getCleanRoot() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();
        return root;
    }
}

