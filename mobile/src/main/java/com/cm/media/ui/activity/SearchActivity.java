package com.cm.media.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.cm.media.R;
import com.cm.media.databinding.ActivitySearchBinding;
import com.cm.media.entity.ViewStatus;
import com.cm.media.entity.search.SearchResult;
import com.cm.media.repository.db.AppDatabase;
import com.cm.media.repository.db.entity.SearchHistory;
import com.cm.media.ui.adapter.SearchResultListAdapter;
import com.cm.media.viewmodel.SearchViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static androidx.cursoradapter.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;

public class SearchActivity extends AppCompatActivity {
    private ActivitySearchBinding binding;
    private SearchViewModel viewModel;

    public static void navi2Search(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.root);
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        binding.setViewModel(viewModel);
        SearchResultListAdapter adapter = new SearchResultListAdapter(Collections.emptyList());
        binding.recyclerView.setAdapter(adapter);
        viewModel.getSearchResult().observe(this, adapter::setNewData);
        viewModel.getViewStatus().observe(this, viewStatus -> binding.setViewStatus(viewStatus));
     /*   binding.editKeyword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (binding.editKeyword.getText() == null || binding.editKeyword.getText().toString().trim().length() == 0) {
                    Snackbar.make(binding.toolbar, "请输入搜索关键词", Snackbar.LENGTH_SHORT).show();
                    return false;
                }
                viewModel.search(binding.editKeyword.getText().toString().trim());
                return true;
            }
            return false;
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.item_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(true);
        //设置最大宽度
        searchView.setMaxWidth(100);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("请输入关键字：");

        //搜索框展开时后面叉叉按钮的点击事件
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Toast.makeText(getApplicationContext(), "Close", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Open", Toast.LENGTH_SHORT).show();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                SearchHistory searchHistory = SearchHistory.Companion.newHistory(s);
                AppDatabase.Companion.getInstance(SearchActivity.this).searchHistoryDao().insert(searchHistory);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchView.setSuggestionsAdapter(new SimpleCursorAdapter(SearchActivity.this, android.R.layout.simple_selectable_list_item,
                        AppDatabase.Companion.getInstance(SearchActivity.this).searchHistoryDao().getHistoryCursor(s),
                        new String[]{"key"}, new int[]{android.R.id.text1}, FLAG_REGISTER_CONTENT_OBSERVER));
                return false;
            }
        });
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(R.id.search_src_text);
        return super.onCreateOptionsMenu(menu);
    }
}
