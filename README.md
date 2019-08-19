# Nginx configuration Java parser

This library helps in analyzing Nginx web server configuration files, looking up for specified parameters, blocks, regular expressions or comments. Then AST can be modified and converted back to plain file.
在原基础上增加了query系列函数、快速方便定位指定节点。

##
# 版本
当前最新版本为  [**1.1.2**](https://mvnrepository.com/artifact/com.xishankeji.forks/nginxparser)

![Version](https://img.shields.io/badge/Version-1.1.2-brightgreen.svg)
![License](http://img.shields.io/:License-Apache2.0-blue.svg)
![JDK 1.8](https://img.shields.io/badge/JDK-1.8-green.svg)

# 码云
[https://gitee.com/xishankeji/nginx-java-parser](https://gitee.com/xishankeji/nginx-java-parser)
# github
[https://github.com/gzxishan/nginx-java-parser](https://github.com/gzxishan/nginx-java-parser)
# original
[https://github.com/odiszapc/nginx-java-parser](https://github.com/odiszapc/nginx-java-parser)


#### Features
- Convert config file to AST tree using ANTLR4 parsing capabilities
- The same is available for JavaCC too (deprecated)
- Rebuild config files and dump them back to *.conf
- Nested blocks support
- If statements support
- Unquoted regexp within location/rewrite/if statements support
- Comments support

#### Installation
Add the following dependency to your POM:
```xml
<dependency>
    <groupId>com.xishankeji.forks</groupId>
    <artifactId>nginxparser</artifactId>
    <version>1.0.12</version>
</dependency>
```

#### Examples
##### Parser

How to perform basic parsing of the following Nginx config:
```java
NgxConfig conf = NgxConfig.read("/etc/nginx/nginx.conf");
NgxParam workers = conf.findParam("worker_processes");       // Ex.1
workers.getValue(); // "1"
NgxParam listen = conf.findParam("http", "server", "listen"); // Ex.2
listen.getValue(); // "8889"
List<NgxEntry> rtmpServers = conf.findAll(NgxConfig.BLOCK, "rtmp", "server"); // Ex.3
for (NgxEntry entry : rtmpServers) {
    ((NgxBlock)entry).getName(); // "server"
    ((NgxBlock)entry).findParam("application", "live"); // "on" for the first iter, "off" for the second one
}
```

/etc/nginx/nginx.conf:
```
worker_processes  1;            # <- Ex.1

http {
    server {
        listen       8889;      # <- Ex.2
        server_name  localhost;
    }
}

rtmp {
    server {                    # <- Ex.3 (first)
        listen 1935;
        application myapp {
            live on;
        }
    }

    server {                    # <- Ex.3 (second)
        listen 1936;
        application myapp2 {
            live off;
        }
    }
}
```

##### Dumper

```java
NgxConfig conf = NgxConfig.read("/etc/nginx/nginx.conf");
// ...
NgxDumper dumper = new NgxDumper(conf);
return dumper.dump(System.out);
```

#### Authors
Alexey Plotnik (odiszapc@gmail.com, http://twitter.com/odiszapc, http://alexey-plotnik.me) I do it just because I like it.



#### License
Apache 2.0

### 编译g4文件
- 安装idea插件：antlr-v4-grammar-plugin
- 在g4文件右键，选择configure ANTLR...,package/namespace可设置成所在包,Output directory设置输出目录

### NgxBlock的query系列函数
- 基础query函数：public <T extends NgxEntry> List<T> query(Class<T> clazz, boolean justOne, Object... queries)
- 查询所有满足条件的NgxBlock：public List<NgxBlock> queryNgxBlock(Object... queries)
- 查询满足条件的第一个NgxBlock：public NgxBlock queryOneNgxBlock(Object... queries)
- 查询所有满足条件的NgxParam：public List<NgxParam> queryNgxParam(Object... queries)
- 查询满足条件的第一个NgxParam：public NgxParam queryOneNgxParam(Object... queries)
- 查询所有满足条件的NgxComment：public List<NgxComment> queryNgxComment(Object... queries)
- 查询满足条件的第一个NgxComment：public NgxBlock queryOneNgxComment(Object... queries)

### query系列函数例子
有如下nginx配置：
```
#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

    server {
        listen 8080;
        #listen 80;

    }

    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            root   html;
            index  index.html index.htm;
        }

        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
        #
        #location ~ \.php$ {
        #    proxy_pass   http://127.0.0.1;
        #}

        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
    }


    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    #server {
    #    listen       8000;
    #    listen       somename:8080;
    #    server_name  somename  alias  another.alias;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}


    # HTTPS server
    #
    #server {
    #    listen       443 ssl;
    #    server_name  localhost;

    #    ssl_certificate      cert.pem;
    #    ssl_certificate_key  cert.key;

    #    ssl_session_cache    shared:SSL:1m;
    #    ssl_session_timeout  5m;

    #    ssl_ciphers  HIGH:!aNULL:!MD5;
    #    ssl_prefer_server_ciphers  on;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}

}
```
- 获得NgxConfig
```
 NgxConfig ngxConfig NgxConfig.read(inputStream,encoding);
```

- 查询注释“#user  nobody;”
```
NgxComment ngxComment = ngxConfig.queryOneNgxComment(new Query.Comment("user  nobody;"));
```
- 查询注释“#pid        logs/nginx.pid;”
```
NgxComment ngxComment =  ngxConfig.queryOneNgxComment(new Query.CommentStarts("pid"))
```

- 查询http下的注释“#gzip  on;”
```
NgxComment ngxComment = ngxConfig.queryOneNgxComment("http",new Query.Comment("gzip  on;"));
```

- 获取监听80端口的server节点（server包含子节点"listen 80"）
```
NgxBlock server = ngxConfig.queryOneNgxBlock("http",
                new Query.AND("server", new Query.SubDetector(new Query.EQ("listen", "80"))))
```
- 获取监听80端口的server节点，或者监听8080端口且含有注释"#listen 80;"的server节点
```
List<NgxBlock> ngxBlocks = ngxConfig.queryNgxBlock("http",
        Query.or(
                Query.and("server", Query.detector(Query.eq("listen", "80"))),
                Query.and("server",
                        Query.detector(Query.eq("listen", "8080")),
                        Query.detector(Query.comment("listen 80;"))
                )
        )
);
```

### NgxEntry的before与after
- before():获取之前的节点
- after():获取之后的节点
- addBefore():再此节点之前添加
- addAfter():再此节点之后添加