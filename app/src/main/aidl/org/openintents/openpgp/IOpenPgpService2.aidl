// IOpenPgpService2.aidl
package org.openintents.openpgp;

interface IOpenPgpService2
{
    /**
     * see org.openintents.openpgp.util.OpenPgpApi for documentation
     */
    ParcelFileDescriptor createOutputPipe(in int pipeId);

    /**
     * see org.openintents.openpgp.util.OpenPgpApi for documentation
     */
    Intent execute(in Intent data, in ParcelFileDescriptor input, int pipeId);
}
