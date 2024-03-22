## clean_experiments.sh
#####################################
# chmod +x clean_experiments.sh
# ./clean_experiments.sh
#

trap "exit" INT

declare -a subjects=(
 "Eclipse_jetty_1_eps1_1"
 "leaksn1b-5_1"
 "blazer_loopandbranch_safe"
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

cd ../../subjects

for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  run_counter=$(( $run_counter + 1 ))
  echo "[$run_counter/$total_number_subjects] Clean ${subjects[i]}.."

  rm -rf ./${subjects[i]}/log
  git rm -r ./${subjects[i]}/log


done

echo "Done."
