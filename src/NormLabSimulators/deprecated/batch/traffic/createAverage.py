from __future__ import division


import csv
import sys
import numpy as np
import os

def getMeans(files, meanvals):
    for i in xrange(0, 6):
        vals = None
        for num, filename in enumerate(files, 1):
            column = np.genfromtxt(filename, dtype=None, delimiter=';', skiprows=1, usecols=(i), userows=4165)
	    print num
	    print filename
            print(column)
            if vals is None:
                vals = column
            else:
                vals += column

        meanvals[i] = (vals / num)
    return meanvals, column

def saveMeans(column, meanvals, path):
    # with open(files[0], 'rb') as fin:
    #     # skip first row
    #     # next(fin)
    #     first = np.genfromtxt(fin, dtype=str, delimiter=';')
    #     # a = ([s.replace('"', '') for s in first])

    with open(path, 'wb') as fout:
        writer = csv.writer(fout, delimiter=';', lineterminator='\n')
        first = ["run","tick","NormativeNetworkCardinality","NormativeSystemCardinality","ComplaintsForNonRegulatedNorms","AvgNonRegulatedCollisions"]

        writer.writerow(first)
        for i in xrange(0, column.size):
            rowtowrite = list()
            for j in xrange(0, 6):
                rowtowrite.append(meanvals.get(j)[i])
            writer.writerow(rowtowrite)

# def searchFiles(path, poblacion):
#     # files = ['Data/fileA.txt', 'Data/fileB.txt']
#     for i in xrange(1, 10):
#         # files = path + poblacion + str(i)
#         # print files
#
#         for file in os.listdir(path):
#             if file.endswith(poblacion + str(i)+".dat"):
#                 print file
#     return files


def main(path, runs):
        files = []
        meanvals = {}
        for i in xrange(1, int(runs)):
            poblacion = "TrafficDataOutput.dat."+str(i)
            file = path + poblacion
            files.append(file)

        meanvals, column = getMeans(files, meanvals)
        pathAverage = path+"/-average.dat"

        # Create folder if doesnt exists.

        dir = os.path.dirname(pathAverage)
        try:
            os.stat(dir)
        except:
            os.mkdir(dir)

        saveMeans(column, meanvals, pathAverage)

        # Create plot of the average file.
        plot.main(pathAverage)

        print pathAverage + " COMPUTED"

#_______________________________________________________________
#
#
#                         MAIN
#
#      Call:
#
#             python createAverage.py {PATH} {N Of Runs}
#
#
#_______________________________________________________________


if __name__ == '__main__':
    path = sys.argv[1]
    runs = sys.argv[2]
    print path

    main(path, runs)

