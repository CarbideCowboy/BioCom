package com.hoker.biocom.utilities;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

import java.io.IOException;

import static com.hoker.biocom.utilities.NdefUtilities.createRandomByteRecord;

public class TagUtilities
{
    public static boolean writeNdefMessage(Tag tag, NdefMessage ndefMessage)
    {
        Ndef ndef = Ndef.get(tag);
        try
        {
            if(ndef != null)
            {
                ndef.connect();
                if(ndef.isConnected())
                {
                    ndef.writeNdefMessage(ndefMessage);
                }
            }
        }
        catch(Exception e)
        {
            try
            {
                ndef.close();
                return false;
            } catch (IOException ex)
            {
                return false;
            }
        }
        try
        {
            if(ndef != null)
            {
                ndef.close();
                return true;
            }
        }
        catch(IOException e)
        {
            return false;
        }
        return true;
    }

    public static boolean eraseNfcTag(Tag tag)
    {
        Ndef ndef = Ndef.get(tag);
        NdefRecord[] randomByteRecord = { createRandomByteRecord(ndef.getMaxSize()-10) };
        NdefMessage message = new NdefMessage(randomByteRecord);
        if(writeNdefMessage(tag, message))
        {
            NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null);
            NdefMessage ndefMessage = new NdefMessage(ndefRecord);
            return writeNdefMessage(tag, ndefMessage);
        }
        else
        {
            return false;
        }
    }
}
