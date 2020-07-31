package com.hoker.biocom.utilities;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;

import java.nio.charset.StandardCharsets;
import java.util.Random;

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
                return new String(payload, StandardCharsets.UTF_8);
            }
        }
        return "NDEF payload could not be parsed to a string";
    }

    public static NdefMessage getNdefMessage(Intent intent)
    {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(rawMessages != null)
        {
            return (NdefMessage)rawMessages[0];
        }
        return null;
    }

    public static boolean writeNdefText(String text, Tag tag)
    {
        NdefRecord[] records = { createTextRecord(text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        String result = new AsyncConnectWrite().doInBackground(message, ndef);
        return result.equals("Message written");
    }

    public static boolean writeNdefMessage(Tag tag, NdefMessage ndefMessage)
    {
        Ndef ndef = Ndef.get(tag);
        String result = new AsyncConnectWrite().doInBackground(ndefMessage, ndef);
        return result.equals("Message written");
    }

    public static NdefRecord createTextRecord(String text)
    {
        byte[] textBytes = text.getBytes();
        int textLength = textBytes.length;
        String lang = "en";
        byte[] langBytes = lang.getBytes(StandardCharsets.US_ASCII);
        int langLength = langBytes.length;
        byte[] payload = new byte[1 + textLength + langLength];
        payload[0] = (byte)langLength;
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }

    private static NdefRecord createRandomByteRecord(int maxTagCapacity)
    {
        String lang = "en";
        byte[] langBytes = lang.getBytes(StandardCharsets.US_ASCII);
        byte[] randomByteArray = new byte[maxTagCapacity];
        Random random = new Random();
        random.nextBytes(randomByteArray);
        byte[] payload = new byte[1 + langBytes.length + randomByteArray.length];
        payload[0] = (byte)langBytes.length;
        System.arraycopy(langBytes, 0, payload, 1, langBytes.length);
        System.arraycopy(randomByteArray, 0, payload, 1 + langBytes.length, randomByteArray.length);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
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
                return writeNdefText("", tag);
            }
            else
            {
                return false;
            }
        }
        return false;
    }
}
