trap "exit" INT

declare -a subjects=(
 #
 # Leak Set 5
 "leaksn1b-5_1"
 )

 run_counter=0
 total_number_subjects=${#subjects[@]}
 echo

 cd ../subjects

 for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
 do
   run_counter=$(( $run_counter + 1 ))
   echo "[$run_counter/$total_number_subjects] Prepare ${subjects[i]}.."

   cd ./${subjects[i]}/
   rm -rf bin
   mkdir bin
   cd src
   javac -cp ".:../../../../tool/instrumentor/build/libs/kelinci.jar:../lib/*" *.java -d ../bin
   cd ..
   rm -rf bin-instr
   java -cp "../../../tool/instrumentor/build/libs/kelinci.jar:lib/*" edu.cmu.sv.kelinci.instrumentor.Instrumentor -mode LABELS -i ./bin/ -o ./bin-instr -skipmain
   cd ..
   echo

 done

 echo ">> Finished preparing the experiment subjects."
