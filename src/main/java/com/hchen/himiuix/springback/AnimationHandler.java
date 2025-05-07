/*
 * This file is part of HiMiuiX.

 * HiMiuiX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.himiuix.springback;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.view.Choreographer;

import java.util.ArrayList;

class AnimationHandler {
    public static final ThreadLocal<AnimationHandler> sAnimatorHandler = new ThreadLocal<>();
    private static final long FRAME_DELAY_MS = 10;
    private final ArrayMap<AnimationFrameCallback, Long> mDelayedCallbackStartTime = new ArrayMap<>();
    private final ArrayList<AnimationFrameCallback> mAnimationCallbacks = new ArrayList<>();
    private final AnimationCallbackDispatcher mCallbackDispatcher = new AnimationCallbackDispatcher();
    private AnimationFrameCallbackProvider mProvider;
    private final long mCurrentFrameTime = 0;
    private boolean mListDirty = false;

    public static AnimationHandler getInstance() {
        ThreadLocal<AnimationHandler> threadLocal = sAnimatorHandler;
        if (threadLocal.get() == null) {
            threadLocal.set(new AnimationHandler());
        }
        return threadLocal.get();
    }

    public static long getFrameTime() {
        ThreadLocal<AnimationHandler> threadLocal = sAnimatorHandler;
        if (threadLocal.get() == null) {
            return 0L;
        }
        return threadLocal.get().mCurrentFrameTime;
    }

    public void postVsyncCallback() {
        getProvider().postVsyncCallback();
    }

    public void recreateProvider() {
        if (Build.VERSION.SDK_INT >= 33) {
            this.mProvider = new FrameCallbackProvider33(this.mCallbackDispatcher);
        } else {
            this.mProvider = new FrameCallbackProvider16(this.mCallbackDispatcher);
        }
    }

    public AnimationFrameCallbackProvider getProvider() {
        if (this.mProvider == null) {
            if (Build.VERSION.SDK_INT >= 33) {
                this.mProvider = new FrameCallbackProvider33(this.mCallbackDispatcher);
            } else {
                this.mProvider = new FrameCallbackProvider16(this.mCallbackDispatcher);
            }
        }
        return this.mProvider;
    }

    public void setProvider(AnimationFrameCallbackProvider animationFrameCallbackProvider) {
        this.mProvider = animationFrameCallbackProvider;
    }

    public void addAnimationFrameCallback(AnimationFrameCallback animationFrameCallback, long j) {
        if (this.mAnimationCallbacks.isEmpty()) {
            getProvider().postFrameCallback();
        }
        if (!this.mAnimationCallbacks.contains(animationFrameCallback)) {
            this.mAnimationCallbacks.add(animationFrameCallback);
        }
        if (j > 0) {
            this.mDelayedCallbackStartTime.put(animationFrameCallback, SystemClock.uptimeMillis() + j);
        }
    }

    public void removeCallback(AnimationFrameCallback animationFrameCallback) {
        this.mDelayedCallbackStartTime.remove(animationFrameCallback);
        int indexOf = this.mAnimationCallbacks.indexOf(animationFrameCallback);
        if (indexOf >= 0) {
            this.mAnimationCallbacks.set(indexOf, null);
            this.mListDirty = true;
        }
    }

    public Looper getLooper() {
        return getProvider().getLooper();
    }

    public boolean isCurrentThread() {
        return getProvider().isCurrentThread();
    }

    public void doAnimationFrame(long j) {
        long uptimeMillis = SystemClock.uptimeMillis();
        for (int i = 0; i < this.mAnimationCallbacks.size(); i++) {
            AnimationFrameCallback animationFrameCallback = this.mAnimationCallbacks.get(i);
            if (animationFrameCallback != null && isCallbackDue(animationFrameCallback, uptimeMillis)) {
                animationFrameCallback.doAnimationFrame(j);
            }
        }
        cleanUpList();
    }

    private boolean isCallbackDue(AnimationFrameCallback animationFrameCallback, long j) {
        Long l = this.mDelayedCallbackStartTime.get(animationFrameCallback);
        if (l == null) {
            return true;
        }
        if (l < j) {
            this.mDelayedCallbackStartTime.remove(animationFrameCallback);
            return true;
        }
        return false;
    }

    private void cleanUpList() {
        if (this.mListDirty) {
            for (int size = this.mAnimationCallbacks.size() - 1; size >= 0; size--) {
                if (this.mAnimationCallbacks.get(size) == null) {
                    this.mAnimationCallbacks.remove(size);
                }
            }
            this.mListDirty = false;
        }
    }

    public long getFrameDeltaNanos() {
        return getProvider().getFrameDeltaNanos();
    }

    public interface AnimationFrameCallback {
        boolean doAnimationFrame(long j);
    }

    @SuppressLint("NewApi")
    public static class FrameCallbackProvider33 extends AnimationFrameCallbackProvider {
        private final Choreographer mChoreographer;
        private final Choreographer.FrameCallback mChoreographerCallback;
        private final Looper mLooper;
        private final Choreographer.VsyncCallback mVsyncCallback;
        private long mFrameDeltaNanos;

        FrameCallbackProvider33(AnimationCallbackDispatcher animationCallbackDispatcher) {
            super(animationCallbackDispatcher);
            this.mChoreographer = Choreographer.getInstance();
            this.mLooper = Looper.myLooper();
            this.mFrameDeltaNanos = 0L;
            this.mVsyncCallback = new VsyncFrameCallback();
            this.mChoreographerCallback = new ChoreographerCallback();
        }

        @Override
        void postFrameCallback() {
            this.mChoreographer.postVsyncCallback(this.mVsyncCallback);
            this.mChoreographer.postFrameCallback(this.mChoreographerCallback);
        }

        @Override
        public void postVsyncCallback() {
            this.mChoreographer.postVsyncCallback(this.mVsyncCallback);
        }

        @Override
        boolean isCurrentThread() {
            return Thread.currentThread() == this.mLooper.getThread();
        }

        @Override
        Looper getLooper() {
            return this.mLooper;
        }

        @Override
        long getFrameDeltaNanos() {
            return this.mFrameDeltaNanos;
        }

        class VsyncFrameCallback implements Choreographer.VsyncCallback {
            public void onVsync(Choreographer.FrameData frameData) {
                Choreographer.FrameTimeline[] frameTimelines = frameData.getFrameTimelines();
                int length = frameTimelines.length;
                if (length > 1) {
                    int i = length - 1;
                    FrameCallbackProvider33.this.mFrameDeltaNanos = Math.round(((frameTimelines[i].getExpectedPresentationTimeNanos() - frameTimelines[0].getExpectedPresentationTimeNanos()) * 1.0d) / i);
                }
            }
        }

        class ChoreographerCallback implements Choreographer.FrameCallback {
            @Override
            public void doFrame(long j) {
                FrameCallbackProvider33.this.mDispatcher.dispatchAnimationFrame(j);
            }
        }
    }

    public static class FrameCallbackProvider16 extends AnimationFrameCallbackProvider {
        private final Choreographer mChoreographer;
        private final Choreographer.FrameCallback mChoreographerCallback;
        private final Looper mLooper;

        FrameCallbackProvider16(AnimationCallbackDispatcher animationCallbackDispatcher) {
            super(animationCallbackDispatcher);
            this.mChoreographer = Choreographer.getInstance();
            this.mLooper = Looper.myLooper();
            this.mChoreographerCallback = new AnonymousClass1();
        }

        @Override
        void postFrameCallback() {
            this.mChoreographer.postFrameCallback(this.mChoreographerCallback);
        }

        @Override
        boolean isCurrentThread() {
            return Thread.currentThread() == this.mLooper.getThread();
        }

        @Override
        Looper getLooper() {
            return this.mLooper;
        }

        class AnonymousClass1 implements Choreographer.FrameCallback {
            AnonymousClass1() {
            }

            @Override
            public void doFrame(long j) {
                FrameCallbackProvider16.this.mDispatcher.dispatchAnimationFrame(j);
            }
        }
    }

    @Deprecated
    private static class FrameCallbackProvider14 extends AnimationFrameCallbackProvider {
        private final Handler mHandler;
        private final Runnable mRunnable;
        private long mLastFrameTime;

        FrameCallbackProvider14(AnimationCallbackDispatcher animationCallbackDispatcher) {
            super(animationCallbackDispatcher);
            this.mLastFrameTime = -1L;
            this.mRunnable = new FrameRunnable();
            this.mHandler = new Handler(Looper.myLooper());
        }

        @Override
        void postFrameCallback() {
            this.mHandler.postDelayed(this.mRunnable, Math.max(10 - (SystemClock.uptimeMillis() - this.mLastFrameTime), 0L));
        }

        @Override
        boolean isCurrentThread() {
            return Thread.currentThread() == this.mHandler.getLooper().getThread();
        }

        @Override
        Looper getLooper() {
            return this.mHandler.getLooper();
        }

        class FrameRunnable implements Runnable {
            @Override
            public void run() {
                FrameCallbackProvider14.this.mLastFrameTime = SystemClock.uptimeMillis();
                FrameCallbackProvider14.this.mDispatcher.dispatchAnimationFrame(FrameCallbackProvider14.this.mLastFrameTime);
            }
        }
    }

    public static abstract class AnimationFrameCallbackProvider {
        final AnimationCallbackDispatcher mDispatcher;

        AnimationFrameCallbackProvider(AnimationCallbackDispatcher animationCallbackDispatcher) {
            this.mDispatcher = animationCallbackDispatcher;
        }

        long getFrameDeltaNanos() {
            return 0L;
        }

        abstract Looper getLooper();

        abstract boolean isCurrentThread();

        abstract void postFrameCallback();

        void postVsyncCallback() {
        }
    }

    public class AnimationCallbackDispatcher {
        AnimationCallbackDispatcher() {
        }

        void dispatchAnimationFrame(long j) {
            AnimationHandler.this.doAnimationFrame(j);
            if (!AnimationHandler.this.mAnimationCallbacks.isEmpty()) {
                AnimationHandler.this.getProvider().postFrameCallback();
            }
        }
    }
}
