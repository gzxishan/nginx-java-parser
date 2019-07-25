package com.github.odiszapc.nginxparser;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Created by https://github.com/CLovinr on 2019-04-24.
 */
public class QueryTest extends ParseTestBase
{
    @Test
    public void testQuery() throws Exception
    {
        NgxConfig conf = parse("nested/query.conf");

        List<NgxParam> blockList = conf
                .queryNgxParam("http", "server", new Query.Eq("server_name", "localhost2"));
        assertEquals(1, blockList.size());


        blockList = conf
                .queryNgxParam("http", "server", new Query.Eq("server_name", "localhost3 localhost4"));
        blockList.forEach(block -> assertEquals("server", block.getParent().getName()));
        assertEquals(1, blockList.size());

    }

    @Test
    public void testQuery2() throws Exception
    {
        NgxConfig conf = parse("nested/query2.conf", "utf-8");
        NgxDumper dumper = new NgxDumper(conf);
        String str = dumper.dump();
        System.out.println(str);

    }

    @Test
    public void testQuery3() throws Exception
    {
        String lastConf = TestUtils.getString("query/nginx.conf");
        NgxConfig ngxConfig = NgxConfig.read(TestUtils.getStream("query/nginx.conf"));
        NgxDumper dumper = new NgxDumper(ngxConfig);
        String conf = dumper.dump();
        ngxConfig = NgxConfig.read(new ByteArrayInputStream(conf.getBytes()));

        assertNotNull(ngxConfig.queryOneNgxComment(new Query.Comment("user  nobody;")));

    }
}
