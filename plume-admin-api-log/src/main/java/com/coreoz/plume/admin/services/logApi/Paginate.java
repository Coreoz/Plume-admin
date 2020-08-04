package com.coreoz.plume.admin.services.logApi;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor(staticName = "of")
public class Paginate<T> {
    private long total;
    private List<T> list;
    private int currentPage;
    private int maxPage;
}
