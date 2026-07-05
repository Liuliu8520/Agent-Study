package com.agentstudy.admin.audit;

import java.util.List;

public interface OperationLogRepository {

    OperationLog save(OperationLog log);

    List<OperationLog> search(OperationLogQuery query);
}
