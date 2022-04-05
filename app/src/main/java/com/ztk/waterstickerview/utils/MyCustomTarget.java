package com.ztk.waterstickerview.utils;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public  class MyCustomTarget<T>  extends CustomTarget<T> {

    public MyCustomTarget(){
        super();
    }

    public MyCustomTarget(int width, int height){
        super(width,height);
    }
    @Override
    public void onResourceReady(@NonNull T resource, @Nullable Transition<? super T> transition) {

    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {

    }
}
