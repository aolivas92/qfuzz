trap "exit" INT

declare -a subjects=(
 #
 "Eclipse_jetty_1_eps1_1"
 "leaksn1b-5_1"
 "blazer_loopandbranch_unsafe"
 "blazer_modpow1_unsafe"
 "blazer_modpow2_unsafe"
 "blazer_k96_unsafe"
 "blazer_gpt14_unsafe"
 "blazer_login_unsafe"
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
