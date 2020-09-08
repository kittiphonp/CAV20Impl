. ./../env.sh

echo $myRoot
code=( DFL DFL_m WP genGraph )
#code=( DFL )

for m in ${code[@]}; do
  echo "Updating $m"
  for visitor in ASTVisitor ASTTraverseModify ASTTraverse Rename SemanticCheck; do
    echo "copying $visitor.java..."
    cp -f $visitor.java $myRoot/mycode/$m/src/parser/visitor/
    echo "cp -f $visitor.java $myRoot/mycode/$m/src/parser/visitor/"
  done

  for parser in VarList PrismParser; do
    echo "copying $parser.java..."
    cp -f $parser.java $myRoot/mycode/$m/src/parser/
  done

  for prism in ExplicitModel2MTBDD Modules2MTBDD; do
    echo "copying $prism.java..."
    cp -f $prism.java $myRoot/mycode/$m/src/prism/
  done

  for pta in DigitalClocks Modules2PTA; do
    echo "copying $pta.java..."
    cp -f $pta.java $myRoot/mycode/$m/src/pta/
  done

  for simulator in Updater; do
    echo "copying $simulator.java..."
    cp -f $simulator.java $myRoot/mycode/$m/src/simulator/
  done

  for UI in SimulationView GUISimulatorPathTableModel; do
    echo "copying $UI.java..."
    cp -f $UI.java $myRoot/mycode/$m/src/userinterface/simulator/
  done
done