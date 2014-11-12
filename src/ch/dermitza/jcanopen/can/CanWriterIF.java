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
public interface CanWriterIF {
    
    public boolean writeMessage(CanMessage message, int delayInTicks);
    public boolean writeMessageImmediate(CanMessage message);
    
}
