/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.corp.concepts.process.automation.handler.log;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

//@formatter:off
@Wid(widfile="CustomLogTask.wid", name="CustomLogTask",
        displayName="CustomLogTask",
        defaultHandler="mvel: new com.corp.concepts.process.automation.handler.log.LogTaskWorkItemHandler()",
        documentation = "log/index.html",
        category = "${artifactId}",
        icon = "icon.png",
        parameters={
            @WidParameter(name="dataToLog"),
            @WidParameter(name="prettyPrint")
        },
        results={
            @WidResult(name="data")
        },
        mavenDepends={
                @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${artifactId}", description = "${description}",
                keywords = "Log, Slf4j",
                action = @WidAction(title = "Log ServiceTask")
        )
)
//@formatter:on
public class LogTaskWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {
	private static final Logger logger = LoggerFactory.getLogger(LogTaskWorkItemHandler.class);
	private static final String DATA_TO_LOG = "dataToLog";
	private static final String PRETTY_PRINT = "prettyPrint";
	private static final String DATA = "data";

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		try {
			RequiredParameterValidator.validate(this.getClass(), workItem);

			// parameters
			Object dataToLog = workItem.getParameter(DATA_TO_LOG);
			String isPrettyStr = (String) workItem.getParameter(PRETTY_PRINT);

			if (dataToLog == null) {
				logger.error("No data to log is provided!");
				manager.abortWorkItem(workItem.getId());
				return;
			}

			boolean isPretty = Boolean.valueOf(isPrettyStr);

			// work item logic
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectWriter objectWriter = null;

			objectWriter = (isPretty) ? objectMapper.writerWithDefaultPrettyPrinter() : objectMapper.writer();

			String dataAsStr = objectWriter.writeValueAsString(dataToLog);

			logger.info(String.format("Data:\n %s", dataAsStr));

			// return data as-is
			Map<String, Object> results = new HashMap<String, Object>();
			results.put(DATA, dataToLog);

			manager.completeWorkItem(workItem.getId(), results);
		} catch (Throwable cause) {
			handleException(cause);
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// Do nothing, this work item cannot be aborted
	}
}
