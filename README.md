# ToDo List Evaluation Process
RedHat Process Automation Manager (RHPAM) provides the ability to integrate/utilize different technologies with built-in service tasks. It is also possible to implement custom service tasks where the out-of-box capabilities does not fulfill the needs. To implement a custom service task one needs to implement `org.kie.api.runtime.process.WorkItemHandler` interface of KIE API. 

For details, please check [official](https://access.redhat.com/documentation/en-us/red_hat_process_automation_manager/7.8/html-single/custom_tasks_and_work_item_handlers_in_business_central/index) documentation.

This project implements a sample BPM project on RHPAM that uses 3 service tasks:
* REST Service Task: Built-in service task that is used to call a test API of [JSONPlaceholder](http://jsonplaceholder.typicode.com/)
* Logger Service Task: Custom service task implementation that logs input data as JSON using [SLF4J](http://www.slf4j.org/) and [Jackson ObjectMapper](https://github.com/FasterXML/jackson-databind/blob/master/src/main/java/com/fasterxml/jackson/databind/ObjectMapper.java).
* MySQL Service Task: Custom service task that persists/inserts task data based on [JSONPlaceholder's ToDo API](http://jsonplaceholder.typicode.com/todos) response schema using [MySQL JDBC driver](https://dev.mysql.com/downloads/connector/j/).

![BPMN diagram](/doc/images/evaluation.bpmn.png)

## Steps to Activate
