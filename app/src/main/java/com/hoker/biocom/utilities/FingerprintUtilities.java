package com.hoker.biocom.utilities;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;

import java.io.IOException;

public class FingerprintUtilities
{
    private static final String GET_VERSION_RESULT_NTAG_216 = "00 04 04 02 01 00 13 03";
    private static final String GET_VERSION_RESULT_DESFIRE_EV1_8K = "AF 04 01 02 01 00 1A 05";
    private static final String GET_VERSION_RESULT_SMARTMX2_P60 = "67 00";
    private static final String GET_VERSION_RESULT_NTAG_I2C_PLUS = "00 04 04 05 02 02 15 03";
    private static final String GET_VERSION_RESULT_DESFIRE_EV2_8K = "AF 04 01 01 12 00 1A 05";

    private static final String NFC_TECH_ISODEP = "android.nfc.tech.IsoDep";
    private static final String NFC_TECH_NFCA = "android.nfc.tech.NfcA";

    public static final String GET_VERSION = "60";

    public static String fingerprintNfcTag(Tag tag)
    {
        //send GET_VERSION command
        String get_version = sendNfcHexCommand(GET_VERSION, tag);
        if(get_version == null)
        {
            return "Unknown";
        }
        switch(get_version)
        {
            case GET_VERSION_RESULT_NTAG_216:
                return "NTAG 216";
            case GET_VERSION_RESULT_DESFIRE_EV1_8K:
                return "DESFire EV1 8K";
            case GET_VERSION_RESULT_SMARTMX2_P60:
                return "SmartMX2 P60";
            case GET_VERSION_RESULT_NTAG_I2C_PLUS:
                return "NTAG I2C Plus 2K";
            case GET_VERSION_RESULT_DESFIRE_EV2_8K:
                return "DESFire EV2 8K";
            default:
                return "Unknown";
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static String sendNfcHexCommand(String command, Tag tag)
    {
        String tech = tag.getTechList()[0];
        byte[] commandBytes = HexUtilities.hexToBytes(command);
        byte[] response = new byte[0];

        try
        {
            if(tech.equals(NFC_TECH_NFCA))
            {
                NfcA nfcA = NfcA.get(tag);
                nfcA.connect();
                response = nfcA.transceive(commandBytes);
            }
            else if(tech.equals(NFC_TECH_ISODEP))
            {
                IsoDep isoDep = IsoDep.get(tag);
                isoDep.connect();
                response = isoDep.transceive(commandBytes);
            }
            if(response.length != 0)
            {
                return HexUtilities.bytesToHex(response);
            }
            return null;
        }
        catch(IOException e)
        {
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