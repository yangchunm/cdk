/*
 *  $RCSfile$
 *  $Author$
 *  $Date$
 *  $Revision$
 *
 *  Copyright (C) 1997-2006  The Chemistry Development Kit (CDK) project
 *
 *  Contact: cdk-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All I ask is that proper credit is given for my work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package org.openscience.cdk.controller;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.ISetOfReactions;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.cdk.tools.manipulator.ReactionManipulator;

/**
 *  Class that acts on MouseEvents and KeyEvents.
 *
 *@author         steinbeck
 *@author         egonw
 *@cdk.created    2. Mai 2005
 *@cdk.keyword    mouse events
 *@cdk.require    java1.4+
 */
public class Controller2D extends SimpleController2D
{
	
	/**
	 *  Constructs a controller that performs operations on the AtomContainer when
	 *  actions are detected from the MouseEvents.
	 *
	 *@param  chemModel  Description of the Parameter
	 *@param  r2dm       Description of the Parameter
	 *@param  c2dm       Description of the Parameter
	 */
	public Controller2D(IChemModel chemModel, Renderer2DModel r2dm, Controller2DModel c2dm)
	{
		super(r2dm, c2dm);
		this.chemModel = chemModel;
	}

	/**
	 *  Sets the chemModel attribute of the Controller2D object
	 *
	 *@param  chemModel  The new chemModel value
	 */
	public void setChemModel(IChemModel chemModel)
	{
		this.chemModel = chemModel;
	}
	
	/**
	 *  Constructor for the Controller2D object
	 *
	 *@param  chemModel  Description of the Parameter
	 *@param  r2dm       Description of the Parameter
	 */
	public Controller2D(IChemModel chemModel, Renderer2DModel r2dm)
	{
		super(r2dm, new Controller2DModel());
		this.chemModel = chemModel;
	}

	IAtomContainer getRelevantAtomContainer(IChemModel chemModel, IAtom atom)
	{
		return ChemModelManipulator.getRelevantAtomContainer(chemModel, atom);
	}

	IAtomContainer getAllInOneContainer(IChemModel chemModel)
	{
		return ChemModelManipulator.getAllInOneContainer(chemModel);	
	}

	/**
	 *  Returns a Reaction if the coordinate is within the reaction 'window'.
	 *
	 *@param  X  The x world coordinate of the point
	 *@param  Y  The y world coordinate of the point
	 *@return    A Reaction if it is in a certain range of the given point
	 */
	IReaction getReactionInRange(int X, int Y) {
		ISetOfReactions reactionSet = chemModel.getSetOfReactions();
		if (reactionSet != null)
		{
			// process reaction by reaction
			IReaction[] reactions = reactionSet.getReactions();
			for (int i = 0; i < reactions.length; i++)
			{
				IAtomContainer atomContainer = ReactionManipulator.getAllInOneContainer(reactions[i]);
				double[] minmax = GeometryTools.getMinMax(atomContainer);
				if ((X <= minmax[2]) && (X >= minmax[0]) &&
						(Y <= minmax[3]) && (Y >= minmax[1]))
				{
					// cursor in in reaction bounding box
					return reactions[i];
				}
			}
		}
		return null;
	}
	
	IReaction getRelevantReaction(IChemModel chemModel, IAtom atom)
	{
		return ChemModelManipulator.getRelevantReaction(chemModel, atom);
	}

	

}

