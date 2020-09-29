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
import org.apache.skywalking.apm.agent.core.meter.Counter;
import org.apache.skywalking.apm.agent.core.meter.CounterMode;
import org.apache.skywalking.apm.agent.core.meter.MeterFactory;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * TransactionInterceptor enhances org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager,
 * creating metrics of transaction commit or rollback counter.
 */
public class TransactionInterceptor implements InstanceMethodsAroundInterceptor {
    
    private static final Counter COMMIT = MeterFactory.counter("proxy_transaction_commit").mode(CounterMode.INCREMENT).build();
    
    private static final Counter ROLLBACK = MeterFactory.counter("proxy_transaction_rollback").mode(CounterMode.INCREMENT).build();
    
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) {
        String methodName = method.getName();
        if (MethodNameConstant.COMMIT.equals(methodName)) {
            COMMIT.increment(1);
        } else if (MethodNameConstant.ROLL_BACK.equals(methodName)) {
            ROLLBACK.increment(1);
        }
    }
    
    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) {
        return ret;
    }
    
    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
    }
}
