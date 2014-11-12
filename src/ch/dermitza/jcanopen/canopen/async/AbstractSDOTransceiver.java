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
package ch.dermitza.jcanopen.canopen.async;

import ch.dermitza.jcanopen.can.CanMessage;
import ch.dermitza.jcanopen.can.CanReaderIF;
import ch.dermitza.jcanopen.can.CanWriterIF;
import ch.dermitza.jcanopen.canopen.SDOFrame;
import ch.dermitza.jcanopen.canopen.io.AbstractPDOReceiver;

/**
 * 
 *
 * @author K. Dermitzakis
 * @version 0.05
 * @since   0.03
 */
public abstract class AbstractSDOTransceiver<T extends AbstractPDOReceiver> {

    protected T receiver;
    protected CanWriterIF writer;
    protected CanReaderIF reader;
    protected final CanMessage rsp;
    protected Thread receiverThread;
    private int retryCount = 0;
    

    public AbstractSDOTransceiver() {
        rsp = new CanMessage();
    }
    
    /* NEEDS TO BE OVERRRIDEN WITH PDO IMPLEMENTATION */
    public abstract void setReader(CanReaderIF reader);
    
    /* NEEDS TO BE OVERRRIDEN WITH PDO IMPLEMENTATION */
    public abstract void setWriter(CanWriterIF writer);

    public int transmitSDO(SDOFrame frame) {
        retryCount = 0;
        if (writer.writeMessageImmediate(frame.getCANMessage())) {
            synchronized(reader){
                while(true){
                    if(reader.readMessage(rsp)){
                        byte[] msg = rsp.getData();
                        // Make sure the response is to our message
                        if(rsp.getID() == frame.cob_r
                                && msg[1] == frame.lsb
                                && msg[2] == frame.msb
                                && msg[3] == frame.subIndex){
                            //System.out.println("SDO msg");
                            // This is a response to our SDO Frame
                            int data = (msg[4] & 0xFF)
                                    | ((msg[5] & 0xFF) << 8)
                                    | ((msg[6] & 0xFF) << 16)
                                    | ((msg[7] & 0xFF) << 24);
                            return data;
                        } else {
                            //System.out.println("SDO: Received non-response");
                            // Potentially a response to a different message
                            // (unlikely) or a PDO msg. Send it to the receiver
                            // for handling
                            receiver.handleExternalMessage(rsp);
                        }
                    }
                    //System.out.println(retryCount);
                    retryCount++;
                    //LockSupport.parkNanos(1000);
                    //try{
                    //    Thread.sleep(1);
                    //}catch(InterruptedException ie){

                    //}
                }
            }
        }
        // It we arrive here we failed to write the message, there is something
        // wrong with the CAN bus.
        return -1;
    }
    
    public void shutdown(){
        receiver.setRunning(false);
    }
}
