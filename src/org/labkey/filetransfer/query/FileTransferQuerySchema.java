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
package org.labkey.filetransfer.query;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.list.ListDefinition;
import org.labkey.api.exp.list.ListService;
import org.labkey.api.module.Module;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.api.view.Portal;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.FileTransferModule;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by susanh on 2/9/17.
 */
public class FileTransferQuerySchema extends UserSchema
{
    public static final String NAME = "fileTransfer";
    private static final String DESCRIPTION = "Provides data on file transfer endpoints";

    public static final String FILE_METADATA_TABLE_NAME = "FileMetadata";

    public FileTransferQuerySchema(User user, Container container)
    {
        super(NAME, DESCRIPTION, user, container, ListService.get().getUserSchema(user, container).getDbSchema());
    }

    public static void register(final FileTransferModule module)
    {
        DefaultSchema.registerProvider(NAME, new DefaultSchema.SchemaProvider(module)
        {
            @Override
            public QuerySchema createSchema(DefaultSchema schema, Module module)
            {
                return new FileTransferQuerySchema(schema.getUser(), schema.getContainer());
            }
        });
    }

    @Override
    public @Nullable TableInfo createTable(String name, ContainerFilter cf)
    {
        if (name.startsWith(FILE_METADATA_TABLE_NAME))
        {
            String[] parts = name.split("_");
            if (parts.length > 1)
            {
                Portal.WebPart webPart = Portal.getPart(getContainer(), Integer.parseInt(parts[1]));
                Map<String, String> properties = webPart.getPropertyMap();
                ListDefinition listDef = FileTransferManager.get().getMetadataList(properties);
                if (listDef != null && listDef.getDomain() != null)
                {
                    UserSchema userSchema = ListService.get().getUserSchema(getUser(), getContainer());
                    TableInfo listTable = userSchema.getTable(listDef.getDomain().getName(), cf, true, true);

                    if (listTable != null)
                        return new FileTransferMetadataTable(properties, listTable, this, cf);
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public Set<String> getTableNames()
    {
        return Collections.emptySet();
    }
}
