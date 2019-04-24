package com.github.odiszapc.nginxparser;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
}
