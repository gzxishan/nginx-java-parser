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
                .queryNgxParam("http", "server", new Query.EQ("server_name", "localhost2"));
        assertEquals(1, blockList.size());


        blockList = conf
                .queryNgxParam("http", "server", new Query.EQ("server_name", "localhost3 localhost4"));
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
        assertNotNull(ngxConfig.queryOneNgxComment(new Query.CommentStarts("pid")));
        assertNotNull(ngxConfig.queryOneNgxComment("http", new Query.Comment("gzip  on;")));

        //AND条件：name为server，且监听80端口(子节点)
        assertNotNull(ngxConfig.queryOneNgxBlock("http",
                Query.and("server", Query.detector(Query.eq("listen", "80"))))
        );

        //获取监听80端口的server节点，或者监听8080端口且含有注释"#listen 80;"的server节点
        List<NgxBlock> ngxBlocks = ngxConfig.queryNgxBlock("http",
                Query.or(
                        Query.and("server", Query.detector(Query.eq("listen", "80"))),
                        Query.and("server",
                                Query.detector(Query.eq("listen", "8080")),
                                Query.detector(Query.comment("listen 80;"))
                        )
                )
        );
        assertEquals(2, ngxBlocks.size());

        NgxBlock firstServer = ngxConfig.queryOneNgxBlock("http", "server");

        NgxComment ngxComment = (NgxComment) firstServer.before();
        assertEquals("gzip  on;", ngxComment.getValue().trim());

        NgxBlock ngxBlock = (NgxBlock) firstServer.after();
        assertEquals("server", ngxBlock.getName());

        firstServer.addBefore(new NgxComment("before comment"));
        firstServer.addAfter(new NgxComment("after comment"));

        ngxComment = (NgxComment) firstServer.before();
        assertEquals("before comment", ngxComment.getValue().trim());
        ngxComment = (NgxComment) firstServer.after();
        assertEquals("after comment", ngxComment.getValue().trim());


    }
}
