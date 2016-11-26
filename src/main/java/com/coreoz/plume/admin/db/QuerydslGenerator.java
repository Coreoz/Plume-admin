package com.coreoz.plume.admin.db;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcDataSource;

import com.coreoz.plume.db.querydsl.generation.IdBeanSerializer;
import com.google.common.base.Throwables;
import com.querydsl.codegen.EntityType;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.codegen.DefaultNamingStrategy;
import com.querydsl.sql.codegen.MetaDataExporter;
import com.querydsl.sql.types.JSR310LocalDateTimeType;
import com.querydsl.sql.types.JSR310LocalDateType;
import com.querydsl.sql.types.JSR310LocalTimeType;
import com.querydsl.sql.types.JSR310ZonedDateTimeType;
import com.querydsl.sql.types.Type;

public class QuerydslGenerator {

	public static void main(String... args) {
		Configuration configuration = new Configuration(SQLTemplates.DEFAULT);
//		configuration.register(classType(JSR310InstantType.class));
		configuration.register(classType(JSR310LocalDateType.class));
		configuration.register(classType(JSR310LocalTimeType.class));
		configuration.register(classType(JSR310LocalDateTimeType.class));
//		configuration.register(classType(JSR310OffsetDateTimeType.class));
//		configuration.register(classType(JSR310OffsetTimeType.class));
		configuration.register(classType(JSR310ZonedDateTimeType.class));
		configuration.registerNumeric(1, 0, Boolean.class);
		configuration.registerNumeric(19, 0, Long.class);

		MetaDataExporter exporter = new MetaDataExporter();
		exporter.setPackageName("com.coreoz.plume.admin.db.generated");
		exporter.setTargetFolder(new File("src/main/java"));
		exporter.setTableNamePattern("PLM_%");
		exporter.setNamingStrategy(new DefaultNamingStrategy() {
			@Override
			public String getClassName(String tableName) {
				// replace "plm_" prefix with "admin_"
				return super.getClassName("admin_" + tableName.substring(4).toLowerCase());
			}
			@Override
			public String getDefaultVariableName(EntityType entityType) {
				String variableName = getClassName(entityType.getData().get("table").toString());
				return variableName.substring(0, 1).toLowerCase() + variableName.substring(1);
			}
		});
		exporter.setBeanSerializer(new IdBeanSerializer().setUseJacksonAnnotation(true));
		exporter.setColumnAnnotations(true);
		exporter.setConfiguration(configuration);

		try {
			exporter.export(h2Connection().getMetaData());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Connection h2Connection() {
		try {
			JdbcDataSource ds = new JdbcDataSource();
			ds.setURL("jdbc:h2:mem:test;MODE=Oracle");
			ds.setUser("sa");
			ds.setPassword("sa");
			Connection connection = ds.getConnection();

			String sqlStatements = new String(Files.readAllBytes(Paths.get("./sql/setup-mysql.sql")), StandardCharsets.UTF_8);

			for(String sqlStatement : sqlStatements.split(";")) {
				connection.createStatement().execute(sqlStatement);
			}

			return connection;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	private static Type<?> classType(Class<?> classType) {
		try {
			return (Type<?>) classType.newInstance();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

}
