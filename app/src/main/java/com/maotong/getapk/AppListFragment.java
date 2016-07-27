/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maotong.getapk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppListFragment extends Fragment {
    private static final String DATA = "AppInfoList";
    private static final String TAG = "AppListFragment";
    private List<AppInfo> mAppInfoList;
    private SimpleStringRecyclerViewAdapter mAdapter;
    private ScrollRecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    public static AppListFragment newInstance(List<AppInfo> appInfoList) {
        AppListFragment appListFragment = new AppListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DATA, (Serializable) appInfoList);
        bundle.putParcelableArrayList(DATA, (ArrayList<? extends Parcelable>) appInfoList);
        appListFragment.setArguments(bundle);
        return appListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (null != getArguments()) {
            mAppInfoList = getArguments().getParcelableArrayList(DATA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = (ScrollRecyclerView) inflater.inflate(
                R.layout.fragment_app_list, container, false);
        setupRecyclerView();
        return mRecyclerView;
    }

    @Subscribe
    public void onEvent(PositionEvent event) {
        mRecyclerView.move(event.getPosition(), true);
    }

    private void setupRecyclerView() {
        if (null == mAppInfoList) {
            mAppInfoList = new ArrayList<AppInfo>();
        }
        mLinearLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mAdapter = new SimpleStringRecyclerViewAdapter(mRecyclerView.getContext(), mAppInfoList);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<AppInfo> mApps;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View mView;
            public final ImageView mImageView;
            public final TextView mNameTextView;
            public final TextView mSizeTextView;
            public final TextView mTimeTextView;


            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.iconImageView);
                mNameTextView = (TextView) view.findViewById(R.id.nameTextView);
                mSizeTextView = (TextView) view.findViewById(R.id.sizeTextView);
                mTimeTextView = (TextView) view.findViewById(R.id.timeTextView);
            }
        }

        public SimpleStringRecyclerViewAdapter(Context context, List<AppInfo> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mApps = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final AppInfo appInfo = mApps.get(position);
            holder.mTimeTextView.setText(mApps.get(position).getAppTime());
            holder.mSizeTextView.setText(appInfo.getAppSize());
            holder.mNameTextView.setText(appInfo.getAppName());
            Glide.with(holder.mImageView.getContext())
                    .load("")
                    .fitCenter()
                    .placeholder(appInfo.getAppIcon())
                    .into(holder.mImageView);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File apkFile = appInfo.getApkFile();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(apkFile));
                    startActivity(intent);
                }
            });


        }

        @Override
        public int getItemCount() {
            return mApps.size();
        }
    }

}
