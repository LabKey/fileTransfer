package org.labkey.filetransfer.query;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbSchemaType;
import org.labkey.api.data.TableInfo;
import org.labkey.api.module.Module;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.filetransfer.FileTransferModule;

import java.util.Collections;
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
        super(NAME, DESCRIPTION, user, container, DbSchema.get(NAME, DbSchemaType.Module));
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
        return null;
    }

    @Override
    public Set<String> getTableNames()
    {
        return Collections.emptySet();
    }
}
