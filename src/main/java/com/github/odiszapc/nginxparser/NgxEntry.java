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


public interface NgxEntry
{
    NgxBlock getParent();

    void setParent(NgxBlock block);

    boolean removeSelf();

    /**
     * @param parent 会将克隆的节点添加到parent
     * @return
     */
    NgxEntry cloneDeep(NgxBlock parent);

    /**
     * 获取之前的节点
     *
     * @return
     */
    NgxEntry before();

    /**
     * 获取之后的节点
     *
     * @return
     */
    NgxEntry after();

    /**
     * 再此节点之前添加
     *
     * @param entry
     */
    void addBefore(NgxEntry entry);

    /**
     * 再此节点之后添加
     *
     * @param entry
     */
    void addAfter(NgxEntry entry);
}
