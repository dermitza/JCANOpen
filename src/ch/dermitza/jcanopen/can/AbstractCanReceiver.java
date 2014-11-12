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

import ch.dermitza.jcanopen.canopen.async.CanDataListener;
import ch.dermitza.jcanopen.canopen.async.DataListenerIF;

/**
 * 
 *
 * @author K. Dermitzakis
 * @version 0.05
 * @since   0.03
 * @param <T>
 */
public abstract class AbstractCanReceiver<T extends CanDataListener> implements Runnable, DataListenerIF{

    private CanReaderIF reader;
    protected final CanMessage msg;
    private boolean running = false;
    private boolean sleepWait = true;
    private long sleepMs = 0;
    private int sleepNs = 1;
    private int retryCount;

    public AbstractCanReceiver(CanReaderIF reader) {
        msg = new CanMessage();
        this.reader = reader;
    }

    public void setReader(CanReaderIF reader) {
        this.reader = reader;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
    
    public void shouldSleepWait(boolean sleepWait){
        this.sleepWait = sleepWait;
    }
    
    public void setSleepWaitTime(long sleepMs, int sleepNs){
        this.sleepMs = sleepMs;
        this.sleepNs = sleepNs;
    }
    
    protected abstract void handleCanMessage();

    @Override
    public void run() {
        if (reader != null) {
            running = true;
        } else {
            System.err.println("CAN reader and/or writer are null, aborting.");
        }

        retryCount = 0;
        boolean haveMsg;

        runLoop:
        while (running) {
            // Lock the reader while doing a read. This is useful in terms
            // of CanOpen SDO communication, where the SDO transceiver
            // should acquire a particular response to a transmitted message.
            synchronized (reader) {
                haveMsg = reader.readMessage(msg);
            }
            if (haveMsg) {
                handleCanMessage();
            } else {
                retryCount++;
                if(sleepWait){
                    try {
                        Thread.sleep(sleepMs, sleepNs);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
        shutdown();
    }
    
    /**
     * This method can be called from an SDO transceiver that has the read lock
     * and has received a message that is not a response to its current SDO.
     * Such a generic message should be handled from the CanReceiver
     * @param message 
     */
    public void handleExternalMessage(CanMessage message){
        msg.setMessage(message.getID(), message.getData(),
                message.getTimestamp(), message.isExtendedFormat(),
                message.isPossibleOverrun(), message.isRTR(),
                message.isSelfReception());
        handleCanMessage();
    }

    protected void shutdown() {
        // Shutdown the reader
        reader = null;
    }
}
