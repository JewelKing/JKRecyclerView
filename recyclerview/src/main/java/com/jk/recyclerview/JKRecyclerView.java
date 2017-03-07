package com.jk.recyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * custom RecyclerView
 *
 * @author JewelKing
 * @date 2017-03-07
 */

public class JKRecyclerView extends RecyclerView {

    private String TAG = getClass().getSimpleName();

    private Context context;

    /**
     * Is loading data
     */
    private boolean isLoading = false;

    /**
     * load type use to mark loading state
     */
    private LoadStateType mState = LoadStateType.STATE_NONE;

    /**
     * scroll type use to mark rolling direction
     */
    private ScrollType scrollType = ScrollType.NONE;

    private int lastVisibleItemPosition;

    private int mTouchSlop;

    public interface OnLoadMoreListener {
        public void onLoadMore();
    }

    private OnLoadMoreListener onLoadMoreListener;

    public OnLoadMoreListener getOnLoadMoreListener() {
        return onLoadMoreListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public JKRecyclerView(Context context) {
        this(context, null);
    }

    public JKRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JKRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init() {
        mTouchSlop = ViewConfiguration.get(getContext().getApplicationContext()).getScaledTouchSlop();
        RecyclerViewUtils.setVerticalLinearLayout(this);
    }

    private AdapterDataObserver mAdapterDataObserver = new AdapterDataObserver() {

        @Override
        public void onChanged() {
            super.onChanged();
            reset();
        }

        private void reset() {
            isLoading = false;
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            reset();

        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            reset();

        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            reset();

        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            reset();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            reset();
        }
    };

    @Override
    public void setAdapter(Adapter adapter) {
        JKAdapter jkAdapter = new JKAdapter(adapter);
        jkAdapter.registerAdapterDataObserver(mAdapterDataObserver);
        super.setAdapter(jkAdapter);
    }

    private float startY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = e.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                // The current position for finger
                float f = Math.abs(e.getRawY() - startY);
                if (f < mTouchSlop) {
                    scrollType = ScrollType.NONE;
                } else if ((e.getRawY() - startY) < 0) {
                    scrollType = ScrollType.UP;
                } else if ((e.getRawY() - startY) == 0) {
                    scrollType = ScrollType.NONE;
                } else if ((e.getRawY() - startY) > 0) {
                    scrollType = ScrollType.DOWN;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                scrollType = ScrollType.NONE;
                break;
            default:
                startY = -1; // reset
                break;
        }
        return super.onTouchEvent(e);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
            staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
            lastVisibleItemPosition = findMax(lastPositions);
        } else {
            throw new RuntimeException(
                    "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (scrollType == ScrollType.UP && null != onLoadMoreListener) {
            if (state == RecyclerView.SCROLL_STATE_IDLE) {
                RecyclerView.LayoutManager layoutManager = getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                if (visibleItemCount > 0
                        && lastVisibleItemPosition >= totalItemCount - 1) {
                    setLoading();
                    onLoadMoreListener.onLoadMore();
                }
            }
        }
    }

    public void setLoading() {
        if (null != getAdapter()) {
            isLoading = true;
            scrollType = ScrollType.NONE;
            mState = LoadStateType.STATE_LOADING;
            getAdapter().notifyDataSetChanged();
//            getAdapter().notifyItemChanged(getAdapter().getItemCount() - 1);
        }
    }

    public void setLoadComplete() {
        if (null != getAdapter()) {
            isLoading = false;
            scrollType = ScrollType.NONE;
            mState = LoadStateType.STATE_COMP;
            getAdapter().notifyDataSetChanged();
//            getAdapter().notifyItemChanged(getAdapter().getItemCount()-1);
        }
    }

    public void setLoadMore() {
        if (null != getAdapter()) {
            isLoading = false;
            scrollType = ScrollType.NONE;
            mState = LoadStateType.STATE_MORE;
            getAdapter().notifyItemChanged(getAdapter().getItemCount()-1);
        }
    }

    public void setLoadNone() {
        if (null != getAdapter()) {
            isLoading = false;
            scrollType = ScrollType.NONE;
            mState = LoadStateType.STATE_NONE;
            getAdapter().notifyItemChanged(getAdapter().getItemCount()-1);
        }
    }

    public void setLoadError() {
        if (null != getAdapter()) {
            isLoading = false;
            scrollType = ScrollType.NONE;
            mState = LoadStateType.STATE_ERROR;
            getAdapter().notifyDataSetChanged();
        }
    }

    public void setLoadEmpty() {
        if (null != getAdapter()) {
            isLoading = false;
            scrollType = ScrollType.NONE;
            mState = LoadStateType.STATE_EMPTY;
            getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * JKAdapter function start
     */
    public class JKAdapter extends RecyclerView.Adapter {

        public static final int VIEW_NONE = -1001;
        public static final int VIEW_MORE = -1002;
        public static final int VIEW_EMPTY = -1003;
        public static final int VIEW_ERROR = -1004;

        private LayoutInflater inflater;

        public RecyclerView.Adapter adapter;

        public JKAdapter(Adapter adapter) {
            this.adapter = adapter;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            super.registerAdapterDataObserver(observer);
            adapter.registerAdapterDataObserver(observer);
        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            super.unregisterAdapterDataObserver(observer);
            adapter.unregisterAdapterDataObserver(observer);
        }

        public boolean isFootView(int position) {
            return position == getItemCount() - 1 && mState != LoadStateType.STATE_NONE;
        }

        @Override
        public int getItemViewType(int position) {
            if (!isFootView(position)) {
                return adapter.getItemViewType(position);
            } else {
                if (mState == LoadStateType.STATE_EMPTY && getItemCount() == 1) {
                    return VIEW_EMPTY;
                } else {
                    return VIEW_MORE;
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_MORE) {
                return new FootVH(inflater.inflate(R.layout.recycler_foot_view, parent, false));
            } else if (viewType == VIEW_EMPTY) {
                return new EmptyVH(inflater.inflate(R.layout.recycler_foot_empty_view, parent, false));
            } else {
                return adapter.onCreateViewHolder(parent, viewType);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (isFootView(position)) {
                if (holder instanceof FootVH) {
                    ((FootVH) holder).onBindData(holder.itemView, mState);
                } else if (holder instanceof EmptyVH) {

                }
            } else {
                adapter.onBindViewHolder(holder, position);
            }
        }

        @Override
        public int getItemCount() {
            if (mState == LoadStateType.STATE_NONE) {
                return adapter.getItemCount();
            } else {
                return adapter.getItemCount() + 1;
            }
        }

        public class FootVH extends RecyclerView.ViewHolder {

            private ProgressBar progressBar;
            private TextView textView;
            private LinearLayout linearLayout;

            public FootVH(View itemView) {
                super(itemView);
                progressBar = (ProgressBar) itemView.findViewById(R.id.jk_loading_progress);
                textView = (TextView) itemView.findViewById(R.id.jk_loading_message);
                linearLayout = (LinearLayout) itemView.findViewById(R.id.ll_loading);
            }

            public void onBindData(View view, LoadStateType state) {
                if (state == LoadStateType.STATE_LOADING) {
                    showProgressBar();
                } else if (state == LoadStateType.STATE_COMP) {
                    showMessage(getResources().getString(R.string.jk_rv_load_more));
                } else if (state == LoadStateType.STATE_MORE) {
                    showMessage(getResources().getString(R.string.jk_rv_load_more));
                } else if (state == LoadStateType.STATE_ERROR) {
                    showMessage(getResources().getString(R.string.jk_rv_error));
                } else if (state == LoadStateType.STATE_NONE) {
                    showMessage("");
                }
            }

            public void showProgressBar() {
                textView.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
            }

            public void showMessage(String text) {
                linearLayout.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                textView.setText(text);
            }

        }

        public class EmptyVH extends RecyclerView.ViewHolder {

            private TextView textView;
            private ImageView imageView;

            public EmptyVH(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.jk_empty_title);
                imageView = (ImageView) itemView.findViewById(R.id.jk_empty_icon);
            }
        }

    }
}
