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

public class AnimationHandler {
    private static final long FRAME_DELAY_MS = 10;
    public static final ThreadLocal<AnimationHandler> sAnimatorHandler = new ThreadLocal<>();
    private AnimationFrameCallbackProvider mProvider;
    private final ArrayMap<AnimationFrameCallback, Long> mDelayedCallbackStartTime = new ArrayMap<>();
    private final ArrayList<AnimationFrameCallback> mAnimationCallbacks = new ArrayList<>();
    private final AnimationCallbackDispatcher mCallbackDispatcher = new AnimationCallbackDispatcher();
    private long mCurrentFrameTime = 0;
    private boolean mListDirty = false;

    public interface AnimationFrameCallback {
        boolean doAnimationFrame(long j);
    }

    public class AnimationCallbackDispatcher {
        AnimationCallbackDispatcher() {
        }

        void dispatchAnimationFrame(long j) {
            AnimationHandler.this.doAnimationFrame(j);
            if (AnimationHandler.this.mAnimationCallbacks.size() > 0) {
                AnimationHandler.this.getProvider().postFrameCallback();
            }
        }
    }

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

    public void setProvider(AnimationFrameCallbackProvider animationFrameCallbackProvider) {
        this.mProvider = animationFrameCallbackProvider;
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

    public void addAnimationFrameCallback(AnimationFrameCallback animationFrameCallback, long j) {
        if (this.mAnimationCallbacks.size() == 0) {
            getProvider().postFrameCallback();
        }
        if (!this.mAnimationCallbacks.contains(animationFrameCallback)) {
            this.mAnimationCallbacks.add(animationFrameCallback);
        }
        if (j > 0) {
            this.mDelayedCallbackStartTime.put(animationFrameCallback, Long.valueOf(SystemClock.uptimeMillis() + j));
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
        if (l.longValue() < j) {
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

    @SuppressLint("NewApi")
    public static class FrameCallbackProvider33 extends AnimationFrameCallbackProvider {
        private final Choreographer mChoreographer;
        private final Choreographer.FrameCallback mChoreographerCallback;
        private long mFrameDeltaNanos;
        private final Looper mLooper;
        private final Choreographer.VsyncCallback mVsyncCallback;

        class AnonymousClass1 implements Choreographer.VsyncCallback {
            AnonymousClass1() {
            }

            public void onVsync(Choreographer.FrameData frameData) {
                Choreographer.FrameTimeline[] frameTimelines = frameData.getFrameTimelines();
                int length = frameTimelines.length;
                if (length > 1) {
                    int i = length - 1;
                    FrameCallbackProvider33.this.mFrameDeltaNanos = Math.round(((frameTimelines[i].getExpectedPresentationTimeNanos() - frameTimelines[0].getExpectedPresentationTimeNanos()) * 1.0d) / i);
                }
            }
        }

        FrameCallbackProvider33(AnimationCallbackDispatcher animationCallbackDispatcher) {
            super(animationCallbackDispatcher);
            this.mChoreographer = Choreographer.getInstance();
            this.mLooper = Looper.myLooper();
            this.mFrameDeltaNanos = 0L;
            this.mVsyncCallback = new AnonymousClass1();
            this.mChoreographerCallback = new AnonymousClass2();
        }

        class AnonymousClass2 implements Choreographer.FrameCallback {
            AnonymousClass2() {
            }

            @Override
            public void doFrame(long j) {
                FrameCallbackProvider33.this.mDispatcher.dispatchAnimationFrame(j);
            }
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
    }

    public static class FrameCallbackProvider16 extends AnimationFrameCallbackProvider {
        private final Choreographer mChoreographer;
        private final Choreographer.FrameCallback mChoreographerCallback;
        private final Looper mLooper;

        class AnonymousClass1 implements Choreographer.FrameCallback {
            AnonymousClass1() {
            }

            @Override
            public void doFrame(long j) {
                FrameCallbackProvider16.this.mDispatcher.dispatchAnimationFrame(j);
            }
        }

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
    }

    @Deprecated
    private static class FrameCallbackProvider14 extends AnimationFrameCallbackProvider {
        private final Handler mHandler;
        private long mLastFrameTime;
        private final Runnable mRunnable;

        class AnonymousClass1 implements Runnable {
            AnonymousClass1() {
            }

            @Override
            public void run() {
                FrameCallbackProvider14.this.mLastFrameTime = SystemClock.uptimeMillis();
                FrameCallbackProvider14.this.mDispatcher.dispatchAnimationFrame(FrameCallbackProvider14.this.mLastFrameTime);
            }
        }

        FrameCallbackProvider14(AnimationCallbackDispatcher animationCallbackDispatcher) {
            super(animationCallbackDispatcher);
            this.mLastFrameTime = -1L;
            this.mRunnable = new AnonymousClass1();
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
    }

    public static abstract class AnimationFrameCallbackProvider {
        final AnimationCallbackDispatcher mDispatcher;

        long getFrameDeltaNanos() {
            return 0L;
        }

        abstract Looper getLooper();

        abstract boolean isCurrentThread();

        abstract void postFrameCallback();

        void postVsyncCallback() {
        }

        AnimationFrameCallbackProvider(AnimationCallbackDispatcher animationCallbackDispatcher) {
            this.mDispatcher = animationCallbackDispatcher;
        }
    }
}
