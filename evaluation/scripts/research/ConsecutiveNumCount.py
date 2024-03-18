import pandas as pd
import math
import os
import subprocess

log_dir = "/log/log_30min_2/"
output_file = "consecutiveNumCount.csv"

subjects=[
 "Eclipse_jetty_1_eps1_1",
 "leaksn1b-5_1",
 "blazer_loopandbranch_safe",
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
        log = file.readlines()

    arr = [float(num.replace("\n", "")) for num in log if float(num.replace("\n", "")) >= 0]
    count_arr = []

    curr = arr[0]
    count = 0
    for num in arr:
        if num == curr:
            count += 1
        elif num != curr:
            count_arr.append([curr, count])
            curr = num
            count = 1

    pd_count_arr = pd.DataFrame(count_arr)
    pd_count_arr.to_csv(cur_subject + output_file, index=False, header=False)

    try:
        subprocess.run(["git", "add", cur_subject + output_file], check=True)
        print(f"Successfully added {subject} to git.")
    except subprocess.CalledProcessError as e:
        print(f"Error: Failed while adding {subject} to git.")

    print("Finished:", subject)
