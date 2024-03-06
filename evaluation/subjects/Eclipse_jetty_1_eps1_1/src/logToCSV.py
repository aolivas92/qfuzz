import csv
with open('Log.txt', 'r') as file:
    whole_log = [file.read()]

split_arr = whole_log[0].split()
split_arr = [float(num) for num in split_arr]


partitioned_arr = [[]]

partition = 0
for i in range(0, len(split_arr) - 1):
    if split_arr[i] >= 0:
        partitioned_arr[partition].append(split_arr[i])
    else:
        partitioned_arr += [[]]
        partition = 1 + partition


csv_file = "exampleCSV.csv"

with open(csv_file, 'w', newline='') as file:
    writer = csv.writer(file)
    writer.writerows(partitioned_arr)

print(len(partitioned_arr))
print(partitioned_arr)
