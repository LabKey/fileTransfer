package org.labkey.filetransfer.settings;

import org.labkey.api.settings.AbstractWriteableSettingsGroup;

public class FileTransferSettingsConfig  extends AbstractWriteableSettingsGroup
{

    @Override
    protected String getGroupName()
    {
        return "File Transfer";
    }

    @Override
    protected String getType()
    {
        return "File Transfer";
    }
}