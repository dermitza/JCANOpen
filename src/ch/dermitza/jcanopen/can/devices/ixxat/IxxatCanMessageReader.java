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
import ch.dermitza.jcanopen.can.CanReaderIF;
import de.ixxat.vci3.bal.can.ICanMessageReader;

/**
 * 
 *
 * @author K. Dermitzakis
 * @version 0.05
 * @since   0.03
 */
public class IxxatCanMessageReader implements CanReaderIF{
    
    private ICanMessageReader reader;
    private final de.ixxat.vci3.bal.can.CanMessage msg;
    private final double freq;
    private final double tscDiv; // Timestamp clock divisor
    
    public IxxatCanMessageReader(ICanMessageReader reader, double freq, double tscDiv){
        this.reader = reader;
        this.freq = freq;
        this.tscDiv = tscDiv;
        msg = new de.ixxat.vci3.bal.can.CanMessage();
    }
    
    /**
     * 
     * @param message
     * @return true if a message was read or false otherwise.
     */
    @Override
    public boolean readMessage(CanMessage message){
        try{
            reader.ReadMessage(msg);
            
            // If this stage is passed, we read a message, so format it to
            // our convention and respond with true;
            message.setMessage(msg.m_dwIdentifier,
                    msg.m_abData,
                    ((double)msg.m_dwTimestamp)*tscDiv/freq,
                    msg.m_fExtendedFrameFormat,
                    msg.m_fPossibleOverrun,
                    msg.m_fRemoteTransmissionRequest,
                    msg.m_fSelfReception);
            return true;
        } catch(Throwable t){
            // do nothing
            return false;
        }
    }
    
    public void dispose(){
        reader = null;
    }
}
