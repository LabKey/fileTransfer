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

package org.labkey.filetransfer;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.module.DefaultModule;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.module.ModuleProperty;
import org.labkey.api.settings.AdminConsole;
import org.labkey.api.view.WebPartFactory;
import org.labkey.api.webdav.WebdavService;
import org.labkey.filetransfer.globus.GlobusFileTransferProvider;
import org.labkey.filetransfer.provider.Registry;
import org.labkey.filetransfer.query.FileTransferQuerySchema;
import org.labkey.filetransfer.view.FileTransferMetadataWebPartFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class FileTransferModule extends DefaultModule
{
    public static final String NAME = "FileTransfer";
    public static final String SCHEMA_NAME = "fileTransfer";
    public static final String FILE_TRANSFER_SOURCE_ENDPOINT_ID = "FileTransferSourceEndpointId";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public double getVersion()
    {
        return 17.10;
    }

    @Override
    @NotNull
    protected Collection<WebPartFactory> createWebPartFactories()
    {
        ArrayList<WebPartFactory> list = new ArrayList<>();
        FileTransferMetadataWebPartFactory factory = new FileTransferMetadataWebPartFactory();
        list.add(factory);
        return list;
    }

    @Override
    public boolean hasScripts()
    {
        return false;
    }

    public static void registerAdminConsoleLinks()
    {
        AdminConsole.addLink(AdminConsole.SettingsLinkType.Configuration, "File Transfer", FileTransferController.getComplianceSettingsURL());
    }

    @Override
    protected void doStartup(ModuleContext moduleContext)
    {
        registerAdminConsoleLinks();
        Registry.registerProvider(GlobusFileTransferProvider.NAME, GlobusFileTransferProvider.class);

        ModuleProperty sourceEndpointIdProp = new ModuleProperty(this, FILE_TRANSFER_SOURCE_ENDPOINT_ID);
        sourceEndpointIdProp.setLabel("Source Endpoint ID");
        sourceEndpointIdProp.setDescription("The unique identifier of the source endpoint.");
        sourceEndpointIdProp.setCanSetPerContainer(true);
        sourceEndpointIdProp.setShowDescriptionInline(true);
        this.addModuleProperty(sourceEndpointIdProp);
    }

    @Override
    protected void init()
    {
        addController(FileTransferController.NAME, FileTransferController.class);

        WebdavService.get().addProvider(new FileTransferWebdavProvider());
        FileTransferQuerySchema.register(this);
    }

    @Override
    @NotNull
    public Set<String> getSchemaNames()
    {
        return Collections.singleton(SCHEMA_NAME);
    }

    @Override
    @NotNull
    public Set<Class> getIntegrationTests()
    {
        return Collections.singleton(FileTransferController.TestCase.class);
    }
}