import pandas as pd
import math

path = "../log/log_30min_1"

with open(path + '/Log.txt', 'r') as file:
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
pd_count_arr.to_csv(path + "/consecutiveNumCount.csv", index=False, header=False)
