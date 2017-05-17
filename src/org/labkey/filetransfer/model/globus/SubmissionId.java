package org.labkey.filetransfer.model.globus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by susanh on 5/16/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionId
{
    private String value;

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
