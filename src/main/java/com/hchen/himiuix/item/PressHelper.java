package com.hchen.himiuix.item;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.hchen.himiuix.R;

class PressHelper {
    private final String TAG = "HiMiuiX";
    private View view;
    private GradientDrawable background;
    private int originalColor;
    private int pressColor;
    private ValueAnimator animator;
    private int touchSlop;
    private boolean autoHapticFeedback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPressCandidate = false;
    private boolean isChanged = false;
    private float initialX;
    private float initialY;
    private final Runnable touchDownRunnable = new Runnable() {
        @Override
        public void run() {
            background.setColor(pressColor);
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
            isChanged = true;
        }
    };
    private final Runnable touchUpRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPressCandidate || isChanged) {
                if (autoHapticFeedback && isPressCandidate)
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                animator = ValueAnimator.ofObject(new ArgbEvaluator(), pressColor, originalColor);
                animator.setDuration(300);
                animator.addUpdateListener(animation -> {
                    int color = (int) animation.getAnimatedValue();
                    background.setColor(color);
                });
                animator.start();
            }
            isPressCandidate = false;
            isChanged = false;
        }
    };

    public static void init(View view, boolean autoHapticFeedback) {
        new PressHelper().applyPressEffect(view, autoHapticFeedback);
    }

    private void applyPressEffect(View view, boolean autoHapticFeedback) {
        this.view = view;
        this.autoHapticFeedback = autoHapticFeedback;
        View itemView = view.findViewById(R.id.miuix_item);
        background = (GradientDrawable) itemView.getBackground();
        originalColor = view.getContext().getColor(R.color.touch_up);
        pressColor = view.getContext().getColor(R.color.touch_down);
        touchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();

        view.setClickable(true);
        view.setHapticFeedbackEnabled(true);
        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        isPressCandidate = true;
                        isChanged = false;
                        handler.removeCallbacks(touchDownRunnable);
                        handler.removeCallbacks(touchUpRunnable);
                        handler.postDelayed(touchDownRunnable, 200);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (isPressCandidate) {
                            float dx = event.getRawX() - initialX;
                            float dy = event.getRawY() - initialY;

                            if (Math.hypot(dx, dy) > touchSlop) {
                                isPressCandidate = false;
                                handler.removeCallbacks(touchDownRunnable);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(touchDownRunnable);
                        handler.post(touchUpRunnable);
                        break;
                }
                return false;
            }
        });
    }
}
