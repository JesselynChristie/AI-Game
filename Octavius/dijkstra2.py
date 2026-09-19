import heapq as hpq
import time as tm
start_pos = [1, 1]
end_pos = [8, 8]

def myGrid():
    myGrid = [
        [9,9,9,9,9,9,9,9,9,9],
        [9,1,9,1,1,1,1,1,2,9],
        [9,1,9,1,9,1,1,1,2,9],
        [9,1,9,1,9,1,1,1,2,9],
        [9,1,9,1,9,1,1,1,2,9],
        [9,1,9,1,9,1,1,1,2,9],
        [9,1,9,1,9,1,1,1,1,9],
        [9,1,9,1,9,1,1,1,1,9],
        [9,1,1,1,9,1,1,1,1,9],
        [9,9,9,9,9,9,9,9,9,9]
    ]
    return myGrid

def display_grid(grid, start, end, path = None):
    grid_mapping = {
        9: 'X',
        1: '.',
        2: 'G',
        0.5: 'I'

    }
    path_set = set(tuple(p) for p in path) if path else set()
    for r in range(len(grid)):
            row_cells = []
            for c in range(len(grid[0])):
                pos = (r, c)
                
                if pos == tuple(start):
                    row_cells.append('S')
                elif pos == tuple(end):
                    row_cells.append('F')
                elif pos in path_set:
                    row_cells.append('*')
                else:
                    val = grid[r][c]
                    row_cells.append(grid_mapping.get(val, str(val)))
                    
            print(" ".join(row_cells))

def findPath(grid, start, end):
    rows = len(grid)
    cols = len(grid[0])

    directions=[(-1,0),(1,0),(0,-1),(0,1)]

    def heuristic(a,b):
        return abs(a[0]-b[0]) + abs(a[1]-b[1])

    tuple_start = tuple(start)
    tuple_end = tuple(end)
    pq = [(0+heuristic(tuple_start, tuple_end),0,tuple_start,[tuple_start])]
    min_costs = {tuple_start: 0}

    while pq:
        _, current_cost, current_pos, path = hpq.heappop(pq)
        if current_pos == tuple_end: return path, current_cost

        r,c = current_pos

        for dr, dc in directions:
            nr, nc = r+dr, c+dc
            if 0 <= nr < rows and 0 <= nc < cols:
                cell_val = grid[nr][nc]

                #wall
                if cell_val == 9:
                    continue
                step_cost = float(cell_val)
                new_cost = current_cost + step_cost
                neigh = (nr,nc)

                if neigh not in min_costs or new_cost < min_costs[neigh]:
                    min_costs[neigh] = new_cost
                    f_score = new_cost + heuristic(neigh,tuple_end)
                    hpq.heappush(pq,(f_score,new_cost,neigh,path + [neigh]))
    return None, float('inf')


grid = myGrid()
display_grid(grid, start_pos, end_pos)
print("Finding path for you...\n")
tm.sleep(2)

path, total_cost = findPath(grid, start_pos, end_pos)
display_grid(grid, start_pos, end_pos,path)

start_pos = [1, 1]
end_pos = [8, 8]



if path:
    print(f"Total Bobot Jalur Tercepat: {total_cost}")
    print("Koordinat Jalur:", path)
else:
    print("Jalur tidak ditemukan.")
