/**
 * This file is part of JCANOpen. Copyright (C) 2014 K. Dermitzakis
 * <dermitza@gmail.com>
 *
 * JCANOpen is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * JCANOpen is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JCANOpen. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.dermitza.jcanopen.can.devices.ixxat;

import ch.dermitza.jcanopen.can.CanDeviceIF;
import de.ixxat.vci3.IVciDevice;
import de.ixxat.vci3.IVciDeviceManager;
import de.ixxat.vci3.IVciEnumDevice;
import de.ixxat.vci3.VciDeviceCapabilities;
import de.ixxat.vci3.VciDeviceInfo;
import de.ixxat.vci3.VciException;
import de.ixxat.vci3.VciServer;
import de.ixxat.vci3.bal.BalFeatures;
import de.ixxat.vci3.bal.BalSocketInfo;
import de.ixxat.vci3.bal.IBalObject;
import de.ixxat.vci3.bal.IBalResource;
import de.ixxat.vci3.bal.can.CanBitrate;
import de.ixxat.vci3.bal.can.CanCapabilities;
import de.ixxat.vci3.bal.can.CanChannelStatus;
import de.ixxat.vci3.bal.can.CanLineStatus;
import de.ixxat.vci3.bal.can.ICanChannel;
import de.ixxat.vci3.bal.can.ICanControl;
import de.ixxat.vci3.bal.can.ICanMessageReader;
import de.ixxat.vci3.bal.can.ICanMessageWriter;
import de.ixxat.vci3.bal.can.ICanScheduler;
import de.ixxat.vci3.bal.can.ICanSocket;

/**
 * 
 *
 * @author K. Dermitzakis
 * @version 0.05
 * @since   0.03
 */
public class Ixxat implements CanDeviceIF {

    private VciServer vciServer = null;
    private IVciDeviceManager deviceManager = null;
    private IVciEnumDevice vciEnumDevice = null;
    VciDeviceInfo vciDeviceInfo[] = null;
    IVciDevice vciDevice = null;
    IBalObject balObject = null;
    ICanControl[] canControl;
    ICanSocket[] canSocket;
    ICanChannel[] canChannel;
    ICanScheduler[] canScheduler;
    ICanMessageReader[] canMsgReader;
    ICanMessageWriter[] canMsgWriter;
    private int[] freq;
    private int[] tscDiv; // Timestamp clock divisor
    private int[] dtxDiv; // Transmitter delay divisor
    private IxxatCanMessageReader[] reader;
    private IxxatCanMessageWriter[] writer;

    @Override
    public void connect() {
        // Create VCI Server Object
        try {
            vciServer = new VciServer();
            // Print Version Number
            System.out.println(vciServer.GetVersion());

            // Open VCI Device Manager
            deviceManager = vciServer.GetDeviceManager();

            // Open VCI Device Enumerator
            vciEnumDevice = deviceManager.EnumDevices();
        } catch (Throwable oException) {
            oException.printStackTrace();
            System.out.print("Error initializing IXXAT, shutting down");
            System.exit(-1);
        }
        System.out.print("Wait for VCI Device Enum Event: ...");
        try {
            vciEnumDevice.WaitFor(3000);
            System.out.println("... change detected!");
        } catch (Throwable oException) {
            System.out.println("... NO change detected!");
        }
        // Show Device list and count devices
        if (vciEnumDevice != null) {
            boolean fEndFlag = false;
            boolean fCounting = true;
            int dwVciDeviceCount = 0;
            int dwVciDeviceIndex = 0;
            VciDeviceInfo oVciDeviceInfo = null;

            do {
                try {
                    // Try to get next device
                    oVciDeviceInfo = vciEnumDevice.Next();
                } catch (Throwable oException) {
                    // Last device reached?
                    oVciDeviceInfo = null;
                }

                // Device available
                if (oVciDeviceInfo != null) {
                    // Do counting only?
                    if (fCounting) {
                        // Print Device Info
                        dwVciDeviceCount++;
                        System.out.println("\nVCI Device: " + dwVciDeviceCount + "\n" + oVciDeviceInfo);
                    } else {
                        if (dwVciDeviceIndex < vciDeviceInfo.length) {
                            vciDeviceInfo[dwVciDeviceIndex++] = oVciDeviceInfo;
                        } else {
                            throw new IndexOutOfBoundsException("VCI Device list has changed during scan -> ABORT");
                        }
                    }
                } else {
                    // Do counting only?
                    if (fCounting) {
                        //Switch of counting and build device list
                        fCounting = false;

                        // Reset Enum Device Index
                        try {
                            vciEnumDevice.Reset();
                        } catch (Throwable oException) {
                            System.out.println("Error resetting Enum Device Index");
                            oException.printStackTrace();
                        }

                        // Build device info list
                        vciDeviceInfo = new VciDeviceInfo[dwVciDeviceCount];
                    } else {
                        fEndFlag = true;

                        // Check if device list has changed
                        if (dwVciDeviceIndex != vciDeviceInfo.length) {
                            throw new IndexOutOfBoundsException("VCI Device list has changed during scan -> ABORT");
                        }
                    }
                }
            } while (!fEndFlag);
        } else {
            System.out.println("No device found.");
            System.exit(0);
        }

        // If more than one device ask user
        long lVciId = 0;
        if (vciDeviceInfo.length != 1) {
            try {
                lVciId = deviceManager.SelectDeviceDialog();
            } catch (Throwable oException) {
                System.err.println("IVciDeviceManager.SelectDeviceDialog() failed with error: " + oException);
                System.out.println("Opening first found VCI 3 board instead\n");

                // In case of error try to open the first board
                lVciId = vciDeviceInfo[0].m_qwVciObjectId;
            }
        } else {
            lVciId = vciDeviceInfo[0].m_qwVciObjectId;
        }

        // Open VCI Device
        try {
            vciDevice = deviceManager.OpenDevice(lVciId);
        } catch (Throwable oException) {
            System.out.println("Error opening VCI Device");
            oException.printStackTrace();
        }

        // Get Device Info and Capabilities
        if (vciDevice != null) {
            VciDeviceCapabilities oVciDeviceCaps = null;
            VciDeviceInfo oVciDeviceInfo = null;

            try {
                oVciDeviceCaps = vciDevice.GetDeviceCaps();
                System.out.println("VCI Device Capabilities: " + oVciDeviceCaps);
            } catch (Throwable oException) {
                System.out.println("Error getting VCI Device Capabilities");
                oException.printStackTrace();
            }

            try {
                oVciDeviceInfo = vciDevice.GetDeviceInfo();
                System.out.println("VCI Device Info: " + oVciDeviceInfo);
            } catch (Throwable oException) {
                System.out.println("Error getting VCI Device Info");
                oException.printStackTrace();
            }

        }

        // Open BAL Object
        try {
            balObject = vciDevice.OpenBusAccessLayer();
        } catch (Throwable oException) {
            System.out.println("Error opening BAL Object");
            oException.printStackTrace();
        }

        // Free VciEnumDevice, DeviceManager and VCI Server which are not longer needed
        try {
            vciEnumDevice.Dispose();
            vciEnumDevice = null;
            deviceManager.Dispose();
            deviceManager = null;
            vciServer.Dispose();
            vciServer = null;
        } catch (Throwable oException) {
            System.out.println("Error disposing resources");
            oException.printStackTrace();
        }

        /* INITIALIZE ARRAYS */
        int sockets = 0;
        try{
            BalFeatures oBalFeatures = balObject.GetFeatures();
            sockets = oBalFeatures.m_wBusSocketCount;
            oBalFeatures = null;
        } catch(Throwable t){
            // TODO DICK;
            System.exit(-1);
        }
        
        System.out.println("Sockets: " + sockets);
        canControl = new ICanControl[sockets];
        canSocket = new ICanSocket[sockets];
        canChannel = new ICanChannel[sockets];
        canScheduler = new ICanScheduler[sockets];
        canMsgReader = new ICanMessageReader[sockets];
        canMsgWriter = new ICanMessageWriter[sockets];
        reader = new IxxatCanMessageReader[sockets];
        writer = new IxxatCanMessageWriter[sockets];
        freq = new int[sockets];
        tscDiv = new int[sockets];// Timestamp clock divisor
        dtxDiv = new int[sockets]; // Transmitter delay divisor
    }

    @Override
    public void disconnect() {
        // Stop EVERY CAN SOCKET
        for(int i=0; i < canControl.length; i++){
        if (canControl[i] != null) {
            try {
                System.out.println("Stopping CAN Controller[" + i+"]");
                canControl[i].StopLine();
                canControl[i].ResetLine();
            } catch (Throwable oException) {
                if (oException instanceof VciException) {
                    VciException oVciException = (VciException) oException;
                    System.err.println("Reset CAN Controller " + i +", VciException: " + oVciException + " => " + oVciException.VciFormatError());
                } else {
                    System.err.println("Reset CAN Controller " + i +", Exception: " + oException);
                }
            }
        }

        // release all references
        System.out.println("Cleaning up CAN Interface references");

        if (reader[i] != null) {
            reader[i].dispose();
        }

        if (writer[i] != null) {
            writer[i].dispose();
        }

        try {
            canControl[i].Dispose();
        } catch (Throwable t) {
            System.err.println("Error while disposing CAN control[" + i + "]");
        } finally {
            canControl[i] = null;
        }
        try {
            canSocket[i].Dispose();
        } catch (Throwable t) {
            System.err.println("Error while disposing CAN socket[" + i+"]");
        } finally {
            canSocket[i] = null;
        }
        try {
            canScheduler[i].Dispose();
        } catch (Throwable t) {
            System.err.println("Error while disposing CAN scheduler[" + i+"]");
        } finally {
            canScheduler[i] = null;
        }
        if (canMsgWriter[i] != null) {
            try {
                canMsgWriter[i].Dispose();
            } catch (Throwable t) {
                System.err.println("Error while disposing CAN writer[" + i + "]");
            }
            canMsgWriter[i] = null;
        }
        if (canMsgReader[i] != null) {
            try {
                canMsgReader[i].Dispose();
            } catch (Throwable t) {
                System.err.println("Error while disposing CAN reader[" + i + "]");
            }
            canMsgReader[i] = null;
        }
        try {
            canChannel[i].Dispose();
        } catch (Throwable t) {
            System.err.println("Error while disposing CAN channel[" + i + "]");
        } finally {
            canChannel[i] = null;
        }
        // release all references
        System.out.println("Cleaning up Interface references to VCI Device");
        try {
            balObject.Dispose();
        } catch (Throwable t) {
            System.err.println("Error while disposing CAN BAL object");
        } finally {
            balObject = null;
        }
        try {
            vciDevice.Dispose();
        } catch (Throwable oException) {
            System.err.println("Error while disposing VCI device");
        } finally {
            vciDevice = null;
        }
        System.out.println("Program finished!");
        }
    }

    @Override
    public void initCan(int socketNo) {
        // Open CAN Control and CAN Channel
        try {
            IBalResource oBalResource = null;
            BalFeatures oBalFeatures = null;

            System.out.println("Using Socket Number: " + socketNo);
            oBalFeatures = balObject.GetFeatures();
            System.out.println("BAL Features: " + oBalFeatures);

            // Socket available?
            if (oBalFeatures.m_wBusSocketCount > socketNo) {
                // Ensure CAN Controller Type
                if (oBalFeatures.m_awBusType[socketNo] == VciDeviceCapabilities.VCI_BUS_CAN) {
                    // Get CAN Control
                    try {
                        oBalResource = balObject.OpenSocket(socketNo, IBalResource.IID_ICanControl);
                        canControl[socketNo] = (ICanControl) oBalResource;

                        oBalResource = null;
                    } catch (Throwable oException) {
                        System.err.println("Error while initializing CAN control");
                        if (oException instanceof VciException) {
                            VciException oVciException = (VciException) oException;
                            System.err.println("Open Socket(IID_ICanControl), VciException: " + oVciException + " => " + oVciException.VciFormatError());
                        } else {
                            System.err.println("Open Socket(IID_ICanControl), exception: " + oException);
                        }
                    }

                    // Get CAN Socket
                    try {
                        oBalResource = balObject.OpenSocket(socketNo, IBalResource.IID_ICanSocket);
                        canSocket[socketNo] = (ICanSocket) oBalResource;

                        oBalResource = null;
                    } catch (Throwable oException) {
                        System.err.println("Error while initializing CAN socket");
                        if (oException instanceof VciException) {
                            VciException oVciException = (VciException) oException;
                            System.err.println("Open Socket(IID_ICanSocket), VciException: " + oVciException + " => " + oVciException.VciFormatError());
                        } else {
                            System.err.println("Open Socket(IID_ICanSockets), exception: " + oException);
                        }
                    }

                    // Get CAN Scheduler
                    try {
                        oBalResource = balObject.OpenSocket(socketNo, IBalResource.IID_ICanScheduler);
                        canScheduler[socketNo] = (ICanScheduler) oBalResource;
                        oBalResource = null;
                    } catch (Throwable oException) {
                        System.err.println("Error while initializing CAN scheduler");
                        if (oException instanceof VciException) {
                            VciException oVciException = (VciException) oException;
                            System.err.println("Open Socket(IID_ICanScheduler), VciException: " + oVciException + " => " + oVciException.VciFormatError());
                        } else {
                            System.err.println("Open Socket(IID_ICanScheduler), exception: " + oException);
                        }
                    }
                } else {
                    System.err.println("Socket No. " + socketNo + " is not a \"VCI_BUS_CAN\"");
                }
            } else {
                System.err.println("Socket No. " + socketNo + " is not a available!");
            }
        } catch (Throwable oException) {
            if (oException instanceof VciException) {
                VciException oVciException = (VciException) oException;
                System.err.println("VciException: " + oVciException + " => " + oVciException.VciFormatError());
            } else {
                System.err.println("Exception: " + oException);
                oException.printStackTrace();
            }
        }

        // Test CAN
        try {
            // Create CAN Channel
            if (canSocket != null) {
                BalSocketInfo oBalSocketInfo = null;
                CanCapabilities oCanCapabilities = null;
                CanLineStatus oCanLineStatus = null;

                oBalSocketInfo = canSocket[socketNo].GetSocketInfo();
                System.out.println("BAL Socket Info: " + oBalSocketInfo);

                oCanCapabilities = canSocket[socketNo].GetCapabilities();
                System.out.println("CAN Capabilities: " + oCanCapabilities);

                tscDiv[socketNo] = oCanCapabilities.m_dwTscDivisor;
                dtxDiv[socketNo] = oCanCapabilities.m_dwDtxDivisor;
                freq[socketNo] = oCanCapabilities.m_dwClockFreq;

                oCanLineStatus = canSocket[socketNo].GetLineStatus();
                System.out.println("CAN Line Status: " + oCanLineStatus);

                System.out.println("Creating CAN Channel");
                canChannel[socketNo] = canSocket[socketNo].CreateChannel(false);
            }

            // Configure, start Channel and Query Reader and Writer
            if (canChannel[socketNo] != null) {
                System.out.println("Initializing CAN Message Channel");
                canChannel[socketNo].Initialize(Short.MAX_VALUE, Short.MAX_VALUE);
                canChannel[socketNo].Activate();

                System.out.println("Querying Message Reader");
                canMsgReader[socketNo] = canChannel[socketNo].GetMessageReader();

                System.out.println("Querying Message Writer");
                canMsgWriter[socketNo] = canChannel[socketNo].GetMessageWriter();
            }

            // Configure and start CAN Controller
            if (canControl[socketNo] != null) {
                CanBitrate oCanBitrate = new CanBitrate(CanBitrate.Cia1000KBit);
                CanChannelStatus oChanStatus = null;
                CanLineStatus oLineStatus = null;

                // Get Line Status
                oLineStatus = canControl[socketNo].GetLineStatus();
                System.out.println("CAN Line Status: " + oLineStatus);

                // Try to detect BaudRate
                try {
                    CanBitrate[] oaCanBitrateList = null;
                    CanBitrate oBitrateDetected = null;
                    oaCanBitrateList = new CanBitrate[]{new CanBitrate(CanBitrate.Cia10KBit),
                        new CanBitrate(CanBitrate.Cia20KBit),
                        new CanBitrate(CanBitrate.Cia50KBit),
                        new CanBitrate(CanBitrate.Cia125KBit),
                        new CanBitrate(CanBitrate.Cia250KBit),
                        new CanBitrate(CanBitrate.Cia500KBit),
                        new CanBitrate(CanBitrate.Cia800KBit),
                        new CanBitrate(CanBitrate.Cia1000KBit)};

                    // Detect BaudRate, wait 100ms between two messages
                    oBitrateDetected = canControl[socketNo].DetectBaud(100, oaCanBitrateList);
                    System.out.println("Detected Baudrate: " + oBitrateDetected);

                } catch (Throwable oException) {
                    if (oException instanceof VciException) {
                        VciException oVciException = (VciException) oException;
                        System.err.println("DetectBaudrate failed with VciException: " + oVciException + " => " + oVciException.VciFormatError());
                    } else {
                        System.err.println("DetectBaudrate failed with Exception: " + oException);
                    }
                }

                System.out.println("Starting CAN Controller with 1000 kBAUD");
                canControl[socketNo].InitLine(ICanControl.CAN_OPMODE_STANDARD
                        | ICanControl.CAN_OPMODE_EXTENDED,
                        oCanBitrate);

                // Filter closed completely
                canControl[socketNo].SetAccFilter(ICanControl.CAN_FILTER_STD, 0xFFFF, 0xFFFF);
                // Filter opened completely
                canControl[socketNo].SetAccFilter(ICanControl.CAN_FILTER_STD, 0, 0);

                // Add ID 1
                canControl[socketNo].AddFilterIds(ICanControl.CAN_FILTER_STD, 1, 0xFFFF);
                // Remove ID 1
                canControl[socketNo].RemFilterIds(ICanControl.CAN_FILTER_STD, 1, 0xFFFF);

                // Start
                canControl[socketNo].StartLine();

                // Wait for controller
                Thread.sleep(250);

                // Get CAN Channel Status
                oChanStatus = canChannel[socketNo].GetStatus();
                System.out.println("CAN Channel Status: " + oChanStatus);
                System.out.println("CAN Line Status: " + oChanStatus.m_oCanLineStatus);
                System.out.println("");
            }

            // CAN Scheduler available
            //if (oCanScheduler != null) {
            //    TestCanScheduler(oCanScheduler);
            //}
            // Read CAN Messages
            if (canMsgReader[socketNo] != null) {
                //TestCanMessageReader(oCanMsgReader);
                reader[socketNo] = new IxxatCanMessageReader(canMsgReader[socketNo], freq[socketNo], tscDiv[socketNo]);
                System.out.println("Message reader created");
            }

            // Write CAN Messages
            if (canMsgWriter[socketNo] != null) {
                writer[socketNo] = new IxxatCanMessageWriter(canMsgWriter[socketNo], dtxDiv[socketNo], freq[socketNo]);
                System.out.println("Message writer created");
                //    TestCanMessageWriter(oCanMsgWriter, oCanMsgReader);

                // Release CAN Message Writer
                //   oCanMsgWriter.Dispose();
                //   oCanMsgWriter = null;
            }

            // Dispose CAN Message Reader
            //if (oCanMsgReader != null) {
            // Release CAN Message Reader
            //    oCanMsgReader.Dispose();
            //    oCanMsgReader = null;
            //}
        } catch (Throwable oException) {
            if (oException instanceof VciException) {
                VciException oVciException = (VciException) oException;
                System.err.println("VciException: " + oVciException + " => " + oVciException.VciFormatError());
            } else {
                System.err.println("Exception: " + oException);
            }
        }

        if (canControl[socketNo] != null) {
            try {
                // Get Line Status
                CanLineStatus oLineStatus = null;
                oLineStatus = canControl[socketNo].GetLineStatus();
                System.out.println("CAN Line Status: " + oLineStatus);
            } catch (Throwable oException) {
                if (oException instanceof VciException) {
                    VciException oVciException = (VciException) oException;
                    System.err.println("Get CAN Line Status, VciException: " + oVciException + " => " + oVciException.VciFormatError());
                } else {
                    System.err.println("Get CAN Line Status, Exception: " + oException);
                }
            }
        }
    }

    @Override
    public IxxatCanMessageReader getReader(int socketNo) {
        return this.reader[socketNo];
    }

    @Override
    public IxxatCanMessageWriter getWriter(int socketNo) {
        return this.writer[socketNo];
    }

    /*
     @Override
     public int getTSCDivisor(){
     return this.tscDiv;
     }
    
     @Override
     public int getDTXDivisor(){
     return this.dtxDiv;
     }
    
     @Override
     public int getFrequency(){
     return this.freq;
     }
     */
}
