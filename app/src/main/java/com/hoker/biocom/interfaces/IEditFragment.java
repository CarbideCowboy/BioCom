package com.hoker.biocom.interfaces;

import android.nfc.NdefRecord;

public interface IEditFragment
{
    NdefRecord getRecord();
    void setPayloadInterface(ITracksPayload iTracksPayload);
}
