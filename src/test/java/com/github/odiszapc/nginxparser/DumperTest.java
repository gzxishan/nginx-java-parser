/*
 * Copyright 2014 Alexey Plotnik
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

package com.github.odiszapc.nginxparser;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class DumperTest {

    @Test
    public void testC1() throws Exception {
        final String expected = "" +
                "user nginx;\n" +
                "worker_processes 2;\n" +
                "timer_resolution 100ms;\n" +
                "worker_rlimit_nofile 8192;\n" +
                "worker_priority -10;\n";
        Assert.assertEquals(TestUtils.dump("common/c1.conf"), expected);
    }

    @Test
    public void testC2() throws Exception {
        final String expected = "" +
                "error_log /var/log/nginx/error.log warn;\n" +
                "pid /var/run/nginx.pid;\n" +
                "events {\n" +
                "    worker_connections 2048;\n" +
                "    use epoll;\n" +
                "}\n";
        Assert.assertEquals(TestUtils.dump("common/c2.conf"), expected);
    }

    @Test
    public void testC3() throws Exception {
        final String expected = "" +
                "user nginx;\n" +
                "#worker_processes  2;\n" +
                "worker_priority -10;\n" +
                "proxy_pass http://unix:/opt/apps/ipn/ipn.sock:/;\n";
        Assert.assertEquals(TestUtils.dump("common/c3.conf"), expected);
    }

    @Test
    public void testConf() throws Exception
    {
        String lastConf = TestUtils.getString("dumper/nginx.conf");
        NgxConfig ngxConfig = NgxConfig.read(TestUtils.getStream("dumper/nginx.conf"));
        NgxDumper dumper = new NgxDumper(ngxConfig);
        String conf = dumper.dump();
//        System.out.println(conf);
    }
}
