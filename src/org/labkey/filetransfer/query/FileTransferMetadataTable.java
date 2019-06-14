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

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.BaseColumnInfo;
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
    public FileTransferMetadataTable(Map<String, String> properties, TableInfo table, @NotNull UserSchema userSchema, ContainerFilter cf)
    {
        // Container filter seems fishy, but tests fail if we just pass in cf. TODO: Document rationale for this...
        super(table, userSchema, ContainerFilter.EVERYTHING);

        wrapAllColumns(true);
        setDetailsURL(null);

        getMutableColumn("CreatedBy").setHidden(true);
        getMutableColumn("Modified").setHidden(true);
        getMutableColumn("ModifiedBy").setHidden(true);
        getMutableColumn("Created").setHidden(true);
        getMutableColumn("Container").setHidden(true);

        File filesDir = FileTransferManager.get().getLocalFilesDirectory(properties);
        if (filesDir != null && filesDir.exists() && filesDir.canRead())
        {
            List<String> activeFiles = FileTransferManager.get().getActiveFiles(filesDir);
            ColumnInfo fromColumn = getRealTable().getColumn(properties.get(REFERENCE_COLUMN));
            if (fromColumn == null)
                return;
            BaseColumnInfo availabilityColumn = wrapColumn("Available", new BaseColumnInfo(fromColumn));
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
