package org.labkey.filetransfer.query;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.list.ListDefinition;
import org.labkey.api.module.Module;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.SchemaKey;
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
    public static final String DESCRIPTION = "Provides data on file transfer endpoints";

    public static final String FILE_METADATA_TABLE_NAME = "FileMetadata";

    public FileTransferQuerySchema(User user, Container container)
    {
        super(NAME, DESCRIPTION, user, container, QueryService.get().getUserSchema(user, container, SchemaKey.fromParts("lists")).getDbSchema());
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

    @Nullable
    @Override
    public TableInfo createTable(String name)
    {
        if (name.startsWith(FILE_METADATA_TABLE_NAME))
        {
            String[] parts = name.split("_");
            if (parts.length > 1)
            {
                Portal.WebPart webPart = Portal.getPart(getContainer(), Integer.parseInt(parts[1]));
                Map<String, String> properties = webPart.getPropertyMap();
                ListDefinition listDef = FileTransferManager.get().getMetadataList(properties);
                return listDef == null ? null : new FileTransferMetadataTable(properties, listDef.getTable(getUser()), this);
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
