//==============================================================================
//	
//	Copyright (c) 2013-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford)
//	* Mateusz Ujma <mateusz.ujma@cs.ox.ac.uk> (University of Oxford)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package heuristics.nextstate;

import java.util.Random;

import heuristics.nextstate.HeuristicNextStateFactory.NextState;
import heuristics.update.StateUpdate;
import explicit.ModelExplorer;

import parser.State;
import prism.PrismException;


public abstract class HeuristicNextState implements Cloneable{

	protected ModelExplorer pme;
	protected StateUpdate stateUpdate;
	
	public HeuristicNextState(ModelExplorer pme, StateUpdate stateUpdate) throws PrismException{
		this.pme = pme;
		this.stateUpdate = stateUpdate;
	}
	
	public abstract State sample(State s, int action, int depth) throws PrismException;
	
	public abstract State sample(State s, int action) throws PrismException;
	
	public abstract NextState getType();
	
	protected State sampleFromDist(int action, double[] dist) throws PrismException {
		for(int i=0;i<dist.length;i++) {
			if(i == 0) {
				dist[i] = Math.ceil(dist[i]*100);	
			} else {
				dist[i] =  Math.ceil(dist[i-1] + dist[i]*100);
			}
		}
		Random r = new Random();
		int max = (int)Math.ceil(dist[dist.length-1]);
		if(max == 0) {
			return pme.computeTransitionTarget(action, r.nextInt(dist.length));
		}
		int rand = r.nextInt(max)+1;
		for(int i=0;i<dist.length;i++) {
			if(rand <= dist[i]) {
				return pme.computeTransitionTarget(action, i);
			}
		}
		throw new PrismException("Not sampling any state, this should never happen");
	}
	
}
