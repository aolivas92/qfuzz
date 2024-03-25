import pandas as pd
import os
import subprocess

log_dir = "/log/log_30min_10/"
output_file = "output.csv"

subjects=[
 "Eclipse_jetty_1_eps1_1",
 "leaksn1b-5_1",
#  "blazer_loopandbranch_safe",
 "blazer_modpow1_unsafe",
 "blazer_modpow2_unsafe",
 "blazer_k96_unsafe",
 "blazer_gpt14_unsafe",
 "blazer_login_unsafe",
]

for subject in subjects:
    cur_subject = "../../subjects/" + subject + log_dir

    if not os.path.exists(cur_subject + "Test_Passed_Log.txt"):
        print("Test_Passed_Log.txt doesnt exist for:", subject)
        continue

    with open(cur_subject + 'Log.txt', 'r') as file:
        whole_log = file.readlines()

    split_arr = [float(num.replace("\n", "")) for num in whole_log if float(num.replace("\n", "")) >= 0]
    indices = []

    with open(cur_subject + 'Test_Passed_Log.txt', 'r') as file:
        # Skip the first line
        next(file)
        for line in file:
            # print(line)
            indices.append(int(line.split()[0]))

    splitteed_array_FINAL = []
    for i in indices:
        splitteed_array_FINAL.append(split_arr[:i])
        splitteed_array_FINAL.append(split_arr[i:])

    le_data = pd.DataFrame(splitteed_array_FINAL)
    le_data = le_data.transpose()
    le_data.to_csv(cur_subject + output_file)

    try:
        subprocess.run(["git", "add", cur_subject + output_file], check=True)
        print(f"Successfully added {subject} to git.")
    except subprocess.CalledProcessError as e:
        print(f"Error: Failed while adding {subject} to git.")

    print("Finished:", subject)