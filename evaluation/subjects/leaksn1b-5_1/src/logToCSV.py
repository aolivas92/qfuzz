import pandas as pd

path = "../log/log_30min_1"

with open(path + '/Log.txt', 'r') as file:
    whole_log = file.readlines()

split_arr = [float(num.replace("\n", "")) for num in whole_log if float(num.replace("\n", "")) >= 0]
indices = []

with open(path + '/Test_Passed_Log.txt', 'r') as file:
    for line in file:

        print(line)
        indices.append(int(line.split()[0]))

splitteed_array_FINAL = []
for i in indices:
    splitteed_array_FINAL.append(split_arr[:i])
    splitteed_array_FINAL.append(split_arr[i:])

le_data = pd.DataFrame(splitteed_array_FINAL)
le_data = le_data.transpose()
le_data.to_csv(path + "/output2.csv")