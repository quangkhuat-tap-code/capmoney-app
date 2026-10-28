package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

// Mot trang ket qua GET /api/transactions/search (4A-2)
public class SearchPage {

    @SerializedName("items")
    private List<Transaction> items;

    @SerializedName("page")
    private int page;

    @SerializedName("has_more")
    private boolean hasMore;

    public List<Transaction> getItems() {
        return items != null ? items : new ArrayList<>();
    }

    public int getPage() {
        return page;
    }

    public boolean hasMore() {
        return hasMore;
    }
}
