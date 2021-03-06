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

import com.github.odiszapc.nginxparser.antlr.NginxLexer;
import com.github.odiszapc.nginxparser.antlr.NginxListenerImpl;
import com.github.odiszapc.nginxparser.antlr.NginxParser;
import com.github.odiszapc.nginxparser.javacc.NginxConfigParser;
import com.github.odiszapc.nginxparser.javacc.ParseException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * Main class that describes Nginx config
 */
public class NgxConfig extends NgxBlock
{

    public static final Class<NgxParam> PARAM = NgxParam.class;
    public static final Class<NgxComment> COMMENT = NgxComment.class;
    public static final Class<NgxBlock> BLOCK = NgxBlock.class;
    public static final Class<NgxIfBlock> IF = NgxIfBlock.class;

    /**
     * Parse an existing config
     *
     * @param filepath Path to config file
     * @return Config object
     * @throws IOException
     */
    public static NgxConfig read(String filepath,String encoding) throws IOException
    {
        FileInputStream input = new FileInputStream(filepath);
        return read(input,encoding);
    }

    public static NgxConfig read(InputStream in) throws IOException
    {
        return readAntlr(in, null);
    }

    public static NgxConfig read(InputStream in, String encoding) throws IOException
    {
        return readAntlr(in, encoding);
    }

    /**
     * Read config from existing stream
     *
     * @param input stream to read from
     * @return Config object
     * @throws IOException
     * @throws ParseException
     */
    public static NgxConfig readJavaCC(InputStream input, String encoding) throws IOException, ParseException
    {
        NginxConfigParser parser = new NginxConfigParser(input, encoding);
        return parser.parse();
    }

    public static NgxConfig readJavaCC(InputStream input) throws IOException, ParseException
    {
        NginxConfigParser parser = new NginxConfigParser(input);
        return parser.parse();
    }

    public static NgxConfig readAntlr(InputStream in) throws IOException
    {
        return readAntlr(in, null);
    }

    public static NgxConfig readAntlr(InputStream in, String encoding) throws IOException
    {
        ANTLRInputStream input = encoding == null ? new ANTLRInputStream(in) : new ANTLRInputStream(
                new InputStreamReader(in, encoding));
        NginxLexer lexer = new NginxLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NginxParser parser = new NginxParser(tokens);
        ParseTreeWalker walker = new ParseTreeWalker();

        ParseTree tree = parser.config(); // begin parsing at init rule
        NginxListenerImpl listener = new NginxListenerImpl();
        walker.walk(listener, tree);

        return listener.getResult();
    }

    @Override
    public Collection<NgxToken> getTokens()
    {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void addValue(NgxToken token)
    {
        throw new IllegalStateException("Not implemented");
    }

    public String toString()
    {
        return "Nginx Config (" + getEntries().size() + " entries)";
    }

}
