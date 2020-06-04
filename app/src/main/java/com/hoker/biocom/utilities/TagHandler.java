package com.hoker.biocom.utilities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class TagHandler
{
    public static String parseStringNdefPayload(Intent intent)
    {
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
        {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if(rawMessages != null)
            {
                NdefMessage ndefMessage = (NdefMessage)rawMessages[0];
                NdefRecord ndefRecord = ndefMessage.getRecords()[0];
                byte[] payload = ndefRecord.getPayload();
                String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
                int languageCodelength = payload[0] & 0063;
                try
                {
                    return new String(payload, languageCodelength + 1, payload.length - languageCodelength - 1, textEncoding);
                }
                catch(java.io.UnsupportedEncodingException e)
                {
                    throw new NullPointerException("this is just a normal exception");
                }
            }
        }
        return "NDEF payload could not be parsed to a string";
    }

    public static boolean writeNdefText(String text, Tag tag)
    {
        try
        {
            NdefRecord[] records = { createTextRecord(text) };
            NdefMessage message = new NdefMessage(records);
            Ndef ndef = Ndef.get(tag);
            new AsyncConnectWrite().execute(message, ndef);
            return true;
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private static NdefRecord createTextRecord(String text) throws UnsupportedEncodingException
    {
        byte[] textBytes = text.getBytes();
        int textLength = textBytes.length;
        String lang = "en";
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        byte[] payload = new byte[1 + textLength + langLength];
        payload[0] = (byte)langLength;
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }
}

class AsyncConnectWrite extends AsyncTask<Object, Void, String>
{
    @Override
    protected String doInBackground(Object... params)
    {
        String result = "";
        NdefMessage message = (NdefMessage)params[0];
        Ndef ndef = (Ndef)params[1];
        try
        {
            if(ndef != null)
            {
                ndef.connect();
                if(ndef.isConnected())
                {
                    ndef.writeNdefMessage(message);
                }
            }
        }
        catch(FormatException e)
        {
            result = "FormatException while writing";
        }
        catch(TagLostException e)
        {
            result = "TagLostException while writing";
        }
        catch(IOException e)
        {
            result = "IOException while writing";
        }
        finally
        {
            try
            {
                if(ndef != null)
                {
                    ndef.close();
                    result = "Message written";
                }
            }
            catch(IOException e)
            {
                result = "IOException while closing";
            }
        }
        return result;
    }
}
