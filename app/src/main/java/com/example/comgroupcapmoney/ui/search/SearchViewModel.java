package com.example.comgroupcapmoney.ui.search;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.data.repository.TransactionRepository;
import com.example.comgroupcapmoney.model.SearchPage;
import com.example.comgroupcapmoney.model.Transaction;
import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends AndroidViewModel {

    public static final int STATE_IDLE = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_RESULTS = 2;
    public static final int STATE_EMPTY = 3;
    public static final int STATE_ERROR = 4;

    private final TransactionRepository repository;

    private final MutableLiveData<List<Transaction>> items = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> state = new MutableLiveData<>(STATE_IDLE);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loadingMore = new MutableLiveData<>(false);

    private String query = "";
    private int page = 0;
    private boolean hasMore = false;
    // Moi lan tim moi tang seq: ket qua cua tu khoa cu ve muon thi bo qua
    private int seq = 0;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
    }

    public LiveData<List<Transaction>> getItems() { return items; }
    public LiveData<Integer> getState() { return state; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getLoadingMore() { return loadingMore; }

    public String getQuery() {
        return query;
    }

    // Goi sau khi nguoi dung ngung go 400ms hoac bam nut tim tren ban phim
    public void setQuery(String raw) {
        String q = raw != null ? raw.trim() : "";
        if (q.length() > 50) {
            q = q.substring(0, 50);
        }
        Integer current = state.getValue();
        if (q.equals(query) && current != null && current != STATE_ERROR && current != STATE_IDLE) {
            return;
        }
        query = q;
        if (q.isEmpty()) {
            seq++;
            page = 0;
            hasMore = false;
            items.setValue(new ArrayList<>());
            loadingMore.setValue(false);
            state.setValue(STATE_IDLE);
            return;
        }
        loadFirstPage();
    }

    // Quay lai tu hop chi tiet (vua sua/xoa): tim lai tu khoa hien tai
    public void refresh() {
        if (!query.isEmpty()) {
            loadFirstPage();
        }
    }

    public void retry() {
        refresh();
    }

    private void loadFirstPage() {
        final int mySeq = ++seq;
        final String q = query;
        state.setValue(STATE_LOADING);
        loadingMore.setValue(false);
        repository.search(q, 1, new TransactionRepository.ResultCallback<SearchPage>() {
            @Override
            public void onSuccess(SearchPage data) {
                if (mySeq != seq) return;
                List<Transaction> list = data != null ? data.getItems() : new ArrayList<>();
                page = 1;
                hasMore = data != null && data.hasMore();
                items.setValue(list);
                state.setValue(list.isEmpty() ? STATE_EMPTY : STATE_RESULTS);
            }

            @Override
            public void onError(String message) {
                if (mySeq != seq) return;
                items.setValue(new ArrayList<>());
                error.setValue(message);
                state.setValue(STATE_ERROR);
            }
        });
    }

    // Cuon gan cuoi danh sach thi tai trang sau, khong goi trung khi dang tai
    public void loadMore() {
        if (!hasMore || Boolean.TRUE.equals(loadingMore.getValue())) return;
        Integer current = state.getValue();
        if (current == null || current != STATE_RESULTS) return;

        final int mySeq = seq;
        final int nextPage = page + 1;
        loadingMore.setValue(true);
        repository.search(query, nextPage, new TransactionRepository.ResultCallback<SearchPage>() {
            @Override
            public void onSuccess(SearchPage data) {
                if (mySeq != seq) return;
                loadingMore.setValue(false);
                page = nextPage;
                hasMore = data != null && data.hasMore();
                List<Transaction> merged = new ArrayList<>(items.getValue() != null ? items.getValue() : new ArrayList<>());
                if (data != null) {
                    merged.addAll(data.getItems());
                }
                items.setValue(merged);
            }

            @Override
            public void onError(String message) {
                if (mySeq != seq) return;
                loadingMore.setValue(false);
                // Giu danh sach da co; cuon lai se thu tai tiep
            }
        });
    }
}
