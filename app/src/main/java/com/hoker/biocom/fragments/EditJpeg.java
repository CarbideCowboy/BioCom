package com.hoker.biocom.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefRecord;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hoker.biocom.R;
import com.hoker.biocom.interfaces.IEditFragment;
import com.hoker.biocom.interfaces.ITracksPayload;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class EditJpeg extends Fragment implements IEditFragment
{
    Button mImageButton;
    Uri _imagePath;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_edit_jpeg, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        mImageButton = (Button) Objects.requireNonNull(getView().findViewById(R.id.edit_jpeg_button));
        mImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                startActivityForResult(Intent.createChooser(intent, "Select a JPEG Image"), 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK)
        {
            if(requestCode == 1)
            {
                _imagePath = data.getData();
            }
        }
    }

    @Override
    public NdefRecord getRecord()
    {
        return NdefRecord.createMime("biocom/jpeg", new byte[0]);
    }

    @Override
    public void setPayloadTrackingInterface(ITracksPayload iTracksPayload)
    {

    }
}