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
Based on their implementations service tasks need input and output parameters for their own processing. The sample project also needs some process parameters (bpmn file view -> properties -> Process Data):
![process_variables](/doc/images/process_variables.png)
| Name | Data Type |
|:-----|:----------|
|taskId|java.lang.Long|
|response|[com.corp.todo.Task](/handlers/mysql/src/main/java/com/corp/concepts/process/automation/handler/mysql/model/Task.java)|
|insertResult|String|

### REST Service Task
The [official documentation (a bit out-dated)](https://access.redhat.com/documentation/en-us/red_hat_jboss_bpm_suite/6.4/html/user_guide/rest_task) contains details on REST Service Task. It is also possible to derive information on input/output parameters and processing logic from WIH implementation class [`RESTWorkItemHandler.java`](https://github.com/kiegroup/jbpm/blob/master/jbpm-workitems/jbpm-workitems-rest/src/main/java/org/jbpm/process/workitem/rest/RESTWorkItemHandler.java).

The sample project set following parameters (Click on service task -> Properties -> Data Assignments):
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
The sample project set following parameters (Click on service task -> Properties -> Data Assignments):
