package com.hoker.biocom.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hoker.biocom.R;

import java.util.Objects;

public class ReadJpeg extends Fragment
{
    ImageView mImageView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_read_jpeg, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        mImageView = Objects.requireNonNull(getView()).findViewById(R.id.read_jpeg_image);
        assert getArguments() != null;
        byte[] payload = getArguments().getByteArray("Payload");
        assert payload != null;
        Bitmap bitmap = BitmapFactory.decodeByteArray(payload, 0, payload.length);
        mImageView.setImageBitmap(bitmap);
    }
}