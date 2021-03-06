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
package com.corp.concepts.process.automation.handler.mysql.model;

import java.sql.JDBCType;

import com.corp.concepts.process.automation.handler.mysql.annotation.Field;

/**
 * Model that aligns with API call result data schema.
 * In case of any schema change just annotate relevant getter using {@link Field}
 * annotation with setting appropriate name and {@link JDBCType} setting.
 *  
 * @author Selcuk SERT - {@code selcuk.sert@gmail.com}
 * @see {@link Field}
 *
 */
public class Task {
	Long id;
	Long userId;
	String title;
	boolean completed;

	public Task() {
	}

	@Field(name = "id", type = JDBCType.BIGINT)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Field(name = "user_id", type = JDBCType.BIGINT)
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Field(name = "title", type = JDBCType.VARCHAR)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Field(name = "completed", type = JDBCType.BOOLEAN)
	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

}
