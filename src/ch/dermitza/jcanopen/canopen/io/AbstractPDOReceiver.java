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

import ch.dermitza.jcanopen.can.AbstractCanReceiver;
import ch.dermitza.jcanopen.can.CanReaderIF;
import ch.dermitza.jcanopen.canopen.async.CanDataListener;
import ch.dermitza.jcanopen.canopen.async.DataListenerIF;
import java.util.ArrayList;

/**
 * 
 *
 * @author K. Dermitzakis
 * @version 0.05
 * @since   0.03
 */
public abstract class AbstractPDOReceiver extends AbstractCanReceiver implements DataListenerIF{
    
    protected ArrayList<CanDataListener> listeners;
    
    public AbstractPDOReceiver(CanReaderIF reader) {
        super(reader);
        listeners = new ArrayList<>();
    }
    
    protected void removeAllListeners(){
        listeners.clear();
    }
    
    @Override
    public void addDataListener(CanDataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeDataListener(CanDataListener listener) {
        listeners.remove(listener);
    }
    
    
    
    public abstract void enableAll(boolean enable);
    public abstract void enable(int id, boolean enable);
}
