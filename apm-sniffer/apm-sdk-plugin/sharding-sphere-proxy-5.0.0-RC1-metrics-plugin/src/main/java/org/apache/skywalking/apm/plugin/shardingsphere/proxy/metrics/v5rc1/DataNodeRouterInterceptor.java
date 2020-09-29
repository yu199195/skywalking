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
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.skywalking.apm.agent.core.meter.Counter;
import org.apache.skywalking.apm.agent.core.meter.CounterMode;
import org.apache.skywalking.apm.agent.core.meter.MeterFactory;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * DataNodeRouterInterceptor enhances org.apache.shardingsphere.infra.router.DataNodeRouter,
 * creating metrics of router and sqlType counter.
 */
public class DataNodeRouterInterceptor implements InstanceMethodsAroundInterceptor {
    
    private static final Counter SELECT = MeterFactory.counter("sql_select").mode(CounterMode.INCREMENT).build();
    
    private static final Counter UPDATE = MeterFactory.counter("sql_update").mode(CounterMode.INCREMENT).build();
    
    private static final Counter DELETE = MeterFactory.counter("sql_delete").mode(CounterMode.INCREMENT).build();
    
    private static final Counter INSERT = MeterFactory.counter("sql_insert").mode(CounterMode.INCREMENT).build();
    
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) {
        SQLStatement sqlStatement = (SQLStatement) allArguments[0];
        if (sqlStatement instanceof InsertStatement) {
            INSERT.increment(1);
        } else if (sqlStatement instanceof DeleteStatement) {
            DELETE.increment(1);
        } else if (sqlStatement instanceof UpdateStatement) {
            UPDATE.increment(1);
        } else if (sqlStatement instanceof SelectStatement) {
            SELECT.increment(1);
        }
    }
    
    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) {
        RouteContext routeContext = (RouteContext) ret;
        if (null != routeContext) {
            RouteResult routeResult = routeContext.getRouteResult();
            for (RouteUnit routeUnit : routeResult.getRouteUnits()) {
                RouteMapper dataSourceMapper = routeUnit.getDataSourceMapper();
                MeterFactory.counter("route_datasource").tag("name", dataSourceMapper.getActualName()).mode(CounterMode.INCREMENT).build().increment(1);
                for (RouteMapper table : routeUnit.getTableMappers()) {
                    MeterFactory.counter("route_table").tag("name", table.getActualName()).mode(CounterMode.INCREMENT).build().increment(1);
                }
            }
        }
        return ret;
    }
    
    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
    }
}
