#!/bin/bash

declare -a subjects=(
#"f"
#"g"
#"h"
#"i"
#"j"
#"k"
#"l"
#"m"
#"n"
#"o"
#"p"
#"q"
#"r"
"s"
"t"
"u"
"v"
"w"
"x"
"y"
"z"
"aa"
"ab"
"ac"
"ad"	
)

for f_name in ${subjects[@]}; do
	bash ./clean_experiments.sh
	cd research/
	bash ./update_logpath.sh $f_name
	cd ..
	bash ./prepare_research_subjects.sh
	bash ./run_research_subjects.sh
done
