package com.infoworks.tasks;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.sql.executor.QueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTask<In extends Message, Out extends Response> extends ExecutableTask<In, Out> implements AutoCloseable {

    private static Logger LOG = LoggerFactory.getLogger("BaseTask");

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

    @Override
    public void close() throws Exception {
        if (getExecutor() != null) {
            getExecutor().close();
        }
    }

    public void closeDbConnections() {
        try {
            close();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public Out abort(In message) throws RuntimeException {
        LOG.info("❌ Default Abort Execution.");
        closeDbConnections();
        return super.abort(message);
    }
}
