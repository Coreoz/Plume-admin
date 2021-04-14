package com.coreoz.plume.admin;

import java.util.function.Predicate;

import okhttp3.Request;

@FunctionalInterface
public interface RequestPredicate extends Predicate<Request> {

	static RequestPredicate alwaysTrue() {
		return request -> true;
	}

    default RequestPredicate filterEndpoint(String endpointToFilter) {
        return request -> test(request)
        	&& OkHttpMatchers.matchRequestEndpoints(request.url(), endpointToFilter);
    }
//
//    public OkHttpLoggerInterceptorFiltersBuilder filterMethod(HttpMethod method) {
//        this.filteredMethods.add(method.name());
//        return this;
//    }

}
