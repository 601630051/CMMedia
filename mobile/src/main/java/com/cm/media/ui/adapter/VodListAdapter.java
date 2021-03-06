package com.cm.media.ui.adapter;

import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.cm.media.R;
import com.cm.media.entity.vod.Vod;
import com.cm.media.ui.activity.VodPlayerActivity;

import java.util.List;

public class VodListAdapter extends BaseQuickAdapter<Vod, BaseViewHolder> {
    private String filters;

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getFilters() {
        return filters;
    }

    public VodListAdapter(@Nullable List<Vod> data) {
        super(R.layout.recycler_item_vod, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Vod item) {
        helper.setText(R.id.vodName, String.valueOf(item.getName()));
        RequestOptions options = RequestOptions.placeholderOf(R.mipmap.place_holder);
        Glide.with(mContext).load(item.getImg()).apply(options).into((ImageView) helper.getView(R.id.vodPost));
        helper.getView(R.id.vodPost).setOnClickListener(view -> VodPlayerActivity.startVodPlay(view.getContext(), item.getId()));
    }
}
