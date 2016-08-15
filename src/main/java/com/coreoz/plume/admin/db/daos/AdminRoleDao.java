package com.coreoz.plume.admin.db.daos;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.entities.AdminRole;
import com.coreoz.plume.admin.db.entities.QAdminRole;
import com.coreoz.plume.db.TransactionManager;
import com.coreoz.plume.db.crud.CrudDao;

@Singleton
public class AdminRoleDao extends CrudDao<AdminRole> {

	@Inject
	public AdminRoleDao(TransactionManager transactionManager) {
		super(QAdminRole.adminRole, transactionManager, QAdminRole.adminRole.label.asc());
	}

	public boolean existsWithLabel(Long idRole, String label) {
		return searchCount(
			idRole != null ? QAdminRole.adminRole.id.ne(idRole) : null,
			QAdminRole.adminRole.label.eq(label)
		) > 0;
	}

}
