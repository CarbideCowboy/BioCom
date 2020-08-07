package com.hoker.biocom.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hoker.biocom.R;

import java.util.Objects;

public class TransmitPulseAnimation extends Fragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_transmit_pulse_animation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        setUpAnimation();
    }

    private void setUpAnimation()
    {
        ImageView mAnimatedImage = Objects.requireNonNull(getView()).findViewById(R.id.animation_imageview);
        final AnimatedVectorDrawableCompat animation = AnimatedVectorDrawableCompat.create(Objects.requireNonNull(getActivity()), R.drawable.avd_transmit_pulse);
        mAnimatedImage.setImageDrawable(animation);
        final Handler handler = new Handler(Looper.getMainLooper());
        assert animation != null;
        animation.registerAnimationCallback(new Animatable2Compat.AnimationCallback()
        {
            @Override
            public void onAnimationEnd(Drawable drawable)
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        animation.start();
                    }
                });
                super.onAnimationStart(drawable);
            }
        });
        animation.start();
    }
}