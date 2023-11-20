package org.nimboscloud.server.services;

import org.nimboscloud.JobFunction.*;

public class ExecuteManager {

    public ExecuteManager(){}

    public byte[] executeJobFuncion(byte[] clientCode) throws JobFunctionException {

        byte[] result = JobFunction.execute(clientCode);

        return result;
    }
}
