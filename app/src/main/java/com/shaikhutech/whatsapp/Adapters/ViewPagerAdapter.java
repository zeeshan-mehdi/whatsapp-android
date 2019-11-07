package com.shaikhutech.whatsapp.Adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shaikhutech.whatsapp.Fragments.Calls;
import com.shaikhutech.whatsapp.Fragments.Chats;
import com.shaikhutech.whatsapp.Fragments.Contacts;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments;


    public ViewPagerAdapter(FragmentManager fm){
        super(fm);

        fragments= new Fragment[]{
            new Chats(),
            new Calls(),
            new Contacts()
        };
    }

    @Override
    public Fragment getItem(int i) {
        return fragments[i];
    }

    @Override
    public int getCount() {
        return fragments.length;//3
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        String title = getItem(position).getClass().getName();

        return title.subSequence(title.lastIndexOf(".")+1,title.length());
    }
}
