package org.labkey.filetransfer.globus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by susanh on 5/16/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferResult
{
    private String task_id;
    private String submission_id;
    private String code;
    private String message;
    private String resource;
    private String request_id;

    public String getTask_id()
    {
        return task_id;
    }

    public void setTask_id(String task_id)
    {
        this.task_id = task_id;
    }

    public String getSubmission_id()
    {
        return submission_id;
    }

    public void setSubmission_id(String submission_id)
    {
        this.submission_id = submission_id;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getResource()
    {
        return resource;
    }

    public void setResource(String resource)
    {
        this.resource = resource;
    }

    public String getRequest_id()
    {
        return request_id;
    }

    public void setRequest_id(String request_id)
    {
        this.request_id = request_id;
    }
}
