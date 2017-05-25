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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.labkey.filetransfer.FileTransferManager.REFERENCE_COLUMN;

/**
 * Created by susanh on 2/9/17.
 */
public class FileTransferMetadataTable extends FilteredTable<UserSchema>
{
    public FileTransferMetadataTable(Map<String, String> properties, TableInfo table, @NotNull UserSchema userSchema)
    {
        super(table, userSchema, ContainerFilter.EVERYTHING);

        wrapAllColumns(true);
        setDetailsURL(null);

        getColumn("CreatedBy").setHidden(true);
        getColumn("Modified").setHidden(true);
        getColumn("ModifiedBy").setHidden(true);
        getColumn("Created").setHidden(true);
        getColumn("Container").setHidden(true);

        File filesDir = FileTransferManager.get().getLocalFilesDirectory(properties);
        if (filesDir != null && filesDir.exists() && filesDir.canRead())
        {
            List<String> activeFiles = FileTransferManager.get().getActiveFiles(filesDir);
            ColumnInfo fromColumn = getRealTable().getColumn(properties.get(REFERENCE_COLUMN));
            if (fromColumn == null)
                return;
            ColumnInfo availabilityColumn = wrapColumn("Available", new ColumnInfo(fromColumn));
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

                @Override
                public void renderDetailsCellContents(RenderContext ctx, Writer out) throws IOException
                {
                    renderGridCellContents(ctx, out);
                }

                @Override
                public boolean isFilterable()
                {
                    return false;
                }

                @Override
                public boolean isSortable()
                {
                    return false;
                }

                @Override
                public boolean isQueryColumn()
                {
                    return false;
                }
            });
        }

    }

    @Override
    public boolean hasDetailsURL()
    {
        return false;
    }
}
