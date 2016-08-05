package com.coreoz.plume.admin.services.hash;

public interface HashService {

	String hashPassword(String password);

	boolean checkPassword(String candidate, String hashed);

}
