/*
 * Copyright 2010-2014, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudbees.syslog.sender;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class UdpSyslogMessageSenderLoadTest {

    public static void main(String[] args) throws Exception {
        final int THREADS_COUNT = 10;
        final int ITERATION_COUNT = 1000;

        ExecutorService executorService = Executors.newFixedThreadPool(THREADS_COUNT);

        final UdpSyslogMessageSender messageSender = new UdpSyslogMessageSender();
        messageSender.setDefaultMessageHostname("mysecretkey");
        messageSender.setDefaultAppName("myapp");
        messageSender.setDefaultFacility(Facility.USER);
        messageSender.setDefaultSeverity(Severity.INFORMATIONAL);
        messageSender.setSyslogServerHostname("logs2.papertrailapp.com");
        // messageSender.setSyslogServerHostname("127.0.0.1");
        messageSender.setSyslogServerPort(46022);

        final AtomicInteger count = new AtomicInteger();


        for (int i = 0; i < THREADS_COUNT; i++) {
            final String prefix = "thread-" + i + "-udp-msg-";

            Runnable command = () -> {
                for (int j = 0; j < ITERATION_COUNT; j++) {
                    try {
                        messageSender.sendMessage(prefix + j);
                    } catch (IOException e) {
                        System.err.println("ERROR in " + prefix);
                        e.printStackTrace();
                        break;
                    }
                }
            };

            executorService.execute(command);
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("sent " + messageSender.getSendCount() + " in " + messageSender.getSendDurationInMillis() + "ms");
        System.out.println("bye");
    }
}
