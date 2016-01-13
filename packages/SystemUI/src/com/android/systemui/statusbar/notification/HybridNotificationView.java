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
 * limitations under the License
 */

package com.android.systemui.statusbar.notification;

import android.annotation.Nullable;
import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.systemui.R;
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.phone.NotificationPanelView;

import java.util.ArrayList;

/**
 * A hybrid view which may contain information about one ore more notifications.
 */
public class HybridNotificationView extends AlphaOptimizedLinearLayout
        implements TransformableView {

    private ViewTransformationHelper mTransformationHelper;

    protected TextView mTitleView;
    protected TextView mTextView;
    private ViewInvertHelper mInvertHelper;

    public HybridNotificationView(Context context) {
        this(context, null);
    }

    public HybridNotificationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HybridNotificationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HybridNotificationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitleView = (TextView) findViewById(R.id.notification_title);
        mTextView = (TextView) findViewById(R.id.notification_text);
        mInvertHelper = new ViewInvertHelper(this, NotificationPanelView.DOZE_ANIMATION_DURATION);
        mTransformationHelper = new ViewTransformationHelper();
        mTransformationHelper.setCustomTransformation(
                new ViewTransformationHelper.CustomTransformation() {
                    @Override
                    public boolean transformTo(TransformState ownState, TransformableView notification,
                            Runnable endRunnable) {
                        // We want to transform to the same y location as the title
                        TransformState otherState = notification.getCurrentState(
                                TRANSFORMING_VIEW_TITLE);
                        CrossFadeHelper.fadeOut(mTextView, endRunnable);
                        if (otherState != null) {
                            ownState.animateViewVerticalTo(otherState, endRunnable);
                            otherState.recycle();
                        }
                        return true;
                    }

                    @Override
                    public boolean transformFrom(TransformState ownState,
                            TransformableView notification) {
                        // We want to transform from the same y location as the title
                        TransformState otherState = notification.getCurrentState(
                                TRANSFORMING_VIEW_TITLE);
                        CrossFadeHelper.fadeIn(mTextView);
                        if (otherState != null) {
                            ownState.animateViewVerticalFrom(otherState);
                            otherState.recycle();
                        }
                        return true;
                    }
                }, TRANSFORMING_VIEW_TEXT);
        mTransformationHelper.addTransformedView(TRANSFORMING_VIEW_TITLE, mTitleView);
        mTransformationHelper.addTransformedView(TRANSFORMING_VIEW_TEXT, mTextView);
    }

    public void bind(CharSequence title) {
        bind(title, null);
    }

    public void bind(CharSequence title, CharSequence text) {
        mTitleView.setText(title);
        if (TextUtils.isEmpty(title)) {
            mTitleView.setVisibility(GONE);
        }
        mTextView.setText(text);
        if (TextUtils.isEmpty(text)) {
            mTextView.setVisibility(GONE);
        }
        requestLayout();
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        mInvertHelper.setInverted(dark, fade, delay);
    }

    @Override
    public TransformState getCurrentState(int fadingView) {
        return mTransformationHelper.getCurrentState(fadingView);
    }

    @Override
    public void transformTo(TransformableView notification, Runnable endRunnable) {
        mTransformationHelper.transformTo(notification, endRunnable);
    }

    @Override
    public void transformFrom(TransformableView notification) {
        mTransformationHelper.transformFrom(notification);
    }

    @Override
    public void setVisible(boolean visible) {
        setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mTransformationHelper.setVisible(visible);
    }
}
