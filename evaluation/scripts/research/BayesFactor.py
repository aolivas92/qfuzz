import pandas as pd
import math
import os
import subprocess

log_dir = "/log/log_30min_1/"
output_file = "BayesFactor.csv"

subjects=[
 "Eclipse_jetty_1_eps1_1",
 "leaksn1b-5_1",
 "blazer_loopandbranch_unsafe",
 "blazer_modpow1_unsafe",
 "blazer_modpow2_unsafe",
 "blazer_k96_unsafe",
 "blazer_gpt14_unsafe",
 "blazer_login_unsafe",
 "blazer_unixlogin_unsafe",
 "leaksn1b-1_1"
]
for subject in subjects:
    cur_subject = "../../subjects/" + subject + log_dir

    if not os.path.exists(cur_subject + "Test_Passed_Log.txt"):
        print("Test_Passed_Log.txt doesnt exist for:", subject)
        continue

    with open(cur_subject + 'Log.txt', 'r') as file:
        log = file.readlines()

    # Array is filtered of negative values
    cost_obs = [float(num.replace("\n", "")) for num in log if float(num.replace("\n", "")) >= 0]

    n = 100

    bayes_test_arr = []

    max_val = -1
    counter = 0
    slicing_beginning_index = 0

    # keep track of index when last test was passed

    for i in range(0, len(cost_obs)):
        # find Max in array so far
        if cost_obs[i] > max_val:
            # if max is found, reset count
            max_val = cost_obs[i]
            counter = 0
        elif counter > n:
            bayes_test_arr.append(cost_obs[slicing_beginning_index:i])
            slicing_beginning_index = i
            counter = 0
        counter += 1

    # Make the rows into columns before converting to csv

    pd_bayes_arr = pd.DataFrame(bayes_test_arr)
    pd_bayes_arr = pd_bayes_arr.transpose()
    print(pd_bayes_arr)

    pd_bayes_arr.to_csv(cur_subject + output_file, index=True, header=False)

    print("Finished:", subject)