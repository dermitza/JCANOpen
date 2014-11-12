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
package ch.dermitza.jcanopen.can;

/**
 * 
 *
 * @author K. Dermitzakis
 * @version 0.05
 * @since   0.03
 */
public class CanMessage {
    
    private int id; // CAN-ID
    private byte[] data; // Data in the can message
    private double timestamp; // Timestamp in seconds
    private boolean extendedFormat; // Whether this frame is of extended format
    private boolean possibleOverrun; 
    private boolean rtr;
    private boolean selfReception;
    
    public CanMessage(){
        data = new byte[0];
        extendedFormat = false;
        possibleOverrun = false;
        rtr = false;
        selfReception = false;
    }
    
    
    public void setMessage(int id, byte[] data, double timestamp,
            boolean extendedFormat, boolean possibleOverrun, boolean rtr,
            boolean selfReception){
        this.id = id;
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
        this.timestamp = timestamp;
        this.extendedFormat = extendedFormat;
        this.possibleOverrun = possibleOverrun;
        this.rtr = rtr;
        this.selfReception = selfReception;
    }
    
    public void setID(int id){
        this.id = id;
    }
    
    public int getID(){
        return this.id;
    }
    
    public void setTimestamp(double timestamp){
        this.timestamp = timestamp;
    }
    
    public double getTimestamp(){
        return this.timestamp;
    }
    
    public void setData(byte[] data){
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }
    
    public byte[] getData(){
        return this.data;
    }
    
    public int getLength(){
        return data.length;
    }
    
    public void setExtendedFormat(boolean extendedFormat){
        this.extendedFormat = extendedFormat;
    }
    
    public boolean isExtendedFormat(){
        return this.extendedFormat;
    }
    
    public void setPossibleOverrun(boolean possibleOverrun){
        this.possibleOverrun = possibleOverrun;
    }
    
    public boolean isPossibleOverrun(){
        return this.possibleOverrun;
    }
    
    public void setRTR(boolean rtr){
        this.rtr = rtr;
    }
    
    public boolean isRTR(){
        return this.rtr;
    }
    
    public void setSelfReception(boolean selfReception){
        this.selfReception = selfReception;
    }
    
    public boolean isSelfReception(){
        return this.selfReception;
    }
    
}
