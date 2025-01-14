import pandas as pd
import math
import os
import subprocess

log_dir = "/log/log_30min_1/"
output_file = "BayesFactor.csv"



# Path to the subjects folder
subjects_dir = '../../../evaluation/subjects'

# List to store paths to subfolders containing 'Test_Passed_log.txt'
log_folders_with_file = []

# Traverse the subjects folder
for subject_name in os.listdir(subjects_dir):
    subject_path = os.path.join(subjects_dir, subject_name)

    if os.path.isdir(subject_path):  # Check if it's a directory
        log_path = os.path.join(subject_path, 'log')

        if os.path.isdir(log_path):  # Check if the 'log' folder exists
            # Traverse the log folder
            for log_subfolder in os.listdir(log_path):
                log_subfolder_path = os.path.join(log_path, log_subfolder)

                if os.path.isdir(log_subfolder_path):  # Check if it's a subfolder
                    # Check if 'Test_Passed_log.txt' exists
                    log_file_path = os.path.join(log_subfolder_path, 'Test_Passed_log.txt')
                    if os.path.isfile(log_file_path):
                        log_folders_with_file.append(log_subfolder_path + '/')

for cur_subject in log_folders_with_file:


    if not os.path.exists(cur_subject + "Test_Passed_Log.txt"):
        print("Test_Passed_Log.txt doesnt exist for:", cur_subject)
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

    print("Finished:", cur_subject)