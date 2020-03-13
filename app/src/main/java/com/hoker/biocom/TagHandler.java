package com.hoker.biocom;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

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
}
