# ToDo List Evaluation Process
RedHat Process Automation Manager (RHPAM) provides the ability to integrate/utilize different technologies with built-in service tasks. It is also possible to implement custom service tasks where out-of-box capabilities do not fulfill the needs. To implement a custom service task one needs to implement `org.kie.api.runtime.process.WorkItemHandler` interface of KIE API. 

For details, please check [official documentation](https://access.redhat.com/documentation/en-us/red_hat_process_automation_manager/7.8/html-single/custom_tasks_and_work_item_handlers_in_business_central/index).

This project implements a sample BPM project on RHPAM that uses 3 service tasks:
* REST Service Task: Built-in service task that is used to call a test API of [JSONPlaceholder](http://jsonplaceholder.typicode.com/)
* [Logger Service Task](/handlers/log): Custom service task implementation that logs input data as JSON using [SLF4J](http://www.slf4j.org/) and [Jackson ObjectMapper](https://github.com/FasterXML/jackson-databind/blob/master/src/main/java/com/fasterxml/jackson/databind/ObjectMapper.java).
* [MySQL Service Task](/handlers/mysql): Custom service task that persists/inserts task data based on [JSONPlaceholder's ToDo API](http://jsonplaceholder.typicode.com/todos) response schema using [MySQL JDBC driver](https://dev.mysql.com/downloads/connector/j/).

![BPMN diagram](/doc/images/evaluation.bpmn.png)

## Service Task Implementations
The two [custom service task types](/handlers) were implemented using KIE Java API which are essentially `WorkItemHandler`s. Due to their nature they are customizable and re-usable throughout processes and projects in Business Central. In order to use them you need to import packaged versions (JAR files) and enable them in Business Central. The [official documentation](https://access.redhat.com/documentation/en-us/red_hat_process_automation_manager/7.8/html-single/custom_tasks_and_work_item_handlers_in_business_central/index) contains relevant details throughout implementation and deployment journeys.

The handler implementations in this project can be build and deployed to an artifact server (here docker hosted Artifactory) using following maven goals:
```bash
mvn clean package deploy
```
The resultant JAR file in `target` folder of project is the packaged version that can be imported to Business Central. 

## Importing Custom Service Tasks
In order to activate custom service tasks (**CustomLogTask**, **MySQLTask**) in Business Central you should follow the procedure set (Chapter 1 - Procedures 1->4) in [official documentation](https://access.redhat.com/documentation/en-us/red_hat_process_automation_manager/7.8/html-single/custom_tasks_and_work_item_handlers_in_business_central/index#manage-service-tasks-proc_custom-tasks):
| | |
|:-------------------------:|:-------------------------:|
|![import_st_c](/doc/images/service_task_c_file.png)|![import_st_u](/doc/images/service_task_u_file.png)|
|![import_st_ac](/doc/images/service_task_a_file.png)|![import_st_ac](/doc/images/service_task_ac_file.png)|

Then you need to activate them via ON/OFF switch right hand side of imported ones:
![service_task_list](/doc/images/service_task_list.png)

## Importing Sample Business Process Project
There also exists a [business project](https://github.com/selcuksert/process-automation-servicetask-repo) which uses custom service tasks mentioned here. For a quick start you can directly import the project into Business Central just using Import Project button with setting the repository URL (https://github.com/selcuksert/process-automation-servicetask-repo) in your designated space:
| | |
|:----:|:----:|
|![import_p](/doc/images/import_project.png)|![import_c](/doc/images/import_project_c.png)|

## Activating Service Tasks for Project
The procedure set (Chapter 1 - Procedures 6->10) in [official documentation](https://access.redhat.com/documentation/en-us/red_hat_process_automation_manager/7.8/html-single/custom_tasks_and_work_item_handlers_in_business_central/index#manage-service-tasks-proc_custom-tasks) need to be applied to activate service tasks for any business process project. For the sample project we need *Rest, CustomLogTask, MySQLTask* to be installed:
| | |
|:-------------------------:|:-------------------------:|
![st_project_install](/doc/images/st_project_inst.png) |![project_st_list](/doc/images/project_st_list.png)

It is also important to note that in deployment settings (MVEL) we need to pass `classLoader` parameter to constructor of WorkItemHandler classes of *Rest, MySQLTask* tasks as KIE classloader needs to be in action to avoid `ClassNotFound` exceptions during process execution:
![project_wih_settings.png](/doc/images/project_wih_settings.png)

## Service Task Parameters
Based on their implementations service tasks need input and output parameters for their own processing. The sample project also needs some process parameters (.bpmn file view -> properties -> Process Data):
![process_variables](/doc/images/process_variables.png)
| Name | Data Type |Description|
|:-----|:----------|:----|
|taskId|java.lang.Long|Process variable to store task ID input of [JSONPlaceholder's ToDo API](http://jsonplaceholder.typicode.com/todos)|
|response|[com.corp.todo.Task](/handlers/mysql/src/main/java/com/corp/concepts/process/automation/handler/mysql/model/Task.java)|Custom model that aligns with ToDo API task data schema|
|insertResult|String|SQL statement execution result|

### REST Service Task
The [official documentation (a bit out-dated)](https://access.redhat.com/documentation/en-us/red_hat_jboss_bpm_suite/6.4/html/user_guide/rest_task) contains details on REST Service Task. It is also possible to derive information on input/output parameters and processing logic from WIH implementation class [`RESTWorkItemHandler.java`](https://github.com/kiegroup/jbpm/blob/master/jbpm-workitems/jbpm-workitems-rest/src/main/java/org/jbpm/process/workitem/rest/RESTWorkItemHandler.java).

The sample project set following parameters (Click on service task -> Properties (Pen icon right hand side) -> Data Assignments):
![rest_st_params](/doc/images/rest_st_params.png)
| Name | Data Type | Source |
|:-----|:----------|:-------|
|AcceptCharset|String|UTF-8|
|AcceptHeader|String|application/json|
|ContentType|String|application/json|
|ContentTypeCharset|String|UTF-8|
|HandleResponseErrors|String|true|
|Method|String|GET|
|ResultClass|String|[com.corp.todo.Task](/handlers/mysql/src/main/java/com/corp/concepts/process/automation/handler/mysql/model/Task.java)|
|Url|String|h<span>ttps://jsonplaceholder.typicode.com/todos/#{taskId}</span>|

| Name | Data Type | Target |
|:-----|:----------|:-------|
|Result|[com.corp.todo.Task](/handlers/mysql/src/main/java/com/corp/concepts/process/automation/handler/mysql/model/Task.java)|response|

*One can directly access to any process data variable using `#{paramName.fieldName}`*

### Log Service Task
The sample project set following parameters (Click on service task -> Properties (Pen icon right hand side) -> Data Assignments):
| | |
|:---:|:---:|
|![log_completed](/doc/images/log_completed.png)|![log_not_completed](/doc/images/log_not_completed.png)|
|![log_rest](/doc/images/log_rest.png)|![log_insert](/doc/images/log_insert.png)|

|Task| Name | Data Type | Source |
|:----|:----|:---|:---|
|Completed|dataToLog|Object|#{response.completed}|
|Completed|prettyPrint|String|true|
|Not Completed|dataToLog|Object|#{response.completed}|
|Not Completed|prettyPrint|String|true|
|REST|dataToLog|Object|response|
|REST|prettyPrint|String|true|
|Insert|dataToLog|Object|insertResult|
|Insert|prettyPrint|String|true|

### MySQL Service Task
The sample project set following parameters (Click on service task -> Properties (Pen icon right hand side) -> Data Assignments):
![mysql_params](/doc/images/mysql_params.png)
| Name | Data Type | Source |
|:-----|:----------|:-------|
|username|String|`MySQL DB Username`|
|password|String|`MySQL DB Password`|
|url|String|`jdbc:mysql://${host}:${port}/{dbName}`|
|taskToInsert|Object|response|

| Name | Data Type | Target |
|:-----|:----------|:-------|
|completed|String|insertResult|

## Triggering the Process
The Business Central comes with Swagger Web Interface (default URL: http(s)://${BC_HOST}:${BC_PORT}/kie-server/docs). For details please refer to the [official documentation](https://access.redhat.com/documentation/en-us/red_hat_process_automation_manager/7.8/html-single/interacting_with_red_hat_process_automation_manager_using_kie_apis/index).

After successfully build and deploy the project to KIE server one can use POST endpoint `/server/containers/{containerId}/processes/{processId}/instances` under *Process Instances* group to trigger the process:
| | |
|:-----|:----------|
|![kie-server](/doc/images/kie-server.png)|![swagger](/doc/images/swagger.png)|

The endpoint requires two parameters:
* `container id` where the process definition resides -> Deployment
* `process id` that new instance should be created from -> Definition Id

These two parameters can be retrieved via process definition page:
| | |
|:-----|:----------|
|![process_def](/doc/images/process_def.png)|![process_def_det](/doc/images/process_def_det.png)|

The body of the post request is the taskId (recall process data that we use in API URL):
```json
{
  "taskId":#
}
```

The result of the API call is the ID of initiated process:
![swagger_result](/doc/images/swagger_result.png)

## The conditions on Exclusive Gateway
An Exclusive Gateway (or XOR Gateway) joins alternative paths within a Process flow. The flow continues through only one of the paths that satisfies condition set on it. 
![exc_gw](/doc/images/exc_gw.png)

The sample project joins two paths on an Exclusive Gateway with following conditions:
| | |
|:-----|:-----|
|![exc_gw_comp](/doc/images/exc_gw_comp.png)|![exc_gw_not_comp](/doc/images/exc_gw_not_comp.png)|

## Result of the Process Execution
The sample project has a business process design that persists data of given task ID to designated MySQL DB if it is **completed**.

### Not Completed Task:
The external test API returns following data for task [#1](https://jsonplaceholder.typicode.com/todos/1):
```json
{
  "userId": 1,
  "id": 1,
  "title": "delectus aut autem",
  "completed": false
}
```

After execution via Swagger UI the stdout log stream of BC server is as follows (the result of custom log service task executions):
```bash
curl -X POST "http://localhost:8080/kie-server/services/rest/server/containers/ToDo_1.0.0-SNAPSHOT/processes/ToDo.evaluation/instances" -H "accept: application/json" -H "content-type: application/json" -d "{ \"taskId\": 1}"
```

```
redhat-pam     | 20-09-2020 19:07:14,817 INFO  [com.corp.concepts.process.automation.handler.log.LogTaskWorkItemHandler] (default task-34) [CustomLogTask-2] Data:
redhat-pam     |  {
redhat-pam     |   "userId" : 1,
redhat-pam     |   "id" : 1,
redhat-pam     |   "title" : "delectus aut autem",
redhat-pam     |   "completed" : false
redhat-pam     | }
redhat-pam     | 20-09-2020 19:07:14,836 INFO  [com.corp.concepts.process.automation.handler.log.LogTaskWorkItemHandler] (default task-34) [CustomLogTask-3] Data:
redhat-pam     |  "false"
```

The MySQL DB table contains no data:
```bash
mysql> use pam;
Database changed
mysql> show columns from task;
+-----------+------------+------+-----+---------+-------+
| Field     | Type       | Null | Key | Default | Extra |
+-----------+------------+------+-----+---------+-------+
| id        | bigint     | YES  |     | NULL    |       |
| user_id   | bigint     | YES  |     | NULL    |       |
| title     | mediumtext | YES  |     | NULL    |       |
| completed | tinyint(1) | YES  |     | NULL    |       |
+-----------+------------+------+-----+---------+-------+
4 rows in set (0.01 sec)

mysql> select * from task;
Empty set (0.00 sec)
```

### Completed Task:
The external test API returns following data for task [#44](https://jsonplaceholder.typicode.com/todos/44):
```json
{
  "userId": 3,
  "id": 44,
  "title": "cum debitis quis accusamus doloremque ipsa natus sapiente omnis",
  "completed": true
}
```

After execution via Swagger UI the stdout log stream of BC server is as follows (the result of custom log service task executions):
```bash
curl -X POST "http://localhost:8080/kie-server/services/rest/server/containers/ToDo_1.0.0-SNAPSHOT/processes/ToDo.evaluation/instances" -H "accept: application/json" -H "content-type: application/json" -d "{ \"taskId\": 44}"
```

```
redhat-pam     | 20-09-2020 19:26:18,417 INFO  [com.corp.concepts.process.automation.handler.log.LogTaskWorkItemHandler] (default task-34) [CustomLogTask-5] Data:
redhat-pam     |  {
redhat-pam     |   "userId" : 3,
redhat-pam     |   "id" : 44,
redhat-pam     |   "title" : "cum debitis quis accusamus doloremque ipsa natus sapiente omnis",
redhat-pam     |   "completed" : true
redhat-pam     | }
redhat-pam     | 20-09-2020 19:26:18,438 INFO  [com.corp.concepts.process.automation.handler.log.LogTaskWorkItemHandler] (default task-34) [CustomLogTask-6] Data:
redhat-pam     |  "true"
redhat-pam     | 20-09-2020 19:26:18,786 INFO  [com.corp.concepts.process.automation.handler.mysql.MySQLWorkItemHandler] (default task-34) Insert statement: com.mysql.jdbc.JDBC42PreparedStatement@3224a981: insert into task (completed,user_id,title,id) values (1,3,'cum debitis quis accusamus doloremque ipsa natus sapiente omnis',44)
redhat-pam     | 20-09-2020 19:26:18,803 INFO  [com.corp.concepts.process.automation.handler.log.LogTaskWorkItemHandler] (default task-34) [CustomLogTask-8] Data:
redhat-pam     |  true
```

The MySQL DB table contains data of related task:
```bash
mysql> use pam;
Database changed
mysql> show columns from task;
+-----------+------------+------+-----+---------+-------+
| Field     | Type       | Null | Key | Default | Extra |
+-----------+------------+------+-----+---------+-------+
| id        | bigint     | YES  |     | NULL    |       |
| user_id   | bigint     | YES  |     | NULL    |       |
| title     | mediumtext | YES  |     | NULL    |       |
| completed | tinyint(1) | YES  |     | NULL    |       |
+-----------+------------+------+-----+---------+-------+
4 rows in set (0.01 sec)

mysql> select * from task;
+------+---------+-----------------------------------------------------------------+-----------+
| id   | user_id | title                                                           | completed |
+------+---------+-----------------------------------------------------------------+-----------+
|   44 |       3 | cum debitis quis accusamus doloremque ipsa natus sapiente omnis |         1 |
+------+---------+-----------------------------------------------------------------+-----------+
1 row in set (0.00 sec)
```

## Docker
The sample project uses Docker images from [docker-images](https://github.com/selcuksert/docker-images) repository:
```bash
docker-compose -f ./docker-images/redhat/pam/docker-compose.yml mysql pam openldap mavenserver
```

