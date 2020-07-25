package com.hoker.biocom.utilities;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;

import java.io.IOException;

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
            try
            {
                ndef.close();
                return result;
            }
            catch(IOException ex)
            {
                result = "IOException while closing";
                return result;
            }
        }
        catch(TagLostException e)
        {
            result = "TagLostException while writing";
            try
            {
                ndef.close();
                return result;
            }
            catch(IOException ex)
            {
                result = "IOException while closing";
                return result;
            }
        }
        catch(IOException e)
        {
            result = "IOException while writing";
            try
            {
                ndef.close();
                return result;
            }
            catch(IOException ex)
            {
                result = "IOException while closing";
                return result;
            }
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
