/* $RCSfile$    
 * $Author$    
 * $Date$    
 * $Revision$
 *
 * Copyright (C) 1997-2006  The Chemistry Development Kit (CDK) project
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.openscience.cdk.interfaces.IChemObjectBuilder;

/**
 * Interface that allows reading atom type configuration data from some
 * source.
 *
 * @cdk.module core
 */
public interface IAtomTypeConfigurator {

    /**
     * Sets the file containing the config data.
     *
     * @param ins InputStream from which the atom type definitions are to be read
     */
    public void setInputStream(InputStream ins);

    /**
     * Reads a set of configured AtomType's into a Vector.
     *
     * @param builder ChemObjectBuilder used to instantiate the AtomType's.
     *
     * @return A Vector containing the AtomTypes extracted from the InputStream
     * @throws IOException when something went wrong with reading the data
     */
    public Vector readAtomTypes(IChemObjectBuilder builder) throws IOException;

}
