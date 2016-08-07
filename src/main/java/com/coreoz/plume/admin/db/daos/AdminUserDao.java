package com.coreoz.plume.admin.db.daos;

import java.util.Optional;

import javax.inject.Singleton;

import com.coreoz.plume.admin.db.entities.AdminUser;
import com.coreoz.plume.admin.db.entities.QAdminUser;
import com.coreoz.plume.db.TransactionManager;
import com.coreoz.plume.db.crud.CrudDao;
import com.querydsl.jpa.hibernate.HibernateUpdateClause;

@Singleton
public class AdminUserDao extends CrudDao<AdminUser> {

	public AdminUserDao(TransactionManager transactionManager) {
		super(QAdminUser.adminUser, transactionManager, QAdminUser.adminUser.userName.asc());
	}

	public Optional<AdminUser> findByUserName(String userName) {
		return Optional.ofNullable(searchOne(QAdminUser.adminUser.userName.eq(userName)));
	}

	public boolean existsWithUsername(Long userId, String newUserName) {
		return searchCount(
			userId != null ? QAdminUser.adminUser.id.ne(userId) : null,
			QAdminUser.adminUser.userName.eq(newUserName)
		) > 0;
	}

	public boolean existsWithEmail(Long userId, String newUserEmail) {
		return searchCount(
			userId != null ? QAdminUser.adminUser.id.ne(userId) : null,
			QAdminUser.adminUser.email.eq(newUserEmail)
		) > 0;
	}

	public void update(Long id, Long idRole, String userName, String email,
			String firstName, String lastName, String password) {
		transactionManager.queryDslExecute(query -> {
			HibernateUpdateClause updateQuery = query
				.update(QAdminUser.adminUser)
				.where(QAdminUser.adminUser.id.eq(id))
				.set(QAdminUser.adminUser.userName, userName)
				.set(QAdminUser.adminUser.email, email)
				.set(QAdminUser.adminUser.firstName, firstName)
				.set(QAdminUser.adminUser.lastName, lastName);

			if(password != null) {
				updateQuery.set(QAdminUser.adminUser.password, password);
			}

			updateQuery.execute();
		});
	}

}
