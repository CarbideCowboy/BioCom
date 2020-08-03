package com.hoker.biocom.utilities;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

import static com.hoker.biocom.utilities.NdefUtilities.createRandomByteRecord;

public class TagUtilities
{
    public static boolean writeNdefMessage(Tag tag, NdefMessage ndefMessage)
    {
        Ndef ndef = Ndef.get(tag);
        String result = new AsyncConnectWrite().doInBackground(ndefMessage, ndef);
        return result.equals("Message written");
    }

    public static boolean eraseNfcTag(Tag tag)
    {
        Ndef ndef = Ndef.get(tag);
        if(ndef != null)
        {
            NdefRecord[] randomByteRecord = { createRandomByteRecord(ndef.getMaxSize()-10) };
            NdefMessage message = new NdefMessage(randomByteRecord);
            String result = new AsyncConnectWrite().doInBackground(message, ndef);
            if(result.equals("Message written"))
            {
                NdefRecord ndefRecord = NdefRecord.createTextRecord( "en", "");
                NdefMessage ndefMessage = new NdefMessage(ndefRecord);
                return writeNdefMessage(tag, ndefMessage);
            }
            else
            {
                return false;
            }
        }
        return false;
    }
}
