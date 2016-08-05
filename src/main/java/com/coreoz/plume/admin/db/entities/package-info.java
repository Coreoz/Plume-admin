/**
 * Enable to use HibernateIdGenerator on identifiers in the package: <code>@GeneratedValue(generator = HibernateIdGenerator.NAME)</code>
 */
@org.hibernate.annotations.GenericGenerator(name = com.coreoz.plume.db.hibernate.HibernateIdGenerator.NAME, strategy = "com.coreoz.plume.db.hibernate.HibernateIdGenerator")
package com.coreoz.plume.admin.db.entities;