/*
 * Copyright (c) 2017 LabKey Corporation
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
package org.labkey.filetransfer.provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.labkey.api.collections.CopyOnWriteHashMap;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class Registry
{
    private static final Logger logger = LogManager.getLogger(Registry.class);
    private static Map<String, Class<? extends FileTransferProvider>> _providers = new CopyOnWriteHashMap<>();
    private static final Registry _instance = new Registry();

    private Registry()
    {
    }

    public static Registry get()
    {
        return _instance;
    }

    public static void registerProvider(String name, Class<? extends FileTransferProvider> provider)
    {
        _providers.put(name, provider);
    }

    public Class<? extends FileTransferProvider> getProviderClass(String name)
    {
        return _providers.get(name);
    }

    public FileTransferProvider getProvider(String name)
    {
        if (name == null)
            return null;
        try
        {
            Class<? extends FileTransferProvider> providerClass = _providers.get(name);
            return providerClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            logger.error("Unable to find provider with name '" + name + "'", e);
            return null;
        }
    }

    public FileTransferProvider getProvider(Container container, User user, String name)
    {
        if (name == null)
            return null;
        try
        {
            Class<? extends FileTransferProvider> providerClass = _providers.get(name);
            return providerClass.getDeclaredConstructor(Container.class, User.class).newInstance(container, user);
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
        {
            logger.error("Unable to find provider with name '" + name + "'", e);
            return null;
        }
    }

    public Map<String, Class<? extends FileTransferProvider>> getProviders()
    {
        return _providers;
    }

    public void setProviders(Map<String, Class<? extends FileTransferProvider>> providers)
    {
        _providers = providers;
    }
}
