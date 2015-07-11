package com.ebay.myriad.scheduler.yarn.interceptor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * An interceptor that wraps other interceptors. The Myriad{Fair,Capacity,Fifo}Scheduler classes
 * instantiate this class and allow interception of the Yarn scheduler events/method calls.
 *
 * The {@link CompositeInterceptor} allows other interceptors to be registered via {@link InterceptorRegistry}
 * and passes control to the registered interceptors whenever a event/method call is being intercepted.
 *
 */
public class CompositeInterceptor implements YarnSchedulerInterceptor, InterceptorRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeInterceptor.class);

    private Map<Class<?>, YarnSchedulerInterceptor> interceptors = Maps.newLinkedHashMap();
    private YarnSchedulerInterceptor myriadInitInterceptor;

    /**
     * Called by Myriad{Fair,Capacity,Fifo}Scheduler classes. Creates an instance of
     * {@link MyriadInitializationInterceptor}.
     */
    public CompositeInterceptor() {
        this.myriadInitInterceptor = new MyriadInitializationInterceptor(this);
    }

    @VisibleForTesting
    public void setMyriadInitInterceptor(YarnSchedulerInterceptor myriadInitInterceptor) {
        this.myriadInitInterceptor = myriadInitInterceptor;
    }

    @Override
    public void register(YarnSchedulerInterceptor interceptor) {
      interceptors.put(interceptor.getClass(), interceptor);
      LOGGER.info("Registered {} into the registry.", interceptor.getClass().getName());
    }

    /**
     * Allows myriad to be initialized via {@link #myriadInitInterceptor}. After myriad is initialized,
     * other interceptors will later register with this class via
     * {@link InterceptorRegistry#register(YarnSchedulerInterceptor)}.
     *
     * @param conf
     * @param yarnScheduler
     * @param rmContext
     * @throws IOException
     */
    @Override
    public void init(Configuration conf, AbstractYarnScheduler yarnScheduler, RMContext rmContext) throws IOException {
        myriadInitInterceptor.init(conf, yarnScheduler, rmContext);
    }

    @Override
    public void beforeRMNodeEventHandled(RMNodeEvent event, RMContext context) {
        for (YarnSchedulerInterceptor interceptor : interceptors.values()) {
            interceptor.beforeRMNodeEventHandled(event, context);
        }
    }

    @Override
    public void beforeSchedulerEventHandled(SchedulerEvent event) {
        for (YarnSchedulerInterceptor interceptor : interceptors.values()) {
            interceptor.beforeSchedulerEventHandled(event);
        }
    }

    @Override
    public void afterSchedulerEventHandled(SchedulerEvent event) {
        for (YarnSchedulerInterceptor interceptor : interceptors.values()) {
            interceptor.afterSchedulerEventHandled(event);
        }
    }
}
