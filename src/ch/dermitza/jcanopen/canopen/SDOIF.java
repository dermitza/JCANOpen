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

import de.ixxat.vci3.bal.can.CanMessage;

/**
 * 
 *
 * @author K. Dermitzakis
 * @version 0.05
 * @since   0.03
 */
public interface SDOIF {
    
    public static final short SDO_RESPONSE_ERROR = 0x80;
    public static final short SDO_RESPONSE_WRITE_OK = 0x60;
    public static final short SDO_RESPONSE_READ_OK = 0x40;
    
    public static final short SDO_TX = 0x580; /* + node id */
    public static final short SDO_RX = 0x600; /* + node id */
    
    /**
    * Sends an SDO_data stuct on the can-bus.
    * \return  0 on success, -1 on error, -2 on timeout
    **/
    public int transmitSDO(SDOFrame frame, boolean write);
    
    /**
    * Sends an SDO acknowledgement package in return to frame message
    * \return 0 on success
    */
    public int acknowledgeSDO(CanMessage message);
}
