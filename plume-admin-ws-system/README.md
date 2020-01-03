Plume Admin System
==================

This module adds two API to manage:
- Logger levels with [Logback](http://logback.qos.ch/)
- Scheduled tasks with [Plume Scheduler](https://github.com/Coreoz/Plume/tree/master/plume-scheduler)

Installation
------------

1. Maven dependency:
```xml
<dependency>
    <groupId>com.coreoz</groupId>
    <artifactId>plume-admin-ws-system</artifactId>
</dependency>
```
2. Configure the new permission for the admin user to access the API: `SystemAdminPermissions.MANAGE_SYSTEM`
3. Make sure [Plume Scheduler](https://github.com/Coreoz/Plume/tree/master/plume-scheduler) is correctly configured.
