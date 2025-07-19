package com.coreoz.plume.admin.db.daos;

import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.db.generated.QAdminUser;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.dml.SQLUpdateClause;

@Singleton
public class AdminUserDao extends CrudDaoQuerydsl<AdminUser> {

	@Inject
	public AdminUserDao(TransactionManagerQuerydsl transactionManager) {
		super(transactionManager, QAdminUser.adminUser, QAdminUser.adminUser.userName.asc());
	}

	public Optional<AdminUser> findByUserName(String userName) {
		return Optional.ofNullable(
			selectFrom()
				.where(QAdminUser.adminUser.userName.eq(userName))
				.fetchOne()
		);
	}

	public boolean existsWithUsername(Long idUser, String newUserName) {
		return existsWithPredicate(idUser, QAdminUser.adminUser.userName.eq(newUserName));
	}

	public boolean existsWithEmail(Long idUser, String newUserEmail) {
		return existsWithPredicate(idUser, QAdminUser.adminUser.email.eq(newUserEmail));
	}

	public void update(Long id, Long idRole, String userName, String email,
			String firstName, String lastName, String password) {
		SQLUpdateClause updateQuery = transactionManager
			.update(QAdminUser.adminUser)
			.where(QAdminUser.adminUser.id.eq(id))
			.set(QAdminUser.adminUser.idRole, idRole)
			.set(QAdminUser.adminUser.userName, userName)
			.set(QAdminUser.adminUser.email, email)
			.set(QAdminUser.adminUser.firstName, firstName)
			.set(QAdminUser.adminUser.lastName, lastName);

		if(password != null) {
			updateQuery.set(QAdminUser.adminUser.password, password);
		}

		updateQuery.execute();
	}

	private boolean existsWithPredicate(Long idUser, Predicate predicate) {
		return transactionManager
			.selectQuery()
			.select(SQLExpressions.selectOne())
			.from(QAdminUser.adminUser)
			.where(idUser != null ? QAdminUser.adminUser.id.ne(idUser) : null)
			.where(predicate)
			.fetchOne() != null;
	}

}
