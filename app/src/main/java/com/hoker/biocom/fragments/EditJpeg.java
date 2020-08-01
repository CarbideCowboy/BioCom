package com.hoker.biocom.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefRecord;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hoker.biocom.R;
import com.hoker.biocom.interfaces.IEditFragment;
import com.hoker.biocom.interfaces.ITracksPayload;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class EditJpeg extends Fragment implements IEditFragment
{
    Button mImageButton;
    ImageView mImageView;
    SeekBar mSeekBar;
    TextView mCompressionTextView;
    Uri _imagePath;
    Bitmap _bitmap;
    int _qualityLevel = 80;
    ITracksPayload payloadInterface;

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
        mImageButton = (Button) Objects.requireNonNull(Objects.requireNonNull(getView()).findViewById(R.id.edit_jpeg_button));
        mImageView = (ImageView) Objects.requireNonNull(getView().findViewById(R.id.edit_jpeg_image));
        mCompressionTextView = getView().findViewById(R.id.edit_jpeg_textview);
        mSeekBar = getView().findViewById(R.id.compression_seekbar);
        setListeners();
        setupImageView();
        setCompressionTextView();
    }

    private void setCompressionTextView()
    {
        String compressionAmount = getString(R.string.jpeg_compression_level) + ": " + (100 - _qualityLevel) + getString(R.string.percentage);
        mCompressionTextView.setText(compressionAmount);
    }

    private void setupImageView()
    {
        assert getArguments() != null;
        byte[] payload = getArguments().getByteArray("Payload");
        assert payload != null;
        _bitmap = BitmapFactory.decodeByteArray(payload, 0, payload.length);
        mImageView.setImageBitmap(_bitmap);
        payloadInterface.payloadChanged();
    }

    private void setListeners()
    {
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

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                _qualityLevel = 100 - (progress * 10);
                payloadInterface.payloadChanged();
                setCompressionTextView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
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
                setImage();
            }
        }
    }

    private void setImage()
    {
        InputStream inputStream;
        try
        {
            inputStream = Objects.requireNonNull(getActivity()).getContentResolver().openInputStream(_imagePath);
            _bitmap = BitmapFactory.decodeStream(inputStream);
            mImageView.setImageBitmap(_bitmap);
            payloadInterface.payloadChanged();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            Toast.makeText(getActivity(), "An error occurred.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public NdefRecord getRecord()
    {
        if(_bitmap == null)
        {
            return NdefRecord.createMime("image/jpeg", new byte[0]);
        }
        else
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            _bitmap.compress(Bitmap.CompressFormat.JPEG, _qualityLevel, stream);
            byte[] bytes = stream.toByteArray();
            return NdefRecord.createMime("image/jpeg", bytes);
        }
    }

    @Override
    public void setPayloadTrackingInterface(ITracksPayload iTracksPayload)
    {
        this.payloadInterface = iTracksPayload;
    }
}