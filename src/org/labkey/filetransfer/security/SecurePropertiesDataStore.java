package org.labkey.filetransfer.security;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import org.labkey.api.data.Container;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.security.User;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.labkey.filetransfer.security.OAuth2Authenticator.ACCESS_TOKEN;
import static org.labkey.filetransfer.security.OAuth2Authenticator.EXPIRE_TIME_MILLIS;
import static org.labkey.filetransfer.security.OAuth2Authenticator.FILE_TRANSFER_AUTH_CATEGORY;
import static org.labkey.filetransfer.security.OAuth2Authenticator.REFRESH_TOKEN;

/**
 * Created by susanh on 5/3/17.
 */
public class SecurePropertiesDataStore implements DataStore<StoredCredential>
{
    private User user;
    private Container container;

    public SecurePropertiesDataStore(User user, Container container)
    {
        this.user = user;
        this.container = container;
    }

    private PropertyManager.PropertyMap getProperties(User user, Container container)
    {
        return PropertyManager.getEncryptedStore().getProperties(user, container, FILE_TRANSFER_AUTH_CATEGORY);
    }

    private PropertyManager.PropertyMap getWritableProperties(User user, Container container)
    {
        return PropertyManager.getEncryptedStore().getWritableProperties(user, container, FILE_TRANSFER_AUTH_CATEGORY, true);
    }

    @Override
    public DataStoreFactory getDataStoreFactory()
    {
        return null;
    }

    @Override
    public String getId()
    {
        return null;
    }

    @Override
    public int size() throws IOException
    {
        return 0;
    }

    @Override
    public boolean isEmpty() throws IOException
    {
        return false;
    }

    @Override
    public boolean containsKey(String key) throws IOException
    {
        return false;
    }

    @Override
    public boolean containsValue(StoredCredential value) throws IOException
    {
        return false;
    }

    @Override
    public Set<String> keySet() throws IOException
    {
        return null;
    }

    public Collection<StoredCredential> values() throws IOException
    {
        return Collections.singleton(get(null));
    }

    public StoredCredential getCredential()
    {
        PropertyManager.PropertyMap map = getProperties(user, container);
        StoredCredential credential = new StoredCredential();
        if (map.get(ACCESS_TOKEN) != null)
            credential.setAccessToken(map.get(ACCESS_TOKEN));
        if (map.get(REFRESH_TOKEN) != null)
            credential.setRefreshToken(map.get(REFRESH_TOKEN));
        if (map.get(EXPIRE_TIME_MILLIS) != null)
            credential.setExpirationTimeMilliseconds(Long.valueOf(map.get(EXPIRE_TIME_MILLIS)));
        return credential;
    }

    public StoredCredential get(String key) throws IOException
    {
        return getCredential();
    }

    public SecurePropertiesDataStore set(String key, StoredCredential value) throws IOException
    {
        PropertyManager.PropertyMap properties = getWritableProperties(user, container);
        properties.put(ACCESS_TOKEN, value.getAccessToken());
        properties.put(REFRESH_TOKEN, value.getRefreshToken());
        properties.put(EXPIRE_TIME_MILLIS, String.valueOf(value.getExpirationTimeMilliseconds()));
        properties.save();

        return this;
    }

    public SecurePropertiesDataStore clear() throws IOException
    {
        PropertyManager.PropertyMap properties = getWritableProperties(user, container);
        properties.clear();
        properties.save();

        return this;
    }

    public SecurePropertiesDataStore delete(String key) throws IOException
    {
        PropertyManager.PropertyMap properties = getWritableProperties(user, container);
        properties.remove(key);
        properties.save();

        return this;
    }
}
