package com.cm.media.ui.fragment;

import android.animation.Animator;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.cm.media.R;
import com.cm.media.databinding.DlgParseVodBinding;

import java.util.Objects;

public class CMDialog extends DialogFragment {
    private static final String TAG = "CMDialog";
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public interface Callback {
        void exitWhenSuccess();

        void onRetry();
    }

    private static final int STATE_LOADING = 0x01;
    private static final int STATE_SUCCESS = 0x02;
    private static final int STATE_ERROR = 0x03;

    private DlgParseVodBinding binding;
    private int state = STATE_LOADING;
    private Callback mCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DlgParseVodBinding.inflate(inflater, container, false);
        binding.root.setOnClickListener(v -> {
            if (state == STATE_ERROR && mCallback != null) {
                mCallback.onRetry();
            }
        });
        return binding.root;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void showLoading(FragmentManager manager) {
        if (!this.isAdded()) {
            this.show(manager, TAG);
        }
        if (binding == null) {
            HANDLER.post(() -> onStateChange(STATE_LOADING));
        } else {
            onStateChange(STATE_LOADING);
        }
    }


    public void onSuccess() {
        this.dismiss();
    }

    public void showError(FragmentManager manager) {
        if (!this.isAdded()) {
            this.show(manager, TAG);
        }
        if (binding == null) {
            HANDLER.post(() -> onStateChange(STATE_ERROR));
        } else {
            onStateChange(STATE_ERROR);
        }
    }


    private void clearAnim() {
        if (binding == null) {
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        switch (state) {
            case STATE_LOADING:

                break;
            case STATE_SUCCESS:
                break;
            case STATE_ERROR:
                break;
            default:
                break;
        }
    }

    private void onStateChange(int newState) {
        clearAnim();
        state = newState;
        switch (newState) {
            case STATE_LOADING:
                binding.animView.setVisibility(View.VISIBLE);
                break;
            case STATE_SUCCESS:
                this.dismiss();
                break;
            case STATE_ERROR:
                this.dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isCancelable() {
        return state != STATE_LOADING;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCallback = null;
    }
}
