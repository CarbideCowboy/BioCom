package com.hoker.biocom.utilities;

public class OpenKeychainIntents
{
    private static final String PACKAGE_NAME = "org.sufficientlysecure.keychain";

    // prefix packagename for exported Intents
    // as described in http://developer.android.com/guide/components/intents-filters.html
    private static final String INTENT_PREFIX = PACKAGE_NAME + ".action.";
    private static final String EXTRA_PREFIX = PACKAGE_NAME + ".";

    public static final String ENCRYPT_TEXT = INTENT_PREFIX + "ENCRYPT_TEXT";
    public static final String ENCRYPT_EXTRA_TEXT = EXTRA_PREFIX + "EXTRA_TEXT"; // String

    public static final String ENCRYPT_DATA = INTENT_PREFIX + "ENCRYPT_DATA";
    public static final String ENCRYPT_EXTRA_ASCII_ARMOR = EXTRA_PREFIX + "EXTRA_ASCII_ARMOR"; // boolean

    public static final String DECRYPT_TEXT = INTENT_PREFIX + "DECRYPT_TEXT";
    public static final String DECRYPT_EXTRA_TEXT = EXTRA_PREFIX + "EXTRA_TEXT"; // String

    public static final String DECRYPT_DATA = INTENT_PREFIX + "DECRYPT_DATA";

    public static final String IMPORT_KEY = INTENT_PREFIX + "IMPORT_KEY";
    public static final String IMPORT_EXTRA_KEY_EXTRA_KEY_BYTES = EXTRA_PREFIX + "EXTRA_KEY_BYTES"; // byte[]

    public static final String IMPORT_KEY_FROM_KEYSERVER = INTENT_PREFIX + "IMPORT_KEY_FROM_KEYSERVER";
    public static final String IMPORT_KEY_FROM_KEYSERVER_EXTRA_QUERY = EXTRA_PREFIX + "EXTRA_QUERY"; // String
    public static final String IMPORT_KEY_FROM_KEYSERVER_EXTRA_FINGERPRINT = EXTRA_PREFIX + "EXTRA_FINGERPRINT"; // String

    public static final String IMPORT_KEY_FROM_QR_CODE = INTENT_PREFIX + "IMPORT_KEY_FROM_QR_CODE";

    public static final String TEST_SIGNED_MESSAGE = "-----BEGIN PGP SIGNED MESSAGE-----\n" +
            "Hash: SHA1\n" +
            "\n" +
            "Hello world!\n" +
            "-----BEGIN PGP SIGNATURE-----\n" +
            "Version: GnuPG v1.4.12 (GNU/Linux)\n" +
            "Comment: Using GnuPG with Thunderbird - http://www.enigmail.net/\n" +
            "\n" +
            "iQEcBAEBAgAGBQJS/7vTAAoJEHGMBwEAASKCkGYH/2jBLzamVyqd61jrjMQM0jUv\n" +
            "MkDcPUxPrYH3wZOO0HcgdBQEo66GZEC2ATmo8izJUMk35Q5jas99k0ac9pXhPUPE\n" +
            "5qDXdQS10S07R6J0SeDYFWDSyrSiDTCZpFkVu3JGP/3S0SkMYXPzfYlh8Ciuxu7i\n" +
            "FR5dmIiz3VQaBgTBSCBFEomNFM5ypynBJqKIzIty8v0NbV72Rtg6Xg76YqWQ/6MC\n" +
            "/MlT3y3++HhfpEmLf5WLEXljbuZ4SfCybgYXG9gBzhJu3+gmBoSicdYTZDHSxBBR\n" +
            "BwI+ueLbhgRz+gU+WJFE7xNw35xKtBp1C4PR0iKI8rZCSHLjsRVzor7iwDaR51M=\n" +
            "=3Ydc\n" +
            "-----END PGP SIGNATURE-----";

}
