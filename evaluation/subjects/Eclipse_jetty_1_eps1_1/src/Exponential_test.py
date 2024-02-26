import numpy as np


def exponential_test(num_tail_samples, sortedSet):
    # go from the threshold to the size of the number of samples
    threshold = 2 # should be 10

    min_num_tail_samples = min(sortedSet.size(), num_tail_samples)
    num_tail_samples = max(min_num_tail_samples, 15) 

    # num_tail_samples will be given and at most will be 50.
    for j in range(threshold, num_tail_samples+1):
        # sorted list of the cost difference that starts at j-1 to the end.
        X_tail = sorted(list(set(sortedSet)))[-j:]
        print(X_tail)
        # Do the exponetial testing
        m = np.mean(X_tail)
        st = np.std(X_tail)
        cv = st/m
        # if the cv is greater than 1 then break.
        if cv > 1:
            return False
        # If j gets to the last sample then exp test has passed and return true.
        if j == num_tail_samples:
            exp_test = True
            return True
            exp_test_iter = j

sortedSet = [0.0, 3.0, 6.0, 9.0, 12.0, 15.0]
print(exponential_test(4, sortedSet))