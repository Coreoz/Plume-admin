1.0.0-rc3
=========
- in `pom.xml` replace `<artifactId>plume-admin</artifactId>` by `<artifactId>plume-admin-ws</artifactId>`
- in all files of the project replace 
`com.coreoz.plume.admin.webservices.security.RestrictToAdmin` by `com.coreoz.plume.admin.jersey.feature.RestrictToAdmin`
- in all files of the project replace 
`com.coreoz.plume.admin.jersey.WebSessionPermission` by `com.coreoz.plume.admin.websession.WebSessionPermission`
- in all files of the project replace 
`com.coreoz.plume.admin.webservices.context.WebSessionAdminFactory` by `com.coreoz.plume.admin.jersey.context.WebSessionAdminFactory`
- in all files of the project replace 
`com.coreoz.plume.admin.webservices.security.AdminSecurityFeature` by `com.coreoz.plume.admin.jersey.feature.AdminSecurityFeature`
- in all files of the project replace `GuiceAdminModule` by `GuiceAdminWsModule`
- in all files of the project replace `GuiceAdminWithDefaultsModule` `GuiceAdminWsWithDefaultsModule`
