/*
 * This file is part of HiMiuiX.

 * HiMiuiX is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * HiMiuiX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2024 HiMiuiX Contributions
 */
package com.hchen.himiuix.helper.springback;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.view.Choreographer;

import java.util.ArrayList;

public class AnimationHandler {
    public static final ThreadLocal<AnimationHandler> sAnimatorHandler = new ThreadLocal<>();
    private AnimationFrameCallbackProvider mProvider;
    private final ArrayMap<AnimationFrameCallback, Long> mDelayedCallbackStartTime = new ArrayMap<>();
    private final ArrayList<AnimationFrameCallback> mAnimationCallbacks = new ArrayList<>();
    private final AnimationCallbackDispatcher mCallbackDispatcher = new AnimationCallbackDispatcher();
    private long mCurrentFrameTime = 0;
    private boolean mListDirty = false;

    public interface AnimationFrameCallback {
        boolean doAnimationFrame(long delayMillis);
    }

    public class AnimationCallbackDispatcher {
        AnimationCallbackDispatcher() {
        }

        void dispatchAnimationFrame(long delayMillis) {
            doAnimationFrame(delayMillis);
            if (!mAnimationCallbacks.isEmpty()) {
                getProvider().postFrameCallback();
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

    public AnimationFrameCallbackProvider getProvider() {
        if (mProvider == null) {
            if (Build.VERSION.SDK_INT >= 33) {
                mProvider = new FrameCallbackProvider33(mCallbackDispatcher);
            } else {
                mProvider = new FrameCallbackProvider16(mCallbackDispatcher);
            }
        }
        return mProvider;
    }

    public void postVsyncCallback() {
        getProvider().postVsyncCallback();
    }

    public void addAnimationFrameCallback(AnimationFrameCallback animationFrameCallback, long delayMillis) {
        if (mAnimationCallbacks.isEmpty()) {
            getProvider().postFrameCallback();
        }
        if (!mAnimationCallbacks.contains(animationFrameCallback)) {
            mAnimationCallbacks.add(animationFrameCallback);
        }
        if (delayMillis > 0) {
            mDelayedCallbackStartTime.put(animationFrameCallback, SystemClock.uptimeMillis() + delayMillis);
        }
    }

    public void removeCallback(AnimationFrameCallback animationFrameCallback) {
        mDelayedCallbackStartTime.remove(animationFrameCallback);
        int indexOf = mAnimationCallbacks.indexOf(animationFrameCallback);
        if (indexOf >= 0) {
            mAnimationCallbacks.set(indexOf, null);
            mListDirty = true;
        }
    }

    public boolean isCurrentThread() {
        return getProvider().isCurrentThread();
    }

    public void doAnimationFrame(long delayMillis) {
        long uptimeMillis = SystemClock.uptimeMillis();
        for (int i = 0; i < mAnimationCallbacks.size(); i++) {
            AnimationFrameCallback animationFrameCallback = mAnimationCallbacks.get(i);
            if (animationFrameCallback != null && isCallbackDue(animationFrameCallback, uptimeMillis)) {
                animationFrameCallback.doAnimationFrame(delayMillis);
            }
        }
        cleanUpList();
    }

    private boolean isCallbackDue(AnimationFrameCallback animationFrameCallback, long delayMillis) {
        Long time = mDelayedCallbackStartTime.get(animationFrameCallback);
        if (time == null)
            return true;
        if (time >= delayMillis)
            return false;

        mDelayedCallbackStartTime.remove(animationFrameCallback);
        return true;
    }

    private void cleanUpList() {
        if (mListDirty) {
            for (int size = mAnimationCallbacks.size() - 1; size >= 0; size--) {
                if (mAnimationCallbacks.get(size) == null) {
                    mAnimationCallbacks.remove(size);
                }
            }
            mListDirty = false;
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

        FrameCallbackProvider33(AnimationCallbackDispatcher animationCallbackDispatcher) {
            super(animationCallbackDispatcher);
            mChoreographer = Choreographer.getInstance();
            mLooper = Looper.myLooper();
            mFrameDeltaNanos = 0L;
            mVsyncCallback = frameData -> {
                Choreographer.FrameTimeline[] frameTimelines = frameData.getFrameTimelines();
                int length = frameTimelines.length;
                if (length > 1) {
                    mFrameDeltaNanos = Math.round(((frameTimelines[length - 1].getExpectedPresentationTimeNanos() - frameTimelines[0].getExpectedPresentationTimeNanos()) * 1.0d) / (length - 1));
                }
            };
            mChoreographerCallback = mDispatcher::dispatchAnimationFrame;
        }

        @Override
        void postFrameCallback() {
            mChoreographer.postVsyncCallback(mVsyncCallback);
            mChoreographer.postFrameCallback(mChoreographerCallback);
        }

        @Override
        public void postVsyncCallback() {
            mChoreographer.postVsyncCallback(mVsyncCallback);
        }

        @Override
        boolean isCurrentThread() {
            return Thread.currentThread() == mLooper.getThread();
        }

        @Override
        long getFrameDeltaNanos() {
            return mFrameDeltaNanos;
        }
    }

    public static class FrameCallbackProvider16 extends AnimationFrameCallbackProvider {
        private final Choreographer mChoreographer;
        private final Choreographer.FrameCallback mChoreographerCallback;
        private final Looper mLooper;

        FrameCallbackProvider16(AnimationCallbackDispatcher animationCallbackDispatcher) {
            super(animationCallbackDispatcher);
            mChoreographer = Choreographer.getInstance();
            mLooper = Looper.myLooper();
            mChoreographerCallback = mDispatcher::dispatchAnimationFrame;
        }

        @Override
        void postFrameCallback() {
            mChoreographer.postFrameCallback(mChoreographerCallback);
        }

        @Override
        boolean isCurrentThread() {
            return Thread.currentThread() == mLooper.getThread();
        }
    }

    public static abstract class AnimationFrameCallbackProvider {
        final AnimationCallbackDispatcher mDispatcher;

        long getFrameDeltaNanos() {
            return 0L;
        }

        abstract boolean isCurrentThread();

        abstract void postFrameCallback();

        void postVsyncCallback() {
        }

        AnimationFrameCallbackProvider(AnimationCallbackDispatcher animationCallbackDispatcher) {
            mDispatcher = animationCallbackDispatcher;
        }
    }
}
