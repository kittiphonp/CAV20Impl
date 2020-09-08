#!/bin/bash

function readLog {              
    if [[ $7 == "-t" || $7 == "" ]]; then #-t ... time
    #grep condition for the model checking time
        if [[ $8 == "DFL-BRTDP"* ]]; then
            lineRead="Model checking completed"
        else
            lineRead="Value iteration ("
        fi
        lineFile="log"
    #grep condition for the global time
        #lineRead="User time (seconds)"
        #lineFile="stat"
    elif [[ $7 == "-s" ]]; then #-s ... states
        if [[ $8 == "DFL-BRTDP"* ]]; then
            lineRead="Explore:"
        else
            lineRead="States:"
        fi
        lineFile="log"
    elif [[ $7 == "-u" ]]; then #-u ... no. of iterations
        if [[ $8 == "DFL-BRTDP"* ]]; then
            lineRead="Trials:"
        else
            lineRead="Value iteration ("
        fi
        lineFile="log"
    elif [[ $7 == "-m" ]]; then #-m ... MECs
        if [[ $8 == "EC" ]]; then
            lineRead="MEC"
        else
            echo "EC computation is done separately from BVI computation"
            exit
        fi
        lineFile="log"
    elif [[ $7 == "-tr" ]]; then #-tr ... no. of transitions
        lineRead="Transitions:"
        lineFile="log"
    else
       echo "I do not know the option $7 in readLog; this should never happen. Exit."
       exit
    fi


    if [[ $4 == "none" ]] && [[ $6 == "none" ]] #specify filePath
    then
        filePath=$1/$2\_$3;
    elif [[ $4 == "none" ]]
    then 
        filePath=$1/$2\_$3-$6;
    elif [[ $6 == "none" ]]
    then
        filePath=$1/$2\_$3\_$4\_$5;
    else 
        filePath=$1/$2\_$3\_$4\_$5-$6;
    fi

    line=$(grep -s "$lineRead" "$filePath.$lineFile" | tail -1);
    result=$(grep -s "Result" "$filePath.log");


    if [ $? -eq 1 ] && [ $7 == "-t" ]
    then
        echo "X"; #no result
    elif [[ $line == "" ]]
    then  
        echo "I"; #inexistant
    else
        if [[ $7 == "-t" || $7 == "" ]]; then 
            if [[ $8 == "DFL-BRTDP"* ]]; then
                time=$(echo $line | cut -d 'n' -f 3 | cut -d 's' -f 1) 
            else
                time=$(echo $line | cut -d 'd' -f 2 | cut -d 's' -f 1) 
            fi
            echo $(echo "scale=1;$time/1" | bc)
        elif [[ $7 == "-s" ]]; then
            if [[ $8 == "DFL-BRTDP"* ]]; then
                echo $(echo $line | cut -d ':' -f 2 | cut -d 'n' -f 1)
            else
                echo $(echo $line | cut -d ':' -f 2 | cut -d '(' -f 1)
            fi           
        elif [[ $7 == "-u" ]]; then
            if [[ $8 == "DFL-BRTDP"* ]]; then
                echo $(echo $line | cut -d ':' -f 2)
            else
                iters=$(echo $line | cut -d 'k' -f 2 | cut -d 'i' -f 1) 
                states=$(readLog $1 $2 $3 $4 $5 $6 "-s" $8)
                echo $iters
            fi  
        elif [[ $7 == "-m" ]]; then  
            mecs=$(echo $line | cut -d '/' -f 2 | cut -d ':' -f 1) 
            echo $mecs
        elif [[ $7 == "-tr" ]]; then
                echo $(echo $line | cut -d ':' -f 2)
        fi
    fi
}  


BASEPATH="../mydata"

OUTPUT="../mytable1.txt"

#legend
echo -e "Model \t\t Parameters \t #States \t #Trans \t #MECs \t\t DFL.itr \t DFL.time \t DFL_m.itr \t DFL_m.time \t DFL_B.itr \t DFL_B.visit \t DFL_B.time \t WP.itr \t WP.time" > $OUTPUT

models=( mdsm cloud teamform investor manyECs ) 
solvers=( DFL DFL_m DFL-BRTDP_0 WP )



for m in ${models[@]}; do
    echo $m
    
    if [[ $m == "mdsm" ]] ; then
        par1=( 3 4 )
        parName1="N"
        par2=( 1 )
        parName2="none"
    elif [[ $m == "cloud" ]] ; then
        par1=( 5 6 7 )
        parName1="N"
        par2=( 1 )
        parName2="none"
    elif [[ $m == "teamform" ]] ; then
        par1=( 3 4 5 )
        parName1="N"
        par2=( 1 )
        parName2="none"
    elif [[ $m == "investor" ]] ; then 
        par1=( 50 100 )
        parName1="N"
        par2=( 1 )
        parName2="none"
    elif [[ $m == "manyECs" ]] ; then 
        par1=( 500 1000 5000 )
        parName1="N"
        par2=( 1 )
        parName2="none"
    else
        echo "I don't know this model: $m Exiting."
        exit
    fi

    for p1 in ${par1[@]}; do
    for p2 in ${par2[@]}; do
            if [[ $parName1 == "A" ]]
            then
                parString="\t\t"
            elif [[ $parName2 == "none" ]]
            then
                parString="$parName1=$p1\t\t"
            else
                parString="$parName1=$p1 $parName2=$p2\t"
            fi
            echo "& $parName1=$p1 $parName2=$p2"

            solStr=""

            CURRPATH="$BASEPATH/DFL/$m"
            numStates=$(readLog $CURRPATH $parName1 $p1 $parName2 $p2 "none" -s "DFL")
            numTrans=$(readLog $CURRPATH $parName1 $p1 $parName2 $p2 "none" -tr "DFL")    


            for solver in ${solvers[@]}; do
                echo $solver
                CURRPATH="$BASEPATH/$solver/$m"

                if [[ $solver == "DFL-BRTDP"* ]]; then
                        sumTime=0
                        timeouts=0
                        samples=0
                        brtdpExplored=0
                        sumItr=0
                        for ((rep=1; rep<21; rep++)); do
                            time=$(readLog $CURRPATH $parName1 $p1 $parName2 $p2 $rep -t $solver)
                            explored=$(readLog $CURRPATH $parName1 $p1 $parName2 $p2 $rep -s $solver)
                            itr=$(readLog $CURRPATH $parName1 $p1 $parName2 $p2 $rep -u $solver)
                            if [[ $time == "X" ]]
                            then
                                timeouts=$(echo "$timeouts+1" | bc)
                            elif [[ $time == "I" ]]
                            then
                                time="File inexistant, do nothing"
                            else
                                samples=$(echo "$samples+1" | bc)
                                sumTime=$(echo "$sumTime+$time" | bc)
                                brtdpExplored=$(echo "$brtdpExplored+$explored" | bc)
                                sumItr=$(echo "$sumItr+$itr" | bc)
                            fi
                        done

                        if [[ $samples == 0 ]]
                        then
                            solStr+="X \t\t \t\t \t\t "
                        else
                            time=$(echo "scale=1;$sumTime/$samples" | bc)
                            itr=$(echo "scale=0;$sumItr/$samples" | bc)
                            brtdpExplored=$(echo "scale=0;$brtdpExplored/$samples" | bc)
                            brtdpExploredRatio=$(echo "scale=1;100*$brtdpExplored/$numStates" | bc)
                            solStr+="$itr \t\t $brtdpExploredRatio \t\t $time \t\t "
                        fi
                else
                    time=$(readLog $CURRPATH $parName1 $p1 $parName2 $p2 "none" -t $solver)
                    if [[ $time == "X" ]]
                    then
                        solStr+="X \t\t \t\t "
                    else
                        iter=$(readLog $CURRPATH $parName1 $p1 $parName2 $p2 "none" -u $solver)
                        solStr+="$iter \t\t $time \t\t "
                    fi
                fi
            done 


            #get numMECS
            CURRPATH="$BASEPATH/EC/$m"
            numMECS=$(<$CURRPATH/$parName1\_$p1.txt)


            if [[ $m == "mdsm" || $m == "cloud" ]] 
            then
                echo -e "$m \t\t $parString $numStates    \t $numTrans   \t $numMECS      \t $solStr" >> $OUTPUT
            else
                echo -e "$m \t $parString $numStates    \t $numTrans   \t $numMECS      \t $solStr" >> $OUTPUT
            fi

    done
    done
done
