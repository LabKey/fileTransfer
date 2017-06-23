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

import com.google.api.client.auth.oauth2.StoredCredential;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.globus.TransferResult;
import org.labkey.filetransfer.security.OAuth2Authenticator;
import org.labkey.filetransfer.security.SecurePropertiesDataStore;
import org.labkey.filetransfer.config.FileTransferSettings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/22/17.
 */
public abstract class FileTransferProvider
{
    protected String name;
    protected StoredCredential credential;
    protected FileTransferSettings settings;

    public FileTransferProvider(String name)
    {
        this.name = name;
        settings = new FileTransferSettings(this.name);
    }

    public FileTransferProvider(Container container, User user, String name) throws IOException
    {
        this(name);
        SecurePropertiesDataStore store = new SecurePropertiesDataStore(user, container);
        credential = store.get(null);
    }

    public String getName()
    {
        return name;
    }

    public FileTransferSettings getSettings()
    {
        return settings;
    }

    public abstract TransferResult transfer(@NotNull TransferEndpoint source, @NotNull TransferEndpoint destination, @NotNull List<String> fileNames, @Nullable String label) throws Exception;

    public abstract String getBrowseEndpointUrl(Container container);

    public abstract TransferEndpoint getEndpoint(String endpointId) throws IOException, URISyntaxException;

    public abstract List<TransferEndpoint> getKnownEndpoints();

    public abstract boolean isTransferApiConfigured();

    public abstract String getTransferUiUrl(Map<String, String> properties, ViewContext context);

    public abstract OAuth2Authenticator getAuthenticator(Container container, User user);

}
