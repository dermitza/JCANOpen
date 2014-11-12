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
package ch.dermitza.jcanopen.canopen.io;

import ch.dermitza.jcanopen.can.CanReaderIF;
import ch.dermitza.jcanopen.can.CanWriterIF;

/**
 * 
 *
 * @author K. Dermitzakis
 * @version 0.05
 * @since   0.03
 */
public class PDOReceiverExample extends AbstractPDOReceiver{
    
    protected CanWriterIF writer;
    
    public PDOReceiverExample(CanReaderIF reader){
        super(reader);
    }
    
    public void setWriter(CanWriterIF writer){
        this.writer = writer;
    }

    @Override
    protected void handleCanMessage() {
        // Here you can handle incoming PDO and/or error messages
    }

    @Override
    public void enableAll(boolean enable) {
        // Here you can enable/disable PDO logging on all controllers
    }

    @Override
    public void enable(int id, boolean enable) {
        // Here you can enable/disable PDO logging to the controller of your
        // choice
    }
    
}
