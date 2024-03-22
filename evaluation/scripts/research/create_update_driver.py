import os

subjects=[
 "Eclipse_jetty_1_eps1_1",
#  "leaksn1b-5_1",
#  "blazer_loopandbranch_unsafe",
#  "blazer_modpow1_unsafe",
#  "blazer_modpow2_unsafe",
#  "blazer_k96_unsafe",
#  "blazer_gpt14_unsafe",
#  "blazer_login_unsafe",
]

source_file = "../../subjects/leaksn1b-5_1/src/Driver_Greedy_Guarantee.java"
start_string = "// Start of research"

destination_code_start_string_1 = "// Start of research"
destination_code_start_string_2 = 'System.out.println("Done.");'

destination_imports_start_string_1 = "import java.util.Arrays;"
destination_imports_start_string_2 = "import edu.cmu.sv.kelinci.quantification.Greedy;"

new_imports_1 = {"import java.io.FileInputStream;\n",
                 "import java.io.IOException;\n",
                 "import java.nio.ByteBuffer;\n",
                 "import java.util.Arrays;\n",
                 "import java.util.ArrayList;\n",
                 "import java.util.List;\n",
                 "import java.util.TreeSet;\n",
                 "import java.util.stream.Stream;\n",
                 "import java.util.SortedSet;\n",}
new_imports_2 = {"import java.io.BufferedReader;\n",
                 "import java.io.File;\n",
                 "import java.io.FileReader;\n",
                 "import java.io.FileWriter;\n",
                 "import java.lang.NumberFormatException;\n",
                 "import java.io.PrintWriter;\n",}

with open(source_file, 'r') as source:
        source_lines = source.readlines()

        extracting = False
        extracted_code = []

        for line in source_lines:
            if start_string in line:
                extracting = True
            if extracting:
                extracted_code.append(line)

for subject in subjects:
    cur_subject = "../../subjects/" + subject + "/src/"

    if not os.path.exists(cur_subject + "Driver_Greedy_Guarantee.java"):
        old_driver = os.path.join(cur_subject + "Driver_Greedy.java")
        new_driver = os.path.join(cur_subject + "Driver_Greedy_Guarantee.java")
        os.system('cp ' + old_driver + " " + new_driver)
        os.system('sed -i "s|public class Driver_Greedy {|public class Driver_Greedy_Guarantee {|g" "' + new_driver + '"')

    destination_file = "../../subjects/" + subject + "/src/Driver_Greedy_Guarantee.java"
    needed_imports_1 = set(new_imports_1).copy()
    needed_imports_2 = set(new_imports_2).copy()

    with open(destination_file, 'r+') as destination:
        destination_lines = destination.readlines()

        insertion_code_index = -1
        insertion_imports_1_index = -1
        insertion_imports_2_index = -1
        for i, line in enumerate(destination_lines):
            if destination_imports_start_string_1 in line:
                insertion_imports_1_index = i
            if line in needed_imports_1:
                needed_imports_1.remove(line)

            if destination_imports_start_string_2 in line:
                insertion_imports_2_index = i
            if line in needed_imports_2:
                needed_imports_2.remove(line)

            if destination_code_start_string_1 in line or destination_code_start_string_2 in line:
                insertion_code_index = i
                break
        
        if insertion_code_index != -1:
            destination_lines[insertion_code_index:] = extracted_code
        else:
            print("Failed code insertion with: " + subject)


        if insertion_imports_1_index != -1 and len(needed_imports_1) > 0:
            destination_lines[:] = destination_lines[:insertion_imports_1_index + 1] + list(needed_imports_1) + destination_lines[insertion_imports_1_index + 1:]
        else:
            print("Failed imports 1 with: " + subject)
        
        insertion_imports_2_index += len(needed_imports_1)
        if insertion_imports_2_index != -1 and len(needed_imports_2) > 0:
            destination_lines[:] = destination_lines[:insertion_imports_2_index + 1] + ["\n"] + list(needed_imports_2) + destination_lines[insertion_imports_2_index + 1:]
        else:
            print("Failed imports 2 with: " + subject)
        
        destination.seek(0)  # Rewind to the beginning of the destination file
        destination.writelines(destination_lines)
    
    cur_subject = os.path.join("../../subjects/" + subject + "/src/Driver_Greedy_Guarantee.java")
    # os.system('git add ' + cur_subject)
