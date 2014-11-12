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
package ch.dermitza.jcanopen.canopen;

import ch.dermitza.jcanopen.can.CanMessage;

/**
 * 
 *
 * @author K. Dermitzakis
 * @version 0.05
 * @since   0.03
 */
public class SDOFrame {

    public short nodeID; // Node id to send data to
    public short index; // Index in Object dictionary
    public byte subIndex; // Subindex in Object dictionary
    public byte[] data; // Data to transmit
    public boolean write = false;
    public short rspID;
    public short rspIdx;
    public byte rspSubIdx;
    public byte[] rspData;
    public short cob, cob_r;
    public byte ccd, msb, lsb;
    
    public SDOFrame(short nodeID, boolean write, short idx, byte subIdx, int data){
        setHeader(nodeID, write, idx, subIdx);
        setData(data);
    }
    
    public SDOFrame(short nodeID, boolean write, short idx, byte subIdx, short data){
        setHeader(nodeID, write, idx, subIdx);
        setData(data);
    }
    
    public SDOFrame(short nodeID, boolean write, short idx, byte subIdx, byte data){
        setHeader(nodeID, write, idx, subIdx);
        setData(data);
    }
    
    public SDOFrame(short nodeID, boolean write, short idx, byte subIdx){
        setHeader(nodeID, write, idx, subIdx);
        setNoData();
    }
    
    private void setHeader(short nodeID, boolean write, short idx, byte subIdx){
        this.nodeID = nodeID;
        this.write = write;
        this.index = idx;
        this.subIndex = subIdx;
        this.data = new byte[8];
        cob = (short) (SDOIF.SDO_RX + nodeID);
        cob_r = (short) (SDOIF.SDO_TX + nodeID);
        msb = (byte) ((index >> 8) & 0xFF);
        lsb = (byte) (index & 0xFF);
        this.data[1] = lsb;
        this.data[2] = msb;
        this.data[3] = subIndex;
    }

    public void fromData(byte[] data) {
        System.arraycopy(data, 0, this.data, 0, 8);
        ccd = data[0];
        index = (short) (((data[2] << 8) & 0xFF) | data[1] & 0xFF);
        subIndex = data[3];
    }

    public void setData(int data) {
        ccd = calculateCCD(4, write);
        
        this.data[0] = ccd;
        this.data[4] = (byte) (data & 0xFF);
        this.data[5] = (byte) ((data >> 8) & 0xFF);
        this.data[6] = (byte) ((data >> 16) & 0xFF);
        this.data[7] = (byte) ((data >> 24) & 0xFF);
    }

    public void setData(short data) {
        ccd = calculateCCD(2, write);
        
        this.data[0] = ccd;
        this.data[4] = (byte) (data & 0xFF);
        this.data[5] = (byte) ((data >> 8) & 0xFF);
        this.data[6] = 0x00;
        this.data[7] = 0x00;
    }

    public void setData(byte data) {
        ccd = calculateCCD(1, write);
        
        this.data[0] = ccd;
        this.data[4] = (byte) (data & 0xFF);
        this.data[5] = 0x00;
        this.data[6] = 0x00;
        this.data[7] = 0x00;
    }

    public void setNoData() {
        ccd = calculateCCD(0, write);
        
        this.data[0] = ccd;
        this.data[4] = 0x00;
        this.data[5] = 0x00;
        this.data[6] = 0x00;
        this.data[7] = 0x00;
    }

    /*
    public void setData(byte[] data) {
        ccd = calculateCCD(data.length, write);

        int fillerBytes = 8 - 4 - data.length;


        this.data[0] = ccd;
        this.data[1] = lsb;
        this.data[2] = msb;
        this.data[3] = subIndex;
        for (int i = 0; i < data.length; i++) {
            this.data[4 + i] = data[i];
        }
        for (int i = 0; i < fillerBytes; i++) {
            this.data[4 + data.length + i] = 0x00;
        }
    }
    */

    /*
    public byte[] getData() {
        byte[] dat = new byte[getDataLength()];
        if (dat.length == 0) {
            return dat;
        }

        for (int i = 0; i < dat.length; i++) {
            dat[i] = data[4 + i];
        }

        return dat;
    }
    */
    
    public CanMessage getCANMessage(){
        CanMessage msg = new CanMessage();
        msg.setMessage(cob, data, 0, false, false, false, false);
        return msg;
    }

    /*
    public CanMessage getCANMessage() {
        CanMessage msg = new CanMessage();
        msg.m_dwIdentifier = cob;
        msg.m_abData = new byte[8];
        msg.m_abData[0] = data[0];
        msg.m_abData[1] = data[1];
        msg.m_abData[2] = data[2];
        msg.m_abData[3] = data[3];
        msg.m_abData[4] = data[4];
        msg.m_abData[5] = data[5];
        msg.m_abData[6] = data[6];
        msg.m_abData[7] = data[7];
        //msg.m_fRemoteTransmissionRequest = true;
        msg.m_bDataLength = 8;
        msg.m_dwTimestamp = 0; // No Delay 
        msg.m_fExtendedFrameFormat = false;
        //msg.m_fSelfReception              = false;

        return msg;
    }
    */

    private int getDataLength() {
        byte base = (write) ? (byte) (data[0] - 0x20) : (byte) (data[0] - 0x40);
        switch (base) {
            case 0:
                return 0;
            case 0x0F:
                return 1;
            case 0x0B:
                return 2;
            case 0x07:
                return 3;
            case 0x03:
                return 4;
        }
        return -1;
    }

    /**
     * Automatic calculation of ccd-field for requests *
     */
    private static byte calculateCCD(int size, boolean write) {
        byte base = (write) ? (byte) 0x20 : 0x40;

        switch (size) {
            case 0:
                return base; /* No data */
            case 1:
                return (byte) (base + 0x0F); /* 1 byte of used data */
            case 2:
                return (byte) (base + 0x0B); /* 2 bytes of used data */
            case 3:
                return (byte) (base + 0x07); /* 3 bytes of used data */
            case 4:
                return (byte) (base + 0x03); /* 4 bytes of used data */
        }
        return 0;
    }
}
