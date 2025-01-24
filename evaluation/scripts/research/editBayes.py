import pandas as pd
import numpy as np
import os



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

idx = log_folders_with_file.index("../../../evaluation/subjects/leaksn1b-5_1/log/log_30min_y/")
log_folders_with_file = log_folders_with_file[idx + 19:]
log_folders_with_file = log_folders_with_file[::-1]

for subject in log_folders_with_file:
    if subject == "../../../evaluation/subjects/blazer_passwordEq_unsafe/log/log_30min_l/":
        continue
    csv_file = subject + "BayesFactor.csv"
    try:
        df = pd.read_csv(csv_file)
    except pd.errors.EmptyDataError:
        continue

    selected_columns = df.iloc[:, 1:3]
    #print(selected_columns)

    list1 = []

    for col in selected_columns.columns:
        list1.append(selected_columns[col].values)

    # filter list1 so only doubles are in it
    list1 = np.array(list1)
    list1 = list1.flatten()
    list1 = [x for x in list1 if str(x) != 'nan']
    # make list1 to a dataframe
    df_list1 = pd.DataFrame(list1)
    print(df_list1)
    print(subject)

    # Open log file
    log = subject + "Log.txt"

    with open(log, 'r') as file:
        whole_log = file.readlines()

    arr_val = [float(num.replace("\n", "")) for num in whole_log if float(num.replace("\n", "")) >= 0]

    # df_all has all values from log file
    df_all = pd.DataFrame(arr_val)

    df_new = pd.DataFrame()

    list1 = []

    # interleave columns from bayes and all values
    for i in range(1, len(df.columns)):
        # put ith column in df_new
        df_new = pd.concat([df_new, df.iloc[:, i]], axis=1)
        df_new = pd.concat([df_new, df_all], axis=1)

    # Set column names to integers
    df_new.columns = range(df_new.shape[1])

    # Reset row indices to integers
    #df_new.reset_index(drop=True, inplace=True)
    df_new.insert(0, '', None)


    df_new.to_csv(subject + "BayesFactor2.csv", index=False)