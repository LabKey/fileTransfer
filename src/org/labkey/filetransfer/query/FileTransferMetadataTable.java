package org.labkey.filetransfer.query;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.UserSchema;
import org.labkey.filetransfer.FileTransferManager;

/**
 * Created by susanh on 2/9/17.
 */
public class FileTransferMetadataTable extends FilteredTable<UserSchema>
{

    public FileTransferMetadataTable(TableInfo table, @NotNull UserSchema userSchema)
    {
        super(table, userSchema, ContainerFilter.EVERYTHING);
        wrapAllColumns(true);

        getColumn("CreatedBy").setHidden(true);
        getColumn("Modified").setHidden(true);
        getColumn("ModifiedBy").setHidden(true);
        getColumn("Created").setHidden(true);
        getColumn("Container").setHidden(true);

        addColumn(wrapColumn("Available", new ColumnInfo(getRealTable().getColumn( FileTransferManager.get().getFileNameColumn(getContainer())))));

    }
}
