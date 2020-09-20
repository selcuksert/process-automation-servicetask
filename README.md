# ToDo List Evaluation Process
RedHat Process Automation Manager (RHPAM) provides the ability to integrate/utilize different technologies with built-in service tasks. It is also possible to implement custom service tasks where the out-of-box capabilities does not fulfill the needs. To implement a custom service task one needs to implement `org.kie.api.runtime.process.WorkItemHandler` interface of KIE API. 

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
In order to activate custom service tasks (**CustomLogTask**, **MySQLTask**) in Business Central you should follow the procedure set (Chapter 1 - Procedures 1-4) in [official documentation](https://access.redhat.com/documentation/en-us/red_hat_process_automation_manager/7.8/html-single/custom_tasks_and_work_item_handlers_in_business_central/index#manage-service-tasks-proc_custom-tasks):
| | |
|:-------------------------:|:-------------------------:|
|![import_st_c](/doc/images/service_task_c_file.png)|![import_st_u](/doc/images/service_task_u_file.png)|
|![import_st_ac](/doc/images/service_task_a_file.png)|![import_st_ac](/doc/images/service_task_ac_file.png)|

Then you need to activate them via ON/OFF switch right hand side of imported ones:
![service_task_list](/doc/images/service_task_list.png)

