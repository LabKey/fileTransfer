package org.labkey.filetransfer.query;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.DataColumn;
import org.labkey.api.data.RenderContext;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.UserSchema;
import org.labkey.filetransfer.FileTransferManager;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Created by susanh on 2/9/17.
 */
public class FileTransferMetadataTable extends FilteredTable<UserSchema>
{

    public FileTransferMetadataTable(TableInfo table, List<String> activeFiles, @NotNull UserSchema userSchema)
    {
        super(table, userSchema, ContainerFilter.EVERYTHING);
        wrapAllColumns(true);

        getColumn("CreatedBy").setHidden(true);
        getColumn("Modified").setHidden(true);
        getColumn("ModifiedBy").setHidden(true);
        getColumn("Created").setHidden(true);
        getColumn("Container").setHidden(true);

        ColumnInfo availabilityColumn = wrapColumn("Available", new ColumnInfo(getRealTable().getColumn( FileTransferManager.get().getFileNameColumn(getContainer()))));
        addColumn(availabilityColumn);
        availabilityColumn.setDisplayColumnFactory(colInfo -> new DataColumn(colInfo)
        {
            @Override
            public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
            {
                Object value = getValue(ctx);
                if (value instanceof String && activeFiles.contains(value))
                    out.write("Yes");
                else
                    out.write("No");
            }
        });

    }
}
