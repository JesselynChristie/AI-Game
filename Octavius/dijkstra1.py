# Terrain

# 00 = free space
# 09 = wall (tidak bisa dilalui)

# start in 1,1
# end in 8,8
startPos = [1,1]
endPos = [8,8]
def myGrid():
    myGrid = [
        [9,9,9,9,9,9,9,9,9,9],
        [9,0,0,0,0,0,0,0,0,9],
        [9,0,0,0,0,0,0,0,0,9],
        [9,0,0,0,0,0,0,0,0,9],
        [9,0,0,0,0,0,0,0,0,9],
        [9,0,0,0,0,0,0,0,0,9],
        [9,0,0,0,0,0,0,0,0,9],
        [9,0,0,0,0,0,0,0,0,9],
        [9,0,0,0,0,0,0,0,0,9],
        [9,9,9,9,9,9,9,9,9,9]
    ]
    return myGrid

def printGrid(grid):
    grid[startPos[0]][startPos[1]] = "S"
    grid[endPos[0]][endPos[1]] = "F"
    for i in range(len(grid)):
        print(grid[i])

printGrid(myGrid())