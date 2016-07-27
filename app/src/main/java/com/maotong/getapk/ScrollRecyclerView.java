package com.maotong.getapk;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.Toast;

/**
 * Created by MaoTong on 2016/7/26.
 * QQ:974291433
 * 完善RecyclerView的滚动方法
 */
public class ScrollRecyclerView extends RecyclerView {

    private static final String TAG = "ScrollRecyclerView";
    private RecyclerView mRecyclerView;
    private boolean isMove = false;
    private boolean isSmoothScroll = false;
    private int mIndex = 0;

    public ScrollRecyclerView(Context context) {
        this(context, null);
    }

    public ScrollRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mRecyclerView = this;
        mRecyclerView.addOnScrollListener(new RecyclerViewListener());
    }

    public void move(int position, boolean smoothScroll) {
        isSmoothScroll = smoothScroll;
        if (position < 0 || position >= mRecyclerView.getAdapter().getItemCount()) {
            Toast.makeText(this.getContext(), "超出范围", Toast.LENGTH_SHORT).show();
            return;
        }
        mIndex = position;
        mRecyclerView.stopScroll();
        if (smoothScroll) {
            smoothMoveToPosition(position);
        } else {
            moveToPosition(position);
        }
    }

    public void moveToPosition(int position) {
        int firstItem = ((LinearLayoutManager) this.getLayoutManager()).findFirstVisibleItemPosition();
        int lastItem = ((LinearLayoutManager) this.getLayoutManager()).findLastVisibleItemPosition();
        if (position <= firstItem) {
            mRecyclerView.scrollToPosition(position);
        } else if (position <= lastItem) {
            int top = mRecyclerView.getChildAt(position - firstItem).getTop();
            mRecyclerView.scrollBy(0, top);
        } else {
            mRecyclerView.scrollToPosition(position);
            isMove = true;
        }
    }

    public void smoothMoveToPosition(int position) {
        int firstItem = ((LinearLayoutManager) this.getLayoutManager()).findFirstVisibleItemPosition();
        int lastItem = ((LinearLayoutManager) this.getLayoutManager()).findLastVisibleItemPosition();
        if (position <= firstItem) {
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            int top = mRecyclerView.getChildAt(position - firstItem).getTop();
            mRecyclerView.smoothScrollBy(0, top);
        } else {
            mRecyclerView.smoothScrollToPosition(position);
            isMove = true;
        }
    }

    class RecyclerViewListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (isMove && newState == RecyclerView.SCROLL_STATE_IDLE && isSmoothScroll) {
                isMove = false;
                int n = mIndex - ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (0 <= n && n < mRecyclerView.getChildCount()) {
                    int top = mRecyclerView.getChildAt(n).getTop();
                    mRecyclerView.smoothScrollBy(0, top);
                }

            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (isMove && !isSmoothScroll) {
                isMove = false;
                int n = mIndex - ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (0 <= n && n < mRecyclerView.getChildCount()) {
                    int top = mRecyclerView.getChildAt(n).getTop();
                    mRecyclerView.scrollBy(0, top);
                }
            }
        }
    }
}
