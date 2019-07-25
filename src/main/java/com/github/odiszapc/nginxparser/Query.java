package com.github.odiszapc.nginxparser;

import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2019-04-23.
 */
public interface Query
{

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

    class Eq extends Compare
    {

        public Eq(String name, String value)
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

    class NEq extends Eq
    {

        public NEq(String name, String value)
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
                throw new RuntimeException("not support!");
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


    class Not implements Query
    {
        private Query query;

        public Not(Query query)
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
        private String description;
        protected Query[] queries;

        public SubQuery(String description, Query... queries)
        {
            this.description = description;
            this.queries = queries;
        }

        public String getDescription()
        {
            return description;
        }
    }

    /**
     * 子查询or，判断当前节点是否有一个满足条件
     */
    class Or extends SubQuery
    {
        public Or(String description, Query... queries)
        {
            super(description, queries);
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
    class And extends SubQuery
    {
        public And(String description, Query... queries)
        {
            super(description, queries);
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
         * @param queries 同{@linkplain NgxBlock#query(Class, boolean, Object...)}的条件。
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
                if (block.queryOneNgxParam(queries) != null || block.queryOneNgxBlock(queries) != null)
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

    class Comment implements Query
    {

        private String value;

        public Comment(String value)
        {
            this.value = value;
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry.getClass() == NgxComment.class)
            {
                NgxComment ngxComment = (NgxComment) entry;
                String str = ngxComment.getValue().replaceAll("[\r\n]", "");
                return str.equals(this.value);
            } else
            {
                return false;
            }
        }
    }

}
