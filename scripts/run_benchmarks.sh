#!/bin/bash

. ./env.sh

#Solver names
DFL="$myRoot/mycode/DFL/bin/prism"
DFL_m="$myRoot/mycode/DFL_m/bin/prism"
WP="$myRoot/mycode/WP/bin/prism"
EC="$myRoot/mycode/genGraph/bin/prism"


mainLog="$myRoot/mydata$1"
mkdir $mainLog


for solverName in DFL DFL-BRTDP_0 DFL_m WP EC; do
    echo $(date +%d.%m.%y---%H:%M:%S) $solverName
    logfile="$mainLog/$solverName"
    mkdir $logfile

#solver defs (solver, 2stormopts, logfile and options, const(=-const or -constants), canDoGames)
    if [[ $solverName == "DFL" ]] ; then
        solver=$DFL
        options="-BVI_A -javamaxmem 2g"
        #Storm stuff
        StormPrism=""
        StormProp=""
        const="-const"
        canDoGames=true
        reps=0
    elif [[ $solverName == "DFL-BRTDP"* ]] ; then
        solver=$DFL
        options="-heuristic RTDP_ADJ -RTDP_ADJ_OPTS $(echo $solverName | cut -d '_' -f 2) -javamaxmem 2g -heuristic_verbose" #CAREFUL: NOT USED; OVERWRITTEN LATER FOR MDP OPT
        #Storm stuff
        StormPrism=""
        StormProp=""
        const="-const"
        canDoGames=true
        reps=2
    elif [[ $solverName == "DFL_m" ]] ; then
        solver=$DFL_m
        options="-BVI_A -javamaxmem 2g"
        #Storm stuff
        StormPrism=""
        StormProp=""
        const="-const"
        canDoGames=true
        reps=0
    elif [[ $solverName == "WP" ]] ; then
        solver=$WP
        options="-ex -BVI_A -javamaxmem 2g"
        #Storm stuff
        StormPrism=""
        StormProp=""
        const="-const"
        canDoGames=true
        reps=0
    elif [[ $solverName == "EC" ]] ; then #EC computation without BVI
        solver=$EC
        options="-BVI_A -javamaxmem 2g"
        #Storm stuff
        StormPrism=""
        StormProp=""
        const="-const"
        canDoGames=true
        reps=0
    else
        echo "I don't know this solver: $solverName Exiting."
        exit
    fi

  

        




    for m in mdsm cloud teamform investor manyECs; do 
        echo $(date +%d.%m.%y---%H:%M:%S) $m

        #model defs (model, props, params,isMDP)
        
        if [[ $m == "mdsm" ]] ; then 
            par1=( 3 4 )
            parName1="N"
            par2=( 1 )
            parName2="none"
            model="$myRoot/mymodels/mdsm/mdsm"
            props="$myRoot/mymodels/mdsm/mdsm.props"
            isMDP=false
        elif [[ $m == "teamform" ]] ; then
            par1=( 3 4 5 )
            parName1="N"
            par2=( 1 )
            parName2="none"
            model="$myRoot/mymodels/team-form/team-form-offline-fc-"
            props="$myRoot/mymodels/team-form/team-form.props"
            isMDP=false
        elif [[ $m == "cloud" ]] ; then
            par1=( 5 6 7 )
            parName1="N"
            par2=( 1 )
            parName2="none"
            model="$myRoot/mymodels/cloud/cloud_"
            props="$myRoot/mymodels/cloud/cloud.props"
            isMDP=false
        elif [[ $m == "investor" ]] ; then 
            par1=( 50 100 )
            parName1="N"
            par2=( 1 )
            parName2="none"
            model="$myRoot/mymodels/investor/investor.prism" 
            props="$myRoot/mymodels/investor/investor.props"
            isMDP=false
        elif [[ $m == "manyECs" ]] ; then 
            par1=( 500 1000 5000 )
            parName1="N"
            par2=( 1 )
            parName2="none"
            model="$myRoot/mymodels/manyECs/manyECs" 
            props="$myRoot/mymodels/manyECs/manyECs.props"
            isMDP=false
        else
            echo "I don't know this model: $m Exiting."
            exit
        fi


        mkdir $logfile/$m

        for p1 in ${par1[@]}; do
        for p2 in ${par2[@]}; do

	    echo $(date +%d.%m.%y---%H:%M:%S) $p1,$p2

            if [[ $parName2 == "none" ]] ; then
                outputfile=$logfile/$m/$parName1\_$p1
            else
                outputfile=$logfile/$m/$parName1\_$p1\_$parName2\_$p2
            fi

            #modelStr and params for each model 

            if [[ $m == "mdsm" ]] ; then
                params=""
                modelStr="$p1.prism"
            elif [[ $m == "teamform" ]] ; then
                params=""
                modelStr="$p1.prism"
            elif [[ $m == "cloud" ]] ; then
                params=""
                modelStr="$p1.prism"
            elif [[ $m == "investor" ]] ; then
                params="-const vmax=$p1,vinit=5"
                modelStr=""
            elif [[ $m == "manyECs" ]] ; then
                params=""
                modelStr="$p1.prism"
            fi
            
            #optimize RTDP for MDPs
            if [[ $solverName == "DFL-BRTDP"* ]] ; then
                if $isMDP ; then
                    options="-heuristic RTDP_ADJ -RTDP_ADJ_OPTS $(echo "$(echo $solverName | cut -d '_' -f 2)+128" | bc ) -heuristic_verbose -javamaxmem 2g"
                else
                    options="-heuristic RTDP_ADJ -RTDP_ADJ_OPTS $(echo $solverName | cut -d '_' -f 2) -heuristic_verbose -javamaxmem 2g"
                fi
            fi

            timeout="15m"

            if [[ $solverName == "EC" ]] ; then
                timeout="5m"
                taskset 0x1 /usr/bin/time -v -o $outputfile.stat timeout $timeout $solver $StormPrism $model$modelStr $StormProp $props $params $options > $outputfile.log
                python3 countEC.py model.txt > $outputfile.txt
		mv model.txt $outputfile.model
            else
                if [[ $reps == 0 ]] ; then
                    taskset 0x1 /usr/bin/time -v -o $outputfile.stat timeout $timeout $solver $StormPrism $model$modelStr $StormProp $props $params $options > $outputfile.log
                else
                    for ((r=1;r <= $reps; r++)); do
                        outputfileReps=$outputfile\-$r
                        taskset 0x1 /usr/bin/time -v -o $outputfileReps.stat timeout $timeout $solver $StormPrism $model$modelStr $StormProp $props $params $options > $outputfileReps.log
                    done
                fi
            fi
        done
        done
    done
done

