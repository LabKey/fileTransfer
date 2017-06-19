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
import org.labkey.api.module.CodeOnlyModule;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.settings.AdminConsole;
import org.labkey.api.view.WebPartFactory;
import org.labkey.filetransfer.globus.GlobusFileTransferProvider;
import org.labkey.filetransfer.provider.Registry;
import org.labkey.filetransfer.query.FileTransferQuerySchema;
import org.labkey.filetransfer.view.FileTransferMetadataWebPartFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class FileTransferModule extends CodeOnlyModule
{
    private static final String NAME = "FileTransfer";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    @NotNull
    protected Collection<WebPartFactory> createWebPartFactories()
    {
        return Collections.singleton(new FileTransferMetadataWebPartFactory());
    }

    private void registerAdminConsoleLinks()
    {
        AdminConsole.addLink(AdminConsole.SettingsLinkType.Premium, "File Transfer", FileTransferController.getComplianceSettingsURL(), AdminPermission.class);
    }

    @Override
    protected void doStartup(ModuleContext moduleContext)
    {
        registerAdminConsoleLinks();
        Registry.registerProvider(GlobusFileTransferProvider.NAME, GlobusFileTransferProvider.class);
    }

    @Override
    protected void init()
    {
        addController(FileTransferController.NAME, FileTransferController.class);

        FileTransferQuerySchema.register(this);
    }

    @Override
    @NotNull
    public Set<Class> getIntegrationTests()
    {
        return Collections.singleton(FileTransferController.TestCase.class);
    }
}