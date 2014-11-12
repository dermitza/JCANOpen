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

import ch.dermitza.jcanopen.can.CanMessage;
import ch.dermitza.jcanopen.can.CanWriterIF;
import de.ixxat.vci3.bal.can.ICanMessageWriter;

/**
 * 
 *
 * @author K. Dermitzakis
 * @version 0.05
 * @since   0.03
 */
public class IxxatCanMessageWriter implements CanWriterIF{
    
    private ICanMessageWriter writer;
    private final de.ixxat.vci3.bal.can.CanMessage msg;
    private final int div; // Delay divisor
    private final int freq; // Clock frequency
    
    public IxxatCanMessageWriter(ICanMessageWriter writer, int div, int freq){
        this.writer = writer;
        this.div = div;
        this.freq = freq;
        msg = new de.ixxat.vci3.bal.can.CanMessage();
    }

    // TODO: Change the delay to use a time value rather than ticks
    @Override
    public boolean writeMessage(CanMessage message, int delayInTicks) {
        
        // First format the message to Ixxat standard
        msg.m_dwIdentifier = message.getID();
        msg.m_abData = new byte[message.getLength()];
        System.arraycopy(message.getData(), 0, msg.m_abData, 0, message.getLength());
        msg.m_dwTimestamp = delayInTicks;
        msg.m_bDataLength = (byte)message.getLength();
        msg.m_fExtendedFrameFormat = message.isExtendedFormat();
        msg.m_fPossibleOverrun = message.isPossibleOverrun();
        msg.m_fRemoteTransmissionRequest = message.isRTR();
        msg.m_fSelfReception = message.isSelfReception();
        
        try{
            writer.WriteMessage(msg);
            // Message written successfully
            return true;
        }catch(Throwable t){
            return false;
        }
    }

    @Override
    public boolean writeMessageImmediate(CanMessage message) {
        return this.writeMessage(message, 0);
    }
    
    public void dispose(){
        writer = null;
    }
    
}
