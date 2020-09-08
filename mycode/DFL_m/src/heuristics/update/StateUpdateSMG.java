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

package heuristics.update;

import heuristics.search.HeuristicRTDP_Adj;
import heuristics.search.StateValue;

import explicit.ModelExplorer;
import explicit.SMG;
import explicit.STPG;
import explicit.STPGExplicit;
import parser.State;
import prism.PrismException;

import java.lang.reflect.Array;
import java.util.*;


public class StateUpdateSMG extends StateUpdate{
	
	public StateUpdateSMG(ModelExplorer me, StateValueContainer container, int bound, boolean min, double epsilon) throws PrismException {
		super(me, container, bound, min, epsilon);
	}

	public int update(State s, int depth) throws PrismException{
		pme.queryState(s);
		int choices = pme.getNumChoices();
		StateValue lowerBound_q_s[] = new StateValue[choices];
		StateValue upperBound_q_s[] = new StateValue[choices];
		double lowerBoundValue = 0;
		double upperBoundValue = 0;
		if(getPlayer(s) == 1) {
			lowerBoundValue = 1;
			upperBoundValue = 1;
		} else {
			lowerBoundValue = 0;
			upperBoundValue = 0;
		}
		int bestAction = 0;
		for(int i=0;i<choices;i++) {
			pme.queryState(s);
			if(getPlayer(s) == 1) {
				lowerBound_q_s[i] = getLowerBoundActionValue(s, i, depth+1);
				upperBound_q_s[i] = getUpperBoundActionValue(s, i, depth+1);
				if(lowerBound_q_s[i].getLowerBound() < lowerBoundValue) {
					bestAction = i;
					lowerBoundValue = lowerBound_q_s[i].getLowerBound();
					upperBoundValue = upperBound_q_s[i].getUpperBound();
				}
			} else {
				lowerBound_q_s[i] = getLowerBoundActionValue(s, i, depth+1);
				upperBound_q_s[i] = getUpperBoundActionValue(s, i, depth+1);
				if(upperBound_q_s[i].getUpperBound() > upperBoundValue) {
					bestAction = i;
					lowerBoundValue = lowerBound_q_s[i].getLowerBound();
					upperBoundValue = upperBound_q_s[i].getUpperBound();
				}
			}
		}
		setQValue(s,new StateValue(lowerBoundValue, upperBoundValue), depth);
		return bestAction;
	}


	//changed on 28.05.17: We are maximizing the upper bound, not minimizing the lower one. This seems to do so stupid things...
	//Things TODO: Add min as bool, so we know, whether we are maxing or mining
	public int update(State s) throws PrismException{
		ArrayList<Integer> bestActions = getBestActions(s);
		//pick a random action from the set of best actions
		int bestAction = bestActions.get(new Random().nextInt(bestActions.size()));
//		if(getPlayer(s) == 1) {
//			pme.queryState(s);
//			boolean isLoop;
//			try{
//				isLoop = s.equals(pme.computeTransitionTarget(bestAction));
//			}
//			catch (PrismException e){
//				return bestAction; //more than one successor, no loop
//			}
//			while (isLoop){
//				bestActions.remove((Integer)bestAction);
//				if (bestActions.size()==0){
//					return 0;//maximizer state, only self loops
//				}
//				bestAction = bestActions.get(new Random().nextInt(bestActions.size()));
//				try{
//					isLoop = s.equals(pme.computeTransitionTarget(bestAction));
//				}
//				catch (PrismException e){
//					return bestAction; //more than one successor, no loop
//				}
//			}
//			return bestAction;//maximizer state, no self loop
//		}
		return bestAction; //minimizer state, no optimization needed
	}

	//TODO: This is very slow. Improve that.
	public int update(State s, HashMap<State, BitSet> state2leavingActions) throws PrismException {
		//get best actions
		ArrayList<Integer> bestActions = getBestActions(s);

		//get a list for the best actions which are also leaving
		ArrayList<Integer> bestLeavingActions = new ArrayList<Integer>();

		if (state2leavingActions.containsKey(s)) { // && state2leavingActions.get(s).size()>0) { //if there is an action leaving the MEC; containsKey ensures that we dont get a null pointer exception when questioning the hashset; also ensures there is at least one leaving action, else the set would not have been initilialize
			for (int i = 0; i < bestActions.size(); i++) {
				if (state2leavingActions.containsKey(s) && state2leavingActions.get(s).get(bestActions.get(i))) {
					bestLeavingActions.add(i);
				}
			}
			if (bestLeavingActions.size() == 0) {
				//It can happen, that the none of the best actions are leaving ones (in that case we have to adjust)
				//HeuristicRTDP_Adj.noticeProblem();
				return bestActions.get(new Random().nextInt(bestActions.size()));
			}
			//pick a random action from the set of remaining best actions (those which are leaving)
			return bestLeavingActions.get(new Random().nextInt(bestLeavingActions.size()));
		}

		//if state is not in the map, then we dont know which actions to prefer
		return bestActions.get(new Random().nextInt(bestActions.size()));
	}
	
	public StateUpdate clone() {
		try {
			return new StateUpdateSMG(null,null, bound, min, epsilon);
		} catch (PrismException e) {
			throw new RuntimeException(e);
		}
	}

	public ArrayList<Integer> getBestActions(State s) throws PrismException{
		pme.queryState(s);
		int choices = pme.getNumChoices();
		ArrayList<Integer> bestActions = new ArrayList<Integer>();
		double currentLowerBound, currentUpperBound;
		double lowerBoundValue;
		double upperBoundValue;
		if(getPlayer(s) == 1) {
			lowerBoundValue = 0;
			upperBoundValue = 0;
		} else {
			lowerBoundValue = 1;
			upperBoundValue = 1;
		}
		for(int i=0;i<choices;i++) {
			pme.queryState(s);
			currentLowerBound = getLowerBoundActionValue(s, i).getLowerBound();
			currentUpperBound = getUpperBoundActionValue(s, i).getUpperBound();
			if(getPlayer(s) == 1) {
				if (currentLowerBound>lowerBoundValue){
					lowerBoundValue = currentLowerBound; //we always remember the maximal lower bound possible
				}
				if(currentUpperBound - upperBoundValue>epsilon) { //The current action is better by more than epsilon
					//new best Action, remove all others we had until now
					bestActions.clear();
					bestActions.add(i);
					upperBoundValue = currentUpperBound;
				}
				else if (Math.abs(currentUpperBound - upperBoundValue)<epsilon){ //The current action is epsilon close to the one that was best before
					//another best Action, add it to the set
					bestActions.add(i);
					//We want to stay epsilon close to the best action, so we need to remember the highest upperBound in bestActions, and we need to check that nothing forbidden is in bestActions
					if(currentUpperBound > upperBoundValue){
						upperBoundValue = currentUpperBound;
						lowerBoundValue = currentLowerBound; //we remember the lower bound of the action with the maximal U. This is definitely correct.
						//The new action is greater than the old; therefor the epsilon area of the best action shifts, and we have to check, whether we have to remove something from bestActions
						for (int j=bestActions.size()-1; j>=0; j--){ //from back to front so we can remove without problems
							if(currentUpperBound - getUpperBoundActionValue(s, bestActions.get(j)).getUpperBound() > epsilon){
								bestActions.remove(j);
							}
						}
					}
				}
			} else {
				if (currentUpperBound<upperBoundValue){
					upperBoundValue = currentUpperBound; //we always remember the minimal upper bound possible
				}
				if(lowerBoundValue - currentLowerBound > epsilon) { //the current action has a value that is smaller by more than epsilon.
					bestActions.clear();
					bestActions.add(i);
					lowerBoundValue = currentLowerBound;
				}
				else if (Math.abs(currentLowerBound - lowerBoundValue)<epsilon){//the current action is epsilon close to the best one
					//another best Action, add it to the set
					bestActions.add(i);
					//We want to stay epsilon close to the best action, so we need to remember the lowest Lower Bound in bestActions, and we need to check that nothing forbidden is in bestActions
					if(lowerBoundValue > currentLowerBound){
						lowerBoundValue = currentLowerBound;
						//The new action is smaller than the old; therefor the epsilon area of the best action shifts, and we have to check, whether we have to remove something from bestActions
						for (int j=bestActions.size()-1; j>=0; j--){ //from back to front so we can remove without problems
							if(getLowerBoundActionValue(s, bestActions.get(j)).getLowerBound() - currentLowerBound > epsilon){
								bestActions.remove(j);
							}
						}
					}

				}
			}
		}
		setQValue(s,new StateValue(lowerBoundValue, upperBoundValue));

		if(bestActions.isEmpty()){System.err.println("Empty set of best actions in StateUpdateSMG. This can never happen.");System.exit(-1);}

		return bestActions;
	}




}
