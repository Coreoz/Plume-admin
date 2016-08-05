package com.coreoz.plume.admin.webservices.data.session;

import java.util.Collection;

import lombok.Value;

@Value(staticConstructor = "of")
public class SessionBo {

	private final String signed;
	private final String fullname;
	private final Collection<String> permissions;

}
