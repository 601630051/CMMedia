package com.cm.media.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.airbnb.lottie.LottieAnimationView;
import com.cm.media.R;
import com.cm.media.entity.ViewStatus;
import com.google.android.material.card.MaterialCardView;

public class IndicatorView extends MaterialCardView {

    private int mState;
    private String infoText = "";
    private LottieAnimationView loadingView;
    private LottieAnimationView errView;
    private LottieAnimationView emptyView;
    private TextView msgView;


    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        mState = array.getInteger(R.styleable.IndicatorView_state, ViewStatus.STATE_LOADING);
        infoText = array.getString(R.styleable.IndicatorView_message);
        array.recycle();
        init();
    }

    public void setState(int state) {
        this.mState = state;
        updateState(state, infoText);
    }

    public void setMessage(String message) {
        infoText = message;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_indicator, this, true);
        loadingView = findViewById(R.id.loadingView);
        errView = findViewById(R.id.errView);
        emptyView = findViewById(R.id.emptyView);
        msgView = findViewById(R.id.msg_text);
        updateState(mState, infoText);
    }


    private void updateState(int state, String msg) {
        mState = state;
        msgView.setText(msg);
        switch (mState) {
            case ViewStatus.STATE_LOADING:
                setVisibility(loadingView, VISIBLE);
                setVisibility(errView, GONE);
                setVisibility(emptyView, GONE);
                break;
            case ViewStatus.STATE_SUCCESS:
                setVisibility(loadingView, GONE);
                setVisibility(errView, GONE);
                setVisibility(emptyView, GONE);
                this.setVisibility(GONE);
                break;
            case ViewStatus.STATE_ERROR:
                setVisibility(loadingView, GONE);
                setVisibility(errView, VISIBLE);
                setVisibility(emptyView, GONE);
                break;
            case ViewStatus.STATE_EMPTY:
                setVisibility(loadingView, GONE);
                setVisibility(errView, GONE);
                setVisibility(emptyView, VISIBLE);
                break;
            default:
                break;

        }
    }

    private void setVisibility(LottieAnimationView view, int visibility) {
        if (view == null || view.getVisibility() == visibility) {
            return;
        }
        view.setVisibility(visibility);
        if (visibility == VISIBLE) {
            view.playAnimation();
        } else {
            view.pauseAnimation();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != VISIBLE) {
            loadingView.cancelAnimation();
            errView.cancelAnimation();
            emptyView.cancelAnimation();
        } else {
            updateState(mState, infoText);
        }
    }
}
