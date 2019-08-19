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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class NgxAbstractEntry implements NgxEntry, Cloneable
{
    private Collection<NgxToken> tokens = new ArrayList<NgxToken>();
    private NgxBlock parent;

    public NgxAbstractEntry(String... rawValues)
    {
        setValues(false, rawValues);
    }

    @Override
    public NgxAbstractEntry cloneDeep(NgxBlock parent)
    {
        try
        {
            NgxAbstractEntry cloneEntry = (NgxAbstractEntry) super.clone();
            cloneEntry.parent = null;
            if (parent != null)
            {
                parent.addEntry(cloneEntry);
            }
            cloneEntry.tokens = new ArrayList<>();
            for (NgxToken ngxToken : this.tokens)
            {
                cloneEntry.tokens.add(ngxToken.clone());
            }
            return cloneEntry;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NgxBlock getParent()
    {
        return parent;
    }

    @Override
    public void setParent(NgxBlock parent)
    {
        this.parent = parent;
    }

    @Override
    public void removeSelf()
    {
        NgxBlock parent = getParent();
        if (parent == null)
        {
            throw new RuntimeException("not found parent");
        }
        parent.remove(this);
    }

    public Collection<NgxToken> getTokens()
    {
        return tokens;
    }

    public void addValue(NgxToken token)
    {
        tokens.add(token);
    }

    public void addValue(String value)
    {
        addValue(new NgxToken(value));
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (NgxToken value : tokens)
        {
            builder.append(value).append(" ");
        }
        String s = builder.toString();
        return s.substring(0, s.length() - 1);
    }

    public String getName()
    {
        if (getTokens().isEmpty())
            return null;

        return getTokens().iterator().next().toString();
    }

    public List<String> getValues()
    {
        ArrayList<String> values = new ArrayList<String>();
        if (getTokens().size() < 2)
            return values;

        Iterator<NgxToken> it = getTokens().iterator();
        it.next();
        while (it.hasNext())
        {
            values.add(it.next().toString());
        }
        return values;
    }

    public String getValue()
    {
        Iterator<String> iterator = getValues().iterator();
        StringBuilder builder = new StringBuilder();
        while (iterator.hasNext())
        {
            builder.append(iterator.next());
            if (iterator.hasNext())
            {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    public void setValues(String... rawValues)
    {
        setValues(true, rawValues);
    }

    private void setValues(boolean addName, String... rawValues)
    {
        String name = null;
        if (addName)
        {
            name = getName();
        }
        tokens.clear();
        if (name != null)
        {
            tokens.add(new NgxToken(name));
        }
        for (String val : rawValues)
        {
            tokens.add(new NgxToken(val));
        }
    }


    /**
     * 再此节点之前添加
     *
     * @param entry
     */
    public void addBefore(NgxEntry entry)
    {
        addBeforeOrAfter(true, entry);
    }

    private void addBeforeOrAfter(boolean isBefore, NgxEntry entry)
    {
        NgxBlock parent = getParent();
        if (parent == null)
        {
            throw new RuntimeException("not have parent");
        }

        List<NgxEntry> parentList = parent.entries;
        for (int i = 0; i < parentList.size(); i++)
        {
            if (parentList.get(i) == this)
            {
                if (isBefore)
                {
                    parentList.add(i, entry);
                } else
                {
                    parentList.add(i + 1, entry);
                }
                if (entry.getParent() != null)
                {
                    entry.removeSelf();
                }
                entry.setParent(parent);
                break;
            }
        }
    }


    /**
     * 再此节点之后添加
     *
     * @param entry
     */
    public void addAfter(NgxEntry entry)
    {
        addBeforeOrAfter(false, entry);
    }

    /**
     * 获取之前的节点
     *
     * @return
     */
    public NgxEntry before()
    {
        return getBeforeOrAfter(true);
    }

    /**
     * 获取之后的节点
     *
     * @return
     */
    public NgxEntry after()
    {
        return getBeforeOrAfter(false);
    }

    private NgxEntry getBeforeOrAfter(boolean isBefore)
    {
        NgxBlock parent = getParent();
        if (parent == null)
        {
            throw new RuntimeException("not have parent");
        }
        NgxEntry ngxEntry = null;

        List<NgxEntry> parentList = parent.entries;
        for (int i = 0; i < parentList.size(); i++)
        {
            if (parentList.get(i) == this)
            {
                if (isBefore)
                {
                    if (i > 0)
                    {
                        ngxEntry = parentList.get(i - 1);
                    }
                } else
                {
                    if (i < parentList.size() - 1)
                    {
                        ngxEntry = parentList.get(i + 1);
                    }
                }
                break;
            }
        }
        return ngxEntry;
    }


}
