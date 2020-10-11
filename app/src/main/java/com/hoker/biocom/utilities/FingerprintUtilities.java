package com.hoker.biocom.utilities;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;

import java.io.IOException;

public class FingerprintUtilities
{
    private static final String GET_VERSION_RESULT_NTAG_210 = "00 04 04 01 01 00 0B 03";
    private static final String GET_VERSION_RESULT_NTAG_212 = "00 04 04 01 01 00 0E 03";
    private static final String GET_VERSION_RESULT_NTAG_213 = "00 04 04 02 01 00 0F 03";
    private static final String GET_VERSION_RESULT_NTAG_213F = "00 04 04 04 01 00 0F 03";
    private static final String GET_VERSION_RESULT_NTAG_215 = "00 04 04 02 01 00 11 03";
    private static final String GET_VERSION_RESULT_NTAG_216 = "00 04 04 02 01 00 13 03";
    private static final String GET_VERSION_RESULT_NTAG_216F = "00 04 04 04 01 00 13 03";
    private static final String GET_VERSION_RESULT_NTAG_I2C_PLUS_2K = "00 04 04 05 02 02 15 03";
    private static final String GET_VERSION_RESULT_DESFIRE_EV1_4K = "AF 04 01 01 01 00 18 05";
    private static final String GET_VERSION_RESULT_DESFIRE_EV1_8K = "AF 04 01 02 01 00 1A 05";
    private static final String GET_VERSION_RESULT_DESFIRE_EV2_8K = "AF 04 01 01 12 00 1A 05";
    private static final String GET_VERSION_RESULT_SMARTMX2_P60 = "67 00";

    private static final String NFC_TECH_ISODEP = "android.nfc.tech.IsoDep";
    private static final String NFC_TECH_NFCA = "android.nfc.tech.NfcA";

    public static final String GET_VERSION = "60";
    public static final String AUTHENTICATE = "1A";

    public static String fingerprintNfcTag(Tag tag)
    {
        //send GET_VERSION command
        String get_version = sendNfcHexCommand(GET_VERSION, tag);

        //logic is repetitious, but until I get an NTAG 203 to test with, it'll have to do
        if(get_version == null)
        {
            String authResponse = sendNfcHexCommand(AUTHENTICATE, tag);
            if(authResponse != null)
            {
                return "Mifare Ultralight C";
            }
            else
            {
                MifareUltralight mifareUltralight = MifareUltralight.get(tag);
                try
                {
                    mifareUltralight.connect();
                    byte[] pageBytes = mifareUltralight.readPages(41);
                    mifareUltralight.close();
                    if (pageBytes != null)
                    {
                        return "Mifare Ultralight";
                    }
                    else
                    {
                        return "NTAG 203";
                    }
                }
                catch(IOException e)
                {
                    try
                    {
                        mifareUltralight.close();
                    }
                    catch(Exception closeError)
                    {
                        return "NTAG 203";
                    }
                    return "NTAG 203";
                }
            }
        }
        switch(get_version)
        {
            case GET_VERSION_RESULT_NTAG_210:
                return "NTAG 210";
            case GET_VERSION_RESULT_NTAG_212:
                return "NTAG 212";
            case GET_VERSION_RESULT_NTAG_213:
                return "NTAG 213";
            case GET_VERSION_RESULT_NTAG_213F:
                return "NTAG 213F";
            case GET_VERSION_RESULT_NTAG_215:
                return "NTAG 215";
            case GET_VERSION_RESULT_NTAG_216:
                return "NTAG 216";
            case GET_VERSION_RESULT_NTAG_216F:
                return "NTAG 216F";
            case GET_VERSION_RESULT_NTAG_I2C_PLUS_2K:
                return "NTAG I2C Plus 2K";
            case GET_VERSION_RESULT_DESFIRE_EV1_4K:
                return "DESFire EV1 4K";
            case GET_VERSION_RESULT_DESFIRE_EV1_8K:
                return "DESFire EV1 8K";
            case GET_VERSION_RESULT_DESFIRE_EV2_8K:
                return "DESFire EV2 8K";
            case GET_VERSION_RESULT_SMARTMX2_P60:
                return "SmartMX2 P60";
            default:
                return "Unknown";
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static String sendNfcHexCommand(String command, Tag tag)
    {
        String tech = tag.getTechList()[0];
        byte[] commandBytes = HexUtilities.hexToBytes(command);
        byte[] response = new byte[0];

        NfcA nfcA = null;
        IsoDep isoDep = null;

        try
        {
            if(tech.equals(NFC_TECH_NFCA))
            {
                nfcA = NfcA.get(tag);
                nfcA.connect();
                response = nfcA.transceive(commandBytes);
                nfcA.close();
            }
            else if(tech.equals(NFC_TECH_ISODEP))
            {
                isoDep = IsoDep.get(tag);
                isoDep.connect();
                response = isoDep.transceive(commandBytes);
                isoDep.close();
            }
            if(response.length != 0)
            {
                return HexUtilities.bytesToHex(response);
            }
            return null;
        }
        catch(IOException e)
        {
            try
            {
                if(nfcA != null)
                {
                    nfcA.close();
                }
                if(isoDep != null)
                {
                    isoDep.close();
                }
            }
            catch(Exception closeError)
            {
                return null;
            }
            return null;
        }
    }

    public static String getManufacturerFromByte(byte manufacturerByte)
    {
        if(manufacturerByte == (byte)0x04)
        {
            return "NXP Semiconductors";
        }
        else
        {
            return "Unknown Manufacturer";
        }
    }
}