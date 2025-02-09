//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <d.a.parker@cs.bham.ac.uk> (University of Birmingham/Oxford)
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

package parser.visitor;

import java.util.Vector;

import parser.ast.*;
import parser.ast.Module;
import parser.type.*;
import prism.ModelType;
import prism.PrismLangException;

/**
 * Perform any required semantic checks. Optionally pass in parent ModulesFile
 * and PropertiesFile for some additional checks (or leave null);
 * These checks are done *before* any undefined constants have been defined.
 */
public class SemanticCheck extends ASTTraverse
{
	private ModulesFile modulesFile;
	private PropertiesFile propertiesFile;
	// Sometimes we need to keep track of parent (ancestor) objects
	private ModulesFile inModulesFile = null;
	private Module inModule = null;
	private Expression inInvariant = null;
	private Expression inGuard = null;
	private Update inUpdate = null;

	public SemanticCheck()
	{
		this(null, null);
	}

	public SemanticCheck(ModulesFile modulesFile)
	{
		this(modulesFile, null);
	}

	public SemanticCheck(ModulesFile modulesFile, PropertiesFile propertiesFile)
	{
		setModulesFile(modulesFile);
		setPropertiesFile(propertiesFile);
	}

	public void setModulesFile(ModulesFile modulesFile)
	{
		this.modulesFile = modulesFile;
	}

	public void setPropertiesFile(PropertiesFile propertiesFile)
	{
		this.propertiesFile = propertiesFile;
	}

	public void visitPre(ModulesFile e) throws PrismLangException
	{
		// Register the fact we are entering a model
		inModulesFile = e;
	}
	
	public void visitPost(ModulesFile e) throws PrismLangException
	{
		int i, j, n, n2;
		Module m;
		Vector<String> v;

		// Register the fact we are leaving a model
		inModulesFile = null;
		
		// Check for use of init...endinit _and_ var initial values
		if (e.getInitialStates() != null) {
			n = e.getNumGlobals();
			for (i = 0; i < n; i++) {
				if (e.getGlobal(i).isStartSpecified())
					throw new PrismLangException("Cannot use both \"init...endinit\" and initial values for variables", e.getGlobal(i).getStart());
			}
			n = e.getNumModules();
			for (i = 0; i < n; i++) {
				m = e.getModule(i);
				n2 = m.getNumDeclarations();
				for (j = 0; j < n2; j++) {
					if (m.getDeclaration(j).isStartSpecified())
						throw new PrismLangException("Cannot use both \"init...endinit\" and initial values for variables", m.getDeclaration(j).getStart());
				}
			}
		}

		// Check system...endsystem construct (if present)
		// Each module should appear exactly once
		if (e.getSystemDefn() != null) {
			e.getSystemDefn().getModules(v = new Vector<String>(), modulesFile);
			n = e.getNumModules();
			for (i = 0; i < n; i++) {
				int k = v.indexOf(e.getModuleName(i));
				if (v.indexOf(e.getModuleName(i), k + 1) != -1) {
					throw new PrismLangException("Module " + e.getModuleName(i) + " appears more than once in the \"system\" construct", e.getSystemDefn());
				}
			}
		}
	}

	public Object visit(SystemReference e) throws PrismLangException
	{
		// Make sure referenced system exists
		if (modulesFile.getSystemDefnByName(e.getName()) == null)
			throw new PrismLangException("Reference to system " + e.getName() + " which does not exist", e);
		return null;
	}
	
	public Object visit(FormulaList e) throws PrismLangException
	{
		// Override - don't need to do any semantic checks on formulas
		// (they will have been expanded in place, where needed)
		// (and we shouldn't check them - e.g. clock vars appearing in errors would show as an error)
		return null;
	}
	
	public void visitPost(LabelList e) throws PrismLangException
	{
		int i, n;
		String s;
		n = e.size();
		for (i = 0; i < n; i++) {
			s = e.getLabelName(i);
			if ("deadlock".equals(s))
				throw new PrismLangException("Cannot define a label called \"deadlock\" - this is a built-in label", e.getLabel(i));
			if ("init".equals(s))
				throw new PrismLangException("Cannot define a label called \"init\" - this is a built-in label", e.getLabel(i));
		}
	}

	public void visitPost(ConstantList e) throws PrismLangException
	{
		int i, n;
		n = e.size();
		for (i = 0; i < n; i++) {
			if (e.getConstant(i) != null && !e.getConstant(i).isConstant()) {
				throw new PrismLangException("Definition of constant \"" + e.getConstantName(i) + "\" is not constant", e.getConstant(i));
			}
		}
	}

	public void visitPost(Declaration e) throws PrismLangException
	{
		if (e.getStart() != null && !e.getStart().isConstant()) {
			throw new PrismLangException("Initial variable value of variable \"" + e.getName() + "\" is not constant", e.getStart());
		}
		// Clocks cannot be given initial variables
		// (Note: it is safe to use getType() here because the type of a Declaration
		// is set on construction, not during type checking).
		if (e.getStart() != null && e.getType() instanceof TypeClock) {
			throw new PrismLangException("Cannot specify initial value for a clock", e);
		}
	}

	public void visitPost(DeclarationInt e) throws PrismLangException
	{
		if (e.getLow() != null && !e.getLow().isConstant()) {
			throw new PrismLangException("Integer range lower bound \"" + e.getLow() + "\" is not constant", e.getLow());
		}
		if (e.getHigh() != null && !e.getHigh().isConstant()) {
			throw new PrismLangException("Integer range upper bound \"" + e.getLow() + "\" is not constant", e.getLow());
		}
	}

	public void visitPost(DeclarationArray e) throws PrismLangException
	{
		if (e.getLow() != null && !e.getLow().isConstant()) {
			throw new PrismLangException("Array lower bound \"" + e.getLow() + "\" is not constant", e.getLow());
		}
		if (e.getHigh() != null && !e.getHigh().isConstant()) {
			throw new PrismLangException("Array upper bound \"" + e.getLow() + "\" is not constant", e.getLow());
		}
	}

	public void visitPost(DeclarationClock e) throws PrismLangException
	{
		// Clocks are only allowed in PTA models
		if (modulesFile.getModelType() != ModelType.PTA) {
			throw new PrismLangException("Clock variables are only allowed in PTA models", e);
		}
	}

	public void visitPre(Module e) throws PrismLangException
	{
		// Register the fact we are entering a module
		inModule = e;
	}

	public Object visit(Module e) throws PrismLangException
	{
		// Override this so we can keep track of when we are in an invariant
		visitPre(e);
		int i, n;
		n = e.getNumDeclarations();
		for (i = 0; i < n; i++) {
			if (e.getDeclaration(i) != null) e.getDeclaration(i).accept(this);
		}
		inInvariant = e.getInvariant();
		if (e.getInvariant() != null)
			e.getInvariant().accept(this);
		inInvariant = null;
		n = e.getNumCommands();
		for (i = 0; i < n; i++) {
			if (e.getCommand(i) != null) e.getCommand(i).accept(this);
		}
		visitPost(e);
		return null;
	}

	public void visitPost(Module e) throws PrismLangException
	{
		// Register the fact we are leaving a module
		inModule = null;
	}

	public Object visit(Command e) throws PrismLangException
	{
		// Override this so we can keep track of when we are in a command
		visitPre(e);
		inGuard = e.getGuard();
		e.getGuard().accept(this);
		inGuard = null;
		e.getUpdates().accept(this);
		visitPost(e);
		return null;
	}
	
	public void visitPre(Update e) throws PrismLangException
	{
		// Register the fact we are entering an update
		inUpdate = e;
	}

	public void visitPost(Update e) throws PrismLangException
	{
		int i, n;
		String s, var;
		Command c;
		Module m;
		ModulesFile mf;
		boolean isLocal, isGlobal;

		// Register the fact we are leaving an update
		inUpdate = null;

		// Determine containing command/module/model
		// (mf should coincide with the stored modulesFile)
		c = e.getParent().getParent();
		m = c.getParent();
		mf = m.getParent();
		n = e.getNumElements();
		for (i = 0; i < n; i++) {
			// Check that the update is allowed to modify this variable
			var = e.getVar(i);
			isLocal = m.isLocalVariable(var);
			isGlobal = isLocal ? false : mf.isGlobalVariable(var);
			if (!isLocal && !isGlobal) {
				s = "Module \"" + m.getName() + "\" is not allowed to modify variable \"" + var + "\"";
				throw new PrismLangException(s, e.getVarIdent(i));
			}
			//TODO this should be dealt with so that the check is not commented out
			/*if (isGlobal && !c.getSynch().equals("")) {
				s = "Synchronous command cannot modify global variable";
				throw new PrismLangException(s, e.getVarIdent(i));
			}*/
		}
	}

	public void visitPost(SystemRename e) throws PrismLangException
	{
		int i, n;
		String s;
		Vector<String> v;

		// Check all actions are valid and ensure no duplicates
		// (only check "from": OK to introduce new actions and to map to same
		// action)
		v = new Vector<String>();
		n = e.getNumRenames();
		for (i = 0; i < n; i++) {
			s = e.getFrom(i);
			if (!modulesFile.isSynch(s)) {
				throw new PrismLangException("Invalid action \"" + s + "\" in \"system\" construct", e);
			}
			if (v.contains(s)) {
				throw new PrismLangException("Duplicated action \"" + s + "\" in parallel composition in \"system\" construct", e);
			} else {
				v.addElement(s);
			}
		}
	}

	public void visitPost(SystemHide e) throws PrismLangException
	{
		int i, n;
		String s;
		Vector<String> v;

		// Check all actions are valid and ensure no duplicates
		v = new Vector<String>();
		n = e.getNumActions();
		for (i = 0; i < n; i++) {
			s = e.getAction(i);
			if (!modulesFile.isSynch(s)) {
				throw new PrismLangException("Invalid action \"" + s + "\" in \"system\" construct", e);
			}
			if (v.contains(s)) {
				throw new PrismLangException("Duplicated action \"" + s + "\" in parallel composition in \"system\" construct", e);
			} else {
				v.addElement(s);
			}
		}
	}

	public void visitPost(SystemParallel e) throws PrismLangException
	{
		int i, n;
		String s;
		Vector<String> v;

		// Check all actions are valid and ensure no duplicates
		v = new Vector<String>();
		n = e.getNumActions();
		for (i = 0; i < n; i++) {
			s = e.getAction(i);
			if (!modulesFile.isSynch(s)) {
				throw new PrismLangException("Invalid action \"" + s + "\" in \"system\" construct", e);
			}
			if (v.contains(s)) {
				throw new PrismLangException("Duplicated action \"" + s + "\" in parallel composition in \"system\" construct", e);
			} else {
				v.addElement(s);
			}
		}
	}

	public void visitPost(ExpressionTemporal e) throws PrismLangException
	{
		int op = e.getOperator();
		Expression operand1 = e.getOperand1();
		Expression operand2 = e.getOperand2();
		Expression lBound = e.getLowerBound();
		Expression uBound = e.getUpperBound();
		if (lBound != null && !lBound.isConstant()) {
			throw new PrismLangException("Lower bound in " + e.getOperatorSymbol() + " operator is not constant", lBound);
		}
		if (uBound != null && !uBound.isConstant()) {
			throw new PrismLangException("Upper bound in " + e.getOperatorSymbol() + " operator is not constant", uBound);
		}
		// Other checks (which parser should never allow to occur anyway)
		if (op == ExpressionTemporal.P_X && (operand1 != null || operand2 == null || lBound != null || uBound != null)) {
			throw new PrismLangException("Cannot attach bounds to " + e.getOperatorSymbol() + " operator", e);
		}
		if (op == ExpressionTemporal.R_C && (operand1 != null || operand2 != null || lBound != null)) {
			// NB: upper bound is optional (e.g. multi-objective allows R...[C] operator)
			throw new PrismLangException("Badly formed " + e.getOperatorSymbol() + " operator", e);
		}
		if (op == ExpressionTemporal.R_I && (operand1 != null || operand2 != null || lBound != null || uBound == null)) {
			throw new PrismLangException("Badly formed " + e.getOperatorSymbol() + " operator", e);
		}
		if (op == ExpressionTemporal.R_S && (operand1 != null || operand2 != null || lBound != null || uBound != null)) {
			throw new PrismLangException("Badly formed " + e.getOperatorSymbol() + " operator", e);
		}
	}

	public void visitPost(ExpressionFunc e) throws PrismLangException
	{
		// Check function name is valid
		if (e.getNameCode() == -1) {
			throw new PrismLangException("Unknown function \"" + e.getName() + "\"", e);
		}
		// Check num arguments
		if (e.getNumOperands() < e.getMinArity()) {
			throw new PrismLangException("Not enough arguments to \"" + e.getName() + "\" function", e);
		}
		if (e.getMaxArity() != -1 && e.getNumOperands() > e.getMaxArity()) {
			throw new PrismLangException("Too many arguments to \"" + e.getName() + "\" function", e);
		}
	}

	public void visitPost(ExpressionIdent e) throws PrismLangException
	{
		// By the time the expression is checked, this should
		// have been converted to an ExpressionVar/ExpressionConstant/...
		throw new PrismLangException("Undeclared identifier", e);
	}

	public void visitPost(ExpressionFormula e) throws PrismLangException
	{
		// This should have been defined or expanded by now
		if (e.getDefinition() == null)
			throw new PrismLangException("Unexpanded formula", e);
	}

	public void visitPost(ExpressionVar e) throws PrismLangException
	{
		// For PTAs, references to variables in modules have to be local
		// (no longer checked here, e.g. because allowed for digital clocks)
		/*if (modulesFile != null && modulesFile.getModelType() == ModelType.PTA && inModule != null) {
			if (!inModule.isLocalVariable(e.getName())) {
				throw new PrismLangException("Modules in a PTA cannot access non-local variables", e);
			}
		}*/
		// Clock references, in models, can only appear in invariants and guards
		// (Note: type checking has not been done, but we know types for ExpressionVars)
		if (e.getType() instanceof TypeClock && inModulesFile != null) {
			if (inInvariant == null && inGuard == null) {
				throw new PrismLangException("Reference to a clock variable cannot appear here", e);
			}
		}
	}

	public void visitPost(ExpressionProb e) throws PrismLangException
	{
		if (e.getModifier() != null) {
			throw new PrismLangException("Modifier \"" + e.getModifier() + "\" not supported for P operator", e);
		}
		if (e.getProb() != null && !e.getProb().isConstant()) {
			throw new PrismLangException("P operator probability bound is not constant", e.getProb());
		}
	}

	public void visitPost(ExpressionReward e) throws PrismLangException
	{
		if (e.getModifier() != null) {
			if (e.getModifier().equals("path")) {
				if (e.getBound() == null) {
					throw new PrismLangException("Properties of the form R(path)[...] are path formulas and cannot use =?", e);
				}
			} else if (!(e.getModifier().equals("exp"))) {
				throw new PrismLangException("Modifier \"" + e.getModifier() + "\" not supported for R operator", e);
			}
		}
		if (e.getRewardStructIndex() != null) {
			if (e.getRewardStructIndex() instanceof Expression) {
				Expression rsi = (Expression) e.getRewardStructIndex();
				if (!(rsi.isConstant())) {
					throw new PrismLangException("R operator reward struct index is not constant", rsi);
				}
			} else if (e.getRewardStructIndex() instanceof String) {
				String s = (String) e.getRewardStructIndex();
				if (modulesFile != null && modulesFile.getRewardStructIndex(s) == -1) {
					throw new PrismLangException("R operator reward struct index \"" + s + "\" does not exist", e);
				}
			}
		}
		if (e.getRewardStructIndexDiv() != null) {
			if (e.getRewardStructIndexDiv() instanceof Expression) {
				Expression rsi = (Expression) e.getRewardStructIndexDiv();
				if (!(rsi.isConstant())) {
					throw new PrismLangException("R operator reward struct index is not constant", rsi);
				}
			} else if (e.getRewardStructIndexDiv() instanceof String) {
				String s = (String) e.getRewardStructIndexDiv();
				if (modulesFile != null && modulesFile.getRewardStructIndex(s) == -1) {
					throw new PrismLangException("R operator reward struct index \"" + s + "\" does not exist", e);
				}
			}
		}
		if (e.getReward() != null && !e.getReward().isConstant()) {
			throw new PrismLangException("R operator reward bound is not constant", e.getReward());
		}
	}

	public void visitPost(ExpressionSS e) throws PrismLangException
	{
		if (e.getModifier() != null) {
			throw new PrismLangException("Modifier \"" + e.getModifier() + "\" not supported for S operator", e);
		}
		if (e.getProb() != null && !e.getProb().isConstant()) {
			throw new PrismLangException("S operator probability bound is not constant", e.getProb());
		}
	}

	public void visitPost(ExpressionLabel e) throws PrismLangException
	{
		LabelList labelList;
		if (propertiesFile != null)
			labelList = propertiesFile.getCombinedLabelList();
		else if (modulesFile != null)
			labelList = modulesFile.getLabelList();
		else
			throw new PrismLangException("Undeclared label", e);
		String name = e.getName();
		// Allow special cases
		if ("deadlock".equals(name) || "init".equals(name))
			return;
		// Otherwise check list
		if (labelList == null || labelList.getLabelIndex(name) == -1) {
			throw new PrismLangException("Undeclared label", e);
		}
	}

	public void visitPost(ExpressionFilter e) throws PrismLangException
	{
		// Check filter type is valid
		if (e.getOperatorType() == null) {
			throw new PrismLangException("Unknown filter type \"" + e.getOperatorName() + "\"", e);
		}
	}
}
