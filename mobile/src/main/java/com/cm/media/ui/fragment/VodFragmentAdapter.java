package com.cm.media.ui.fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.cm.media.entity.category.Category;

import java.util.List;

public class VodFragmentAdapter extends FragmentPagerAdapter {
    private List<Category> mList;

    public VodFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public void replaceData(List<Category> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            return HomeTopicFragment.newInstance();
        }
        return VodListFragment.newInstance(mList.get(i));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mList.get(position).getName();
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View child = container.getChildAt(position);
        if (child != null) {
            container.removeView(child);
        }
    }
}