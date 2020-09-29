/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.plugin.shardingsphere.proxy.metrics.v5rc1;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.DoubleAdder;
import org.apache.skywalking.apm.agent.core.meter.Counter;
import org.apache.skywalking.apm.agent.core.meter.CounterMode;
import org.apache.skywalking.apm.agent.core.meter.MeterFactory;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * ChannelHandlerInterceptor enhances org.apache.shardingsphere.proxy.frontend.netty.FrontendChannelInboundHandler,
 * creating metrics of channel handler.
 */
public class ChannelHandlerInterceptor implements InstanceMethodsAroundInterceptor {
    
    private static final Counter COUNTER = MeterFactory.counter("proxy_total_request").mode(CounterMode.INCREMENT).build();
    
    private static final DoubleAdder CONNECTION = new DoubleAdder();
    
    public ChannelHandlerInterceptor() {
        MeterFactory.gauge("connection_total", CONNECTION::doubleValue).build();
    }
    
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) {
        collectMetrics(method.getName());
    }
    
    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) {
        return ret;
    }
    
    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
    }
    
    private void collectMetrics(String methodName) {
        if (MethodNameConstant.CHANNEL_READ.equals(methodName)) {
            COUNTER.increment(1);
        } else if (MethodNameConstant.CHANNEL_ACTIVE.equals(methodName)) {
            CONNECTION.add(1.0);
        } else if (MethodNameConstant.CHANNEL_INACTIVE.equals(methodName)) {
            CONNECTION.add(-1.0);
        }
    }
    
}
