package com.infoworks.tasks;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.sql.executor.QueryExecutor;

public abstract class BaseTask<In extends Message, Out extends Response> extends ExecutableTask<In, Out>{

    public BaseTask(Property... properties) {
        super(properties);
    }

    private QueryExecutor executor;

    public QueryExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(QueryExecutor executor) {
        this.executor = executor;
    }
}
