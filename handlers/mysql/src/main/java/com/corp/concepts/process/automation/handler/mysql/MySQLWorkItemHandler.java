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
package com.corp.concepts.process.automation.handler.mysql;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.corp.concepts.process.automation.handler.mysql.annotation.Field;
import com.corp.concepts.process.automation.handler.mysql.model.Task;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

//@formatter:off
@Wid(widfile="MySQLTask.wid", name="MySQLTask",
        displayName="MySQLTask",
        defaultHandler="mvel: new com.corp.concepts.process.automation.handler.mysql.MySQLWorkItemHandler(classLoader)",
        documentation = "mysql/index.html",
        category = "${artifactId}",
        icon = "MySQLTask.png",
        parameters={
            @WidParameter(name="url"),
            @WidParameter(name="username"),
            @WidParameter(name="password"),
            @WidParameter(name="taskToInsert")
        },
        results={
            @WidResult(name="completed")
        },
        mavenDepends={
            @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
        },
        serviceInfo = @WidService(category = "${artifactId}", description = "${description}",
                keywords = "MySQL, JDBC",
                action = @WidAction(title = "MySQL ServiceTask")
        )
)
//@formatter:on
public class MySQLWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {
	private static final Logger logger = LoggerFactory.getLogger(MySQLWorkItemHandler.class);
	private boolean initCompleted;
	private String fieldsStr = "";
	private String valuesStr = "";
	private ObjectMapper objectMapper;
	private int size;

	public MySQLWorkItemHandler(ClassLoader classLoader) {
		try {
			Class.forName("com.mysql.jdbc.Driver", true, classLoader).newInstance();
			this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			initCompleted = true;
		} catch (Exception e) {
			logger.error("Error: ", e);
			initCompleted = false;
		}
	}

	public MySQLWorkItemHandler() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			initCompleted = true;
		} catch (Exception e) {
			logger.error("Error: ", e);
			initCompleted = false;
		}
	}

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		if (!initCompleted) {
			manager.abortWorkItem(workItem.getId());
			return;
		}

		try {
			RequiredParameterValidator.validate(this.getClass(), workItem);

			// parameters
			String url = (String) workItem.getParameter("url");
			String username = (String) workItem.getParameter("username");
			String password = (String) workItem.getParameter("password");
			Object taskToInsert = workItem.getParameter("taskToInsert");

			// work item logic
			boolean completed = insertData(url, username, password, taskToInsert);

			// return results
			Map<String, Object> results = new HashMap<String, Object>();
			results.put("completed", completed);

			manager.completeWorkItem(workItem.getId(), results);
		} catch (Throwable cause) {
			handleException(cause);
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.error(workItem.getName() + " aborted.");
	}

	private boolean insertData(String url, String username, String password, Object taskToInsert) {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, username, password);
			// ------------------------------------------------------------------
			// Just modify the content of hash map and Task model in case of
			// any schema change

			PreparedStatement pStmt = generatePreparedStatement(connection, taskToInsert);
			pStmt.execute();
			
			return true;
		} catch (SQLException e) {
			logger.error("Error: ", e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error("Error: ", e);
				}
			}
		}

		return false;
	}

	private PreparedStatement generatePreparedStatement(Connection connection, Object taskToInsert)
			throws SQLException {

		List<Method> methods = Arrays.asList(Task.class.getMethods());
		List<String> fieldList = methods.stream().filter(m -> m.isAnnotationPresent(Field.class))
				.map(m -> m.getAnnotation(Field.class).name()).collect(Collectors.toList());

		fieldsStr = "";
		valuesStr = "";
		size = fieldList.size();

		fieldList.forEach(field -> {
			if (size > 1) {
				fieldsStr += field + ",";
				valuesStr += "?,";
			} else {
				fieldsStr += field;
				valuesStr += "?";
			}
			size--;
		});

		String insertSql = "insert into task (" + fieldsStr + ") values (" + valuesStr + ")";

		PreparedStatement pStmt = connection.prepareStatement(insertSql);
		Task task = objectMapper.convertValue(taskToInsert, Task.class);

		fieldList.stream().forEach(field -> {
			try {
				Method method = methods.stream()
						.filter(m -> m.isAnnotationPresent(Field.class) && m.getAnnotation(Field.class).name() == field)
						.findFirst().get();
				pStmt.setObject((fieldList.indexOf(field) + 1), method.invoke(task),
						method.getAnnotation(Field.class).type());
			} catch (Exception e) {
				logger.error("Error: ", e);
			}
		});

		logger.info(String.format("Insert statement: %s", pStmt.toString()));

		return pStmt;
	}

}
