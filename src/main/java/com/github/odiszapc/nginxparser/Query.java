package com.github.odiszapc.nginxparser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2019-04-23.
 */
public interface Query
{
    /**
     * 支持{@linkplain Query}和String（或CharSequence），其中String（或CharSequence）等同于{@linkplain Name}
     *
     * @param queries
     * @return
     */
    static List<Query> toQuery(Object[] queries)
    {
        List<Query> queryList = new ArrayList<>();

        for (int i = 0; i < queries.length; i++)
        {
            if (queries[i] instanceof Query)
            {
                queryList.add((Query) queries[i]);
            } else if (queries[i] instanceof CharSequence)
            {
                queryList.add(new Query.Name(String.valueOf(queries[i])));
            } else
            {
                throw new RuntimeException("unknown query item:" + queries[i] + ",index=" + i);
            }
        }
        return queryList;
    }

    static Query or(Object... queries)
    {
        return new OR(queries);
    }

    static Query and(Object... queries)
    {
        return new AND(queries);
    }

    static Query not(Query query)
    {
        return new NOT(query);
    }

    static Query eq(String name, String value)
    {
        return new EQ(name, value);
    }

    static Query detector(Object... queries)
    {
        return new SubDetector(queries);
    }

    static Query comment(String commentContent)
    {
        return new Comment(commentContent);
    }

    boolean accept(NgxEntry entry);


    class Name implements Query
    {
        private String name;

        public Name(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry instanceof NgxParam)
            {
                return ((NgxParam) entry).getName().equals(name);
            } else
            {
                return ((NgxBlock) entry).getName().equals(name);
            }
        }

    }

    abstract class Compare implements Query
    {
        private String name;
        private String value;

        public Compare(String name, String value)
        {
            this.name = name;
            this.value = value;
        }

        public String getName()
        {
            return name;
        }

        public String getValue()
        {
            return value;
        }
    }

    class EQ extends Compare
    {

        public EQ(String name, String value)
        {
            super(name, value);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry instanceof NgxAbstractEntry)
            {
                NgxAbstractEntry abstractEntry = (NgxAbstractEntry) entry;
                return abstractEntry.getName().equals(getName()) && abstractEntry.getValue().equals(getValue());
            } else
            {
                throw new RuntimeException("not support!");
            }
        }
    }

    class NEQ extends EQ
    {

        public NEQ(String name, String value)
        {
            super(name, value);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            return !super.accept(entry);
        }
    }

    class Reg extends Compare
    {
        private Pattern pattern;

        public Reg(String name, String reg)
        {
            super(name, reg);
            this.pattern = Pattern.compile(reg);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry instanceof NgxAbstractEntry)
            {
                NgxAbstractEntry abstractEntry = (NgxAbstractEntry) entry;
                return abstractEntry.getName().equals(getName()) && pattern.matcher(abstractEntry.getValue()).find();
            } else
            {
                return false;
            }
        }
    }

    /**
     * 判断值存在指定的内容
     */
    class Contains extends Compare
    {

        public Contains(String name, String value)
        {
            super(name, value);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry instanceof NgxAbstractEntry)
            {
                NgxAbstractEntry abstractEntry = (NgxAbstractEntry) entry;
                return abstractEntry.getName().equals(getName()) && abstractEntry.getValue().contains(getValue());
            } else
            {
                throw new RuntimeException("not support!");
            }
        }
    }

    /**
     * 判断以指定的值开始
     */
    class Starts extends Compare
    {

        public Starts(String name, String value)
        {
            super(name, value);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry instanceof NgxAbstractEntry)
            {
                NgxAbstractEntry abstractEntry = (NgxAbstractEntry) entry;
                return abstractEntry.getName().equals(getName()) && abstractEntry.getValue().startsWith(getValue());
            } else
            {
                throw new RuntimeException("not support!");
            }
        }
    }

    /**
     * 判断以指定的值结束
     */
    class Ends extends Compare
    {

        public Ends(String name, String value)
        {
            super(name, value);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry instanceof NgxAbstractEntry)
            {
                NgxAbstractEntry abstractEntry = (NgxAbstractEntry) entry;
                return abstractEntry.getName().equals(getName()) && abstractEntry.getValue().endsWith(getValue());
            } else
            {
                throw new RuntimeException("not support!");
            }
        }
    }


    class NOT implements Query
    {
        private Query query;

        public NOT(Query query)
        {
            this.query = query;
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            return !query.accept(entry);
        }
    }


    abstract class SubQuery implements Query
    {
        protected List<Query> queries;

        public SubQuery(Object... queries)
        {
            this.queries = Query.toQuery(queries);
        }
    }

    /**
     * 子查询or，判断当前节点是否有一个满足条件
     */
    class OR extends SubQuery
    {
        public OR(Object... queries)
        {
            super(queries);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            for (Query query : queries)
            {
                if (query.accept(entry))
                {
                    return true;
                }
            }
            return false;
        }
    }


    /**
     * 子查询and,判断当前节点是否满足所有条件
     */
    class AND extends SubQuery
    {
        public AND(Object... queries)
        {
            super(queries);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            for (Query query : queries)
            {
                if (!query.accept(entry))
                {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * 子检测,用于判断{@linkplain NgxBlock}当前节点是否能找到条件匹配的子节点，如果能则当前节点通过.
     */
    class SubDetector implements Query
    {
        private Object[] queries;

        /**
         * @param queries 见{@linkplain Query#toQuery(Object[])}。
         */
        public SubDetector(Object... queries)
        {
            this.queries = queries;
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry.getClass() == NgxBlock.class)
            {
                NgxBlock block = (NgxBlock) entry;
                if (block.queryOneNgxParam(queries) != null || block.queryOneNgxBlock(queries) != null || block
                        .queryOneNgxComment(queries) != null)
                {
                    return true;
                } else
                {
                    return false;
                }
            } else
            {
                return false;
            }
        }
    }

    /**
     * 判断是否等于指定注释内容，会先去掉实际注释结尾的换行字符。
     */
    class Comment implements Query
    {

        protected String value;

        public Comment(String value)
        {
            this.value = value;
        }

        protected static String getCommentString(NgxComment ngxComment)
        {
            String str = ngxComment.getValue().replaceAll("[\r\n]", "");
            return str;
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry.getClass() == NgxComment.class)
            {
                NgxComment ngxComment = (NgxComment) entry;
                String str = getCommentString(ngxComment);
                return str.equals(this.value);
            } else
            {
                return false;
            }
        }
    }

    /**
     * 判断是否以指定注释内容开头，会先去掉实际注释后面的换行字符。
     */
    class CommentStarts extends Comment
    {

        public CommentStarts(String value)
        {
            super(value);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry.getClass() == NgxComment.class)
            {
                NgxComment ngxComment = (NgxComment) entry;
                String str = getCommentString(ngxComment);
                return str.startsWith(super.value);
            } else
            {
                return false;
            }
        }
    }

    /**
     * 判断是否以指定注释内容结尾，会先去掉实际注释后面的换行字符。
     */
    class CommentEnds extends Comment
    {

        public CommentEnds(String value)
        {
            super(value);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry.getClass() == NgxComment.class)
            {
                NgxComment ngxComment = (NgxComment) entry;
                String str = getCommentString(ngxComment);
                return str.endsWith(super.value);
            } else
            {
                return false;
            }
        }
    }

    /**
     * 判断是否包含指定注释内容，会先去掉实际注释后面的换行字符。
     */
    class CommentContains extends Comment
    {

        public CommentContains(String value)
        {
            super(value);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry.getClass() == NgxComment.class)
            {
                NgxComment ngxComment = (NgxComment) entry;
                String str = getCommentString(ngxComment);
                return str.contains(super.value);
            } else
            {
                return false;
            }
        }
    }

    /**
     * 判断是否匹配指定的正则表达式，会先去掉实际注释后面的换行字符。
     */
    class CommentReg implements Query
    {
        private Pattern pattern;

        public CommentReg(String reg)
        {
            this.pattern = Pattern.compile(reg);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry instanceof NgxComment)
            {
                NgxComment ngxComment = (NgxComment) entry;
                String str = Comment.getCommentString(ngxComment);
                return pattern.matcher(str).find();
            } else
            {
                return false;
            }
        }
    }

}
