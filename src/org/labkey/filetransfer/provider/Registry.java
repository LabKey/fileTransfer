package org.labkey.filetransfer.provider;

import org.apache.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by susanh on 5/22/17.
 */
public class Registry
{
    private static final Logger logger = Logger.getLogger(Registry.class);
    private static Map<String, Class<? extends FileTransferProvider>> _providers = new ConcurrentHashMap<>();
    private static Registry _instance = new Registry();

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
