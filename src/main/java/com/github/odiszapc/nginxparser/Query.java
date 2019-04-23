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
            if (entry instanceof NgxParam)
            {
                NgxParam param = (NgxParam) entry;
                return param.getName().equals(getName()) && param.getValue().equals(getValue());
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
            if (entry instanceof NgxParam)
            {
                NgxParam param = (NgxParam) entry;
                return param.getName().equals(getName()) && pattern.matcher(param.getValue()).find();
            } else
            {
                throw new RuntimeException("not support!");
            }
        }
    }

    class NReg extends Reg
    {

        public NReg(String name, String reg)
        {
            super(name, reg);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            return !super.accept(entry);
        }
    }

    class Contains extends Compare
    {

        public Contains(String name, String value)
        {
            super(name, value);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            if (entry instanceof NgxParam)
            {
                NgxParam param = (NgxParam) entry;
                return param.getName().equals(getName()) && param.getValue().contains(getValue());
            } else
            {
                throw new RuntimeException("not support!");
            }
        }
    }

    class NContains extends Contains
    {

        public NContains(String name, String value)
        {
            super(name, value);
        }

        @Override
        public boolean accept(NgxEntry entry)
        {
            return !super.accept(entry);
        }
    }

}
