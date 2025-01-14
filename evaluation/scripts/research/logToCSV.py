import pandas as pd
import os
import subprocess

# log_dir = "/log/log_30min_2/"
output_file = "output.csv"

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


print(log_folders_with_file[0])
for cur_subject in log_folders_with_file:
    # cur_subject = "../../subjects/" + subject + log_dir

    if not os.path.exists(cur_subject + "Test_Passed_Log.txt"):
        print("Test_Passed_Log.txt doesnt exist for:", cur_subject)
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

    print("Finished:", cur_subject)