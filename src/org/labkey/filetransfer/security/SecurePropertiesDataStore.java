/*
 * Copyright (c) 2017-2019 LabKey Corporation
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
package org.labkey.filetransfer.security;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import org.labkey.api.data.Container;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.security.User;

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
    private final User user;
    private final Container container;

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
    public int size()
    {
        return 0;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean containsKey(String key)
    {
        return false;
    }

    @Override
    public boolean containsValue(StoredCredential value)
    {
        return false;
    }

    @Override
    public Set<String> keySet()
    {
        return null;
    }

    @Override
    public Collection<StoredCredential> values()
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

    @Override
    public StoredCredential get(String key)
    {
        return getCredential();
    }

    @Override
    public SecurePropertiesDataStore set(String key, StoredCredential value)
    {
        PropertyManager.PropertyMap properties = getWritableProperties(user, container);
        properties.put(ACCESS_TOKEN, value.getAccessToken());
        properties.put(REFRESH_TOKEN, value.getRefreshToken());
        properties.put(EXPIRE_TIME_MILLIS, String.valueOf(value.getExpirationTimeMilliseconds()));
        properties.save();

        return this;
    }

    @Override
    public SecurePropertiesDataStore clear()
    {
        PropertyManager.PropertyMap properties = getWritableProperties(user, container);
        properties.clear();
        properties.save();

        return this;
    }

    @Override
    public SecurePropertiesDataStore delete(String key)
    {
        PropertyManager.PropertyMap properties = getWritableProperties(user, container);
        properties.remove(key);
        properties.save();

        return this;
    }
}
