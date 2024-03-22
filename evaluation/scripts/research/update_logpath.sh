trap "exit" INT

declare -a subjects=(
 #
#  "Eclipse_jetty_1_eps1_1"
#  "leaksn1b-5_1"
 "blazer_loopandbranch_unsafe"
 "blazer_modpow1_unsafe"
 "blazer_modpow2_unsafe"
 "blazer_k96_unsafe"
 "blazer_gpt14_unsafe"
 "blazer_login_unsafe"
 )
 
 #driver="Driver_KDynamic"
#driver="Driver_Greedy"
driver="Driver_Greedy_Guarantee"

 new_dir_path="./log/log_30min_3/"

 total_number_subjects=${#subjects[@]}
 echo

 cd ../../subjects


 for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
 do

   cd ./${subjects[i]}/
   cd src
   
   sed -i "s|String dirPath = \".*\";|String dirPath = \"$new_dir_path\";|g" "$driver.java"

   git add $driver.java

   cd ../../

 done

 echo ">> Finished updating dir log path for subjects."
