/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.scheduler.event.handlers;

import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import org.apache.myriad.scheduler.ReconcileService;
import org.apache.myriad.scheduler.event.ReRegisteredEvent;
import org.apache.myriad.state.SchedulerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles and logs mesos re-register events
 */
public class ReRegisteredEventHandler implements EventHandler<ReRegisteredEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReRegisteredEventHandler.class);

  @Inject
  private SchedulerState state;

  @Inject
  private ReconcileService reconcileService;

  @Override
  public void onEvent(ReRegisteredEvent event, long sequence, boolean endOfBatch) throws Exception {
    LOGGER.info("Framework re-registered: {}", event);
    reconcileService.reconcile(event.getDriver());
  }
}
