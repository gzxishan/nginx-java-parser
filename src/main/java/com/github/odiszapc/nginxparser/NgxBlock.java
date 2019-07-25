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

import java.util.*;

/**
 * Describes block section. Example:
 * http {
 * ...
 * }
 */
public class NgxBlock extends NgxAbstractEntry implements Iterable<NgxEntry>
{
    private Collection<NgxEntry> entries = new ArrayList<NgxEntry>();


    @Override
    public NgxBlock cloneDeep(NgxBlock parent)
    {
        NgxBlock cloneBlock = (NgxBlock) super.cloneDeep(parent);
        cloneBlock.entries = new ArrayList<>();
        for (NgxEntry entry : this.entries)
        {
            entry.cloneDeep(cloneBlock);
        }
        return cloneBlock;
    }

    public Collection<NgxEntry> getEntries()
    {
        return entries;
    }

    public Collection<NgxBlock> getBlockEntries()
    {
        Collection<NgxBlock> collection = new ArrayList<>();
        for (NgxEntry entry : getEntries())
        {
            if (entry instanceof NgxBlock)
            {
                collection.add((NgxBlock) entry);
            }
        }
        return collection;
    }

    public void addEntry(NgxEntry entry)
    {
        if (entry.getParent() != null)
        {
            entry.removeSelf();
        }
        entry.setParent(this);
        entries.add(entry);
    }

    @Override
    public String toString()
    {
        return super.toString() + " {";
    }


    @Override
    public Iterator<NgxEntry> iterator()
    {
        return getEntries().iterator();
    }

    public void remove(NgxEntry itemToRemove)
    {
        if (null == itemToRemove)
            throw new NullPointerException("Item can not be null");

        Iterator<NgxEntry> it = entries.iterator();
        while (it.hasNext())
        {
            NgxEntry entry = it.next();
            switch (NgxEntryType.fromClass(entry.getClass()))
            {
                case PARAM:
                case COMMENT:
                    if (entry == itemToRemove)
                    {
                        it.remove();
                        itemToRemove.setParent(null);
                    }
                    break;
                case BLOCK:
                    if (entry == itemToRemove)
                    {
                        it.remove();
                        itemToRemove.setParent(null);
                    } else
                    {
                        NgxBlock block = (NgxBlock) entry;
                        block.remove(itemToRemove);//递归
                    }
                    break;
            }
        }
    }

    public void removeAll(Iterable<? extends NgxEntry> itemsToRemove)
    {
        if (null == itemsToRemove)
            throw new NullPointerException("Items can not be null");
        for (NgxEntry itemToRemove : itemsToRemove)
        {
            remove(itemToRemove);
        }
    }

    public <T extends NgxEntry> T find(Class<T> clazz, String... params)
    {
        List<T> all = findAll(clazz, params);
        if (all.isEmpty())
            return null;

        return all.get(0);
    }

    public NgxBlock findBlock(String... params)
    {
        NgxEntry entry = find(NgxConfig.BLOCK, params);
        if (null == entry)
            return null;
        return (NgxBlock) entry;
    }

    public NgxParam findParam(String... params)
    {
        NgxEntry entry = find(NgxConfig.PARAM, params);
        if (null == entry)
            return null;
        return (NgxParam) entry;
    }


    public <T extends NgxEntry> List<T> findAll(Class<T> clazz, String... params)
    {
        List<T> res = new ArrayList<>();

        if (0 == params.length)
        {
            return res;
        }

        String head = params[0];
        String[] tail = params.length > 1 ? Arrays.copyOfRange(params, 1, params.length) : new String[0];

        for (NgxEntry entry : getEntries())
        {
            switch (NgxEntryType.fromClass(entry.getClass()))
            {
                case PARAM:
                    NgxParam param = (NgxParam) entry;
                    if (param.getName().equals(head) && param.getClass() == clazz)
                    {
                        res.add((T) param);
                    }
                    break;

                case BLOCK:
                    NgxBlock block = (NgxBlock) entry;
                    if (tail.length > 0)
                    {
                        if (block.getName().equals(head))
                        {
                            res.addAll(block.findAll(clazz, tail));
                        }
                    } else
                    {
                        if (block.getName().equals(head) && (clazz.equals(NgxBlock.class)))
                        {
                            res.add((T) block);
                        }
                    }
                    break;
            }
        }

        return res;
    }


    public NgxParam queryOneNgxParam(Object... queries)
    {
        List<NgxParam> list = query(NgxConfig.PARAM, true, queries);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<NgxParam> queryNgxParam(Object... queries)
    {
        return query(NgxConfig.PARAM, false, queries);
    }

    /**
     * 注意：注释包含后面的多个换行符
     *
     * @param queries
     * @return
     */
    public NgxComment queryOneNgxComment(Object... queries)
    {
        List<NgxComment> list = query(NgxConfig.COMMENT, true, queries);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<NgxComment> queryNgxComment(Object... queries)
    {
        return query(NgxConfig.COMMENT, false, queries);
    }

    public NgxBlock queryOneNgxBlock(Object... queries)
    {
        List<NgxBlock> list = query(NgxConfig.BLOCK, true, queries);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<NgxBlock> queryNgxBlock(Object... queries)
    {
        return query(NgxConfig.BLOCK, false, queries);
    }

    /**
     * @param clazz
     * @param queries 支持String（同{@linkplain Query.Name}）与{@linkplain Query}
     * @param <T>
     * @return
     */
    public <T extends NgxEntry> List<T> query(Class<T> clazz, boolean justOne, Object... queries)
    {
        List<T> res = new ArrayList<>();

        if (0 == queries.length)
        {
            queries = new Query[]{
                    (entry) -> true
            };
        }

        List<Query> queryList = new ArrayList<>();

        for (int i = 0; i < queries.length; i++)
        {
            if (queries[i] instanceof Query)
            {
                queryList.add((Query) queries[i]);
            } else
            {
                queryList.add(new Query.Name(String.valueOf(queries[i])));
            }
        }

        Query head = queryList.remove(0);

        for (NgxEntry entry : getEntries())
        {
            switch (NgxEntryType.fromClass(entry.getClass()))
            {
                case PARAM:
                case COMMENT:
                    if (queryList.isEmpty() && entry.getClass() == clazz && head.accept(entry))
                    {
                        res.add((T) entry);//获取到节点
                    }
                    break;
                case BLOCK:
                    NgxBlock block = (NgxBlock) entry;
                    if (queryList.size() > 0)
                    {
                        if (head.accept(block))
                        {
                            res.addAll(block.query(clazz, justOne, queryList.toArray(new Query[0])));//查询子节点
                        }
                    } else
                    {
                        if (entry.getClass() == clazz && head.accept(block))
                        {
                            res.add((T) block);//获取到节点
                        }
                    }
                    break;
            }
            if (justOne && res.size() > 0)
            {
                break;
            }
        }

        return res;
    }
}
