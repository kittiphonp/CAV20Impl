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

package heuristics.search;

import heuristics.HeuristicsSMGModelChecker;
import heuristics.nextstate.HeuristicNextState;
import heuristics.update.StateUpdate;
import explicit.ModelExplorer;
import prism.PrismException;

public class HeuristicFactory
{
	public enum HeuristicType {
		RTDP, LRTDP, RTDP_PARALLEL, LRTDP_PARALLEL, ILAO, RTDP_UNBOUNDED, RTDP_ADJ
	}
	
	public static Heuristic createHeuristic(HeuristicType type, HeuristicsSMGModelChecker mc, StateUpdate su, HeuristicNextState nextState, ModelExplorer pme, boolean min) throws PrismException{
		switch(type) {
			case RTDP:
				return new HeuristicRTDP(mc, su, nextState, pme, min);
			case LRTDP:
				return new HeuristicLRTDP(mc, su, nextState, pme, min);
			case RTDP_PARALLEL:
				return new HeuristicRTDPParallel(mc, su, nextState, pme, min);
			case LRTDP_PARALLEL:
				return new HeuristicLRTDPParallel(mc, su, nextState, pme, min);
			case ILAO:
				return new HeuristicILAO(mc, su, nextState, pme, min);
			//case RTDP_UNBOUNDED:
			//	return new HeuristicRTDP_Unbounded(mc, su, nextState, pme, min);
			case RTDP_ADJ: //The new heuristic based not on collapsing, but on adjusting the bounds
				return new HeuristicRTDP_Adj(mc, su, nextState, pme, min);
		}
		throw new PrismException("Unimplemented heuristic: " + type);
	}
	
}
