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
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.module.CodeOnlyModule;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.settings.AdminConsole;
import org.labkey.api.view.SimpleWebPartFactory;
import org.labkey.api.view.WebPartFactory;
import org.labkey.filetransfer.view.FileTransferMetadataView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FileTransferModule extends CodeOnlyModule
{
    public static final String NAME = "FileTransfer";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    @NotNull
    protected Collection<WebPartFactory> createWebPartFactories()
    {
        ArrayList<WebPartFactory> list = new ArrayList<>();
        SimpleWebPartFactory factory = new SimpleWebPartFactory("File Transfer Metadata", WebPartFactory.LOCATION_BODY, FileTransferMetadataView.class, null);
        list.add(factory);
        return list;
    }

    @Override
    protected void init()
    {
        addController(FileTransferController.NAME, FileTransferController.class);
    }

    @Override
    public void doStartup(ModuleContext moduleContext)
    {
        // add a container listener so we'll know when our container is deleted:
        ContainerManager.addContainerListener(new FileTransferContainerListener());
    }

    @Override
    @NotNull
    public Collection<String> getSummary(Container c)
    {
        return Collections.emptyList();
    }
}