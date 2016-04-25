HEADER_FILE="header.txt" #where is the header?
INCLUDE=["../src"] #to which files/directorys should be added?
EXCLUDE=[".DS_Store", "resources"] #which files/directorys should be ignored?
UPDATE_HEADER=True #remove old header?

#which comment characters should be used?
START_COMMENT="/**" #first line of comment
LINE_COMMENT=" *" #lines between first and last comment
END_COMMENT=" */" #last line of comment, can be empty
BLANK_FIRST_LINE=True #leave first line empty?

import os
import itertools

def writeHeader(header, filePath):
    with open(filePath, "r") as f:
        original = f.readlines()
    with open(filePath, "w") as f:
        content = original

        if UPDATE_HEADER:
            content = itertools.dropwhile(
                        lambda x: x.startswith(START_COMMENT) or
                                x.startswith(LINE_COMMENT) or
                                x.startswith(END_COMMENT)
                        , original)
        content = itertools.dropwhile(lambda x:  not x or x.isspace(), content)
        newLines = header + "\n\n" + "".join(content)
        f.write(newLines)

def asComment(lines):
    comment = START_COMMENT
    isFirstLine = True

    if BLANK_FIRST_LINE: comment += "\n" + LINE_COMMENT + " "
    else: comment += " "

    comment += lines[0]

    for line in lines[1:]:
        commentedLine = LINE_COMMENT + " " + line
        comment += commentedLine

    comment += END_COMMENT
    return comment

def addHeader(header):
    def headerRec(path):
        #print(path)
        for item in os.listdir(path):
            itemPath = os.path.abspath(path + "/"+item)
            if not item in EXCLUDE:
                if(os.path.isfile(itemPath)):
                    print("add to: " + itemPath)
                    writeHeader(header, itemPath)
                elif(os.path.isdir(itemPath)):
                    headerRec(itemPath)
            else:
                print("ignored: "+item)

    for item in INCLUDE:
        headerRec(item)

def main():
    with open(HEADER_FILE) as hfile:
        headerLines = hfile.readlines()
        commentHeader = asComment(headerLines)
        addHeader(commentHeader)

main()
