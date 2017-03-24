package com.ucl.news.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.ucl.news.main.LevelOneTab;
import com.ucl.news.main.LevelThreeTab;
import com.ucl.news.main.LevelTwoTab;

public class TabPagerAdapter extends FragmentPagerAdapter {
    int mNumOfTabs;

    public TabPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                LevelOneTab tab1 = new LevelOneTab();
                return tab1;
            case 1:
                LevelTwoTab tab2 = new LevelTwoTab();
                return tab2;
            case 2:
                LevelThreeTab tab3 = new LevelThreeTab();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
