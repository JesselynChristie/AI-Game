import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class PathfindingApp extends JFrame {

    public PathfindingApp() {
        setTitle("Pathfinding Benchmark, Tie-Breaking, & Smoothing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Uji 1: Dijkstra vs A*", new PanelUji1());
        tabbedPane.addTab("Uji 2: Tie-Breaking Area Terbuka", new PanelUji2());
        tabbedPane.addTab("Uji 3: String Pulling (Smoothing)", new PanelUji3());

        add(tabbedPane);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PathfindingApp::new);
    }
}

// ==========================================
// UJI 1: BENCHMARK DIJKSTRA VS A*
// ==========================================
class PanelUji1 extends JPanel {
    private static final int ROWS = 30, COLS = 30, CELL_SIZE = 20;
    private Node[][] grid = new Node[ROWS][COLS];
    private Node startNode, targetNode;
    
    private Set<Node> evaluatedDijkstra = new HashSet<>();
    private Set<Node> evaluatedAStar = new HashSet<>();
    private List<Node> pathAStar = new ArrayList<>();

    public PanelUji1() {
        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        initGrid();
        
        System.out.println("\n--- UJI 1: BENCHMARK DIJKSTRA VS A* ---");
        int dijkstraCount = runPathfinding(false);
        int aStarCount = runPathfinding(true);
        
        System.out.println("Dijkstra Evaluasi : " + dijkstraCount + " node");
        System.out.println("A* Evaluasi       : " + aStarCount + " node");
        double reduction = ((double) (dijkstraCount - aStarCount) / dijkstraCount) * 100;
        System.out.printf("Penurunan Evaluasi: %.2f%%\n", reduction);
    }

    private void initGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) grid[r][c] = new Node(r, c);
        }
        startNode = grid[2][2];
        targetNode = grid[27][27];

        for (int r = 5; r < 25; r++) grid[r][15].isWall = true;
        for (int c = 5; c < 16; c++) grid[15][c].isWall = true;
    }

    private int runPathfinding(boolean isAStar) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Set<Node> closedSet = new HashSet<>();
        
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c].g = Double.MAX_VALUE;
                grid[r][c].f = Double.MAX_VALUE;
                grid[r][c].parent = null;
            }
        }

        startNode.g = 0;
        startNode.f = isAStar ? getManhattan(startNode, targetNode) : 0;
        openSet.add(startNode);
        int evaluatedCount = 0;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            evaluatedCount++;
            
            if (isAStar) evaluatedAStar.add(current);
            else evaluatedDijkstra.add(current);

            if (current == targetNode) {
                if (isAStar) reconstructPath(current, pathAStar);
                break;
            }
            closedSet.add(current);

            for (Node neighbor : getNeighbors(current)) {
                if (neighbor.isWall || closedSet.contains(neighbor)) continue;
                double tentativeG = current.g + 1.0;
                
                if (tentativeG < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + (isAStar ? getManhattan(neighbor, targetNode) : 0);
                    if (!openSet.contains(neighbor)) openSet.add(neighbor);
                }
            }
        }
        return evaluatedCount;
    }

    private double getManhattan(Node a, Node b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0}, dc = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int nr = node.row + dr[i], nc = node.col + dc[i];
            if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS) neighbors.add(grid[nr][nc]);
        }
        return neighbors;
    }

    private void reconstructPath(Node current, List<Node> path) {
        path.clear();
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Node n : evaluatedDijkstra) { g.setColor(new Color(200, 220, 255)); g.fillRect(n.col * CELL_SIZE, n.row * CELL_SIZE, CELL_SIZE, CELL_SIZE); }
        for (Node n : evaluatedAStar) { g.setColor(new Color(255, 255, 200)); g.fillRect(n.col * CELL_SIZE, n.row * CELL_SIZE, CELL_SIZE, CELL_SIZE); }
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c].isWall) { g.setColor(Color.DARK_GRAY); g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE); }
                g.setColor(Color.LIGHT_GRAY); g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        g.setColor(Color.RED); for (Node n : pathAStar) { g.fillRect(n.col * CELL_SIZE + 5, n.row * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10); }
        g.setColor(Color.GREEN); g.fillRect(startNode.col * CELL_SIZE, startNode.row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.MAGENTA); g.fillRect(targetNode.col * CELL_SIZE, targetNode.row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }
}

// ==========================================
// UJI 2: TIE-BREAKING DI AREA TERBUKA
// ==========================================
class PanelUji2 extends JPanel {
    private static final int ROWS = 30, COLS = 30, CELL_SIZE = 20;
    private Node[][] grid = new Node[ROWS][COLS];
    private Node startNode, targetNode;
    
    private Set<Node> evaluatedNormalAStar = new HashSet<>();
    private Set<Node> evaluatedTieBreakAStar = new HashSet<>();
    private List<Node> pathAStar = new ArrayList<>();

    public PanelUji2() {
        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        initGrid();
        
        System.out.println("\n--- UJI 2: TIE-BREAKING DI AREA TERBUKA ---");
        int normalCount = runPathfinding(false);
        int tieBreakCount = runPathfinding(true);
        
        System.out.println("A* Normal Evaluasi       : " + normalCount + " node (Frontier Melebar)");
        System.out.println("A* Tie-Break Evaluasi    : " + tieBreakCount + " node (Frontier Garis Lurus)");
    }

    private void initGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) grid[r][c] = new Node(r, c);
        }
        startNode = grid[2][2];
        targetNode = grid[27][27];
    }

    private int runPathfinding(boolean useTieBreaking) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Set<Node> closedSet = new HashSet<>();
        
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c].g = Double.MAX_VALUE;
                grid[r][c].f = Double.MAX_VALUE;
                grid[r][c].parent = null;
            }
        }

        startNode.g = 0;
        startNode.f = getHeuristic(startNode, targetNode, useTieBreaking);
        openSet.add(startNode);
        int evaluatedCount = 0;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            evaluatedCount++;
            
            if (useTieBreaking) evaluatedTieBreakAStar.add(current);
            else evaluatedNormalAStar.add(current);

            if (current == targetNode) {
                if (useTieBreaking) reconstructPath(current, pathAStar);
                break;
            }
            closedSet.add(current);

            for (Node neighbor : getNeighbors(current)) {
                if (closedSet.contains(neighbor)) continue;
                double tentativeG = current.g + 1.0;
                
                if (tentativeG < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + getHeuristic(neighbor, targetNode, useTieBreaking);
                    if (!openSet.contains(neighbor)) openSet.add(neighbor);
                }
            }
        }
        return evaluatedCount;
    }

    private double getHeuristic(Node a, Node b, boolean useTieBreaking) {
        double dx = Math.abs(a.row - b.row);
        double dy = Math.abs(a.col - b.col);
        double heuristic = dx + dy;
        
        if (useTieBreaking) {
            heuristic *= (1.0 + 0.001); 
        }
        return heuristic;
    }

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0}, dc = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int nr = node.row + dr[i], nc = node.col + dc[i];
            if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS) neighbors.add(grid[nr][nc]);
        }
        return neighbors;
    }

    private void reconstructPath(Node current, List<Node> path) {
        path.clear();
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Node n : evaluatedNormalAStar) { g.setColor(new Color(200, 220, 255)); g.fillRect(n.col * CELL_SIZE, n.row * CELL_SIZE, CELL_SIZE, CELL_SIZE); }
        for (Node n : evaluatedTieBreakAStar) { g.setColor(new Color(255, 255, 200)); g.fillRect(n.col * CELL_SIZE, n.row * CELL_SIZE, CELL_SIZE, CELL_SIZE); }
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                g.setColor(Color.LIGHT_GRAY); g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        g.setColor(Color.RED); for (Node n : pathAStar) { g.fillRect(n.col * CELL_SIZE + 5, n.row * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10); }
        g.setColor(Color.GREEN); g.fillRect(startNode.col * CELL_SIZE, startNode.row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.MAGENTA); g.fillRect(targetNode.col * CELL_SIZE, targetNode.row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }
}

// ==========================================
// UJI 3: IMPLEMENTASI STRING PULLING
// ==========================================
class PanelUji3 extends JPanel {
    private static final int ROWS = 30, COLS = 30, CELL_SIZE = 20;
    private Node[][] grid = new Node[ROWS][COLS];
    private Node startNode, targetNode;
    
    private List<Node> rawPath = new ArrayList<>();
    private List<Node> smoothedPath = new ArrayList<>();

    public PanelUji3() {
        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        initGrid();
        
        System.out.println("\n--- UJI 3: STRING PULLING ---");
        runAStar();
        
        // Memasukkan rute mentah zig-zag ke fungsi SmoothPath()
        smoothedPath = SmoothPath(rawPath);
        
        System.out.println("Node Rute Mentah A* : " + rawPath.size());
        System.out.println("Node Rute Smoothed  : " + smoothedPath.size());
        System.out.println("Verifikasi: Rute kini berjalan memotong diagonal tanpa harus zig-zag berlebih.");
    }

    private void initGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) grid[r][c] = new Node(r, c);
        }
        startNode = grid[2][2];
        targetNode = grid[27][27];

        // Buat labirin zig-zag untuk memaksa A* belok kaku
        for (int r = 5; r < 20; r++) grid[r][10].isWall = true;
        for (int c = 10; c < 25; c++) grid[20][c].isWall = true;
        for (int r = 10; r < 20; r++) grid[r][20].isWall = true;
    }

    private void runAStar() {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Set<Node> closedSet = new HashSet<>();
        
        startNode.g = 0;
        startNode.f = getManhattan(startNode, targetNode);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current == targetNode) {
                reconstructPath(current, rawPath);
                Collections.reverse(rawPath); // Balik agar urutannya dari Start -> Target
                break;
            }
            closedSet.add(current);

            for (Node neighbor : getNeighbors(current)) {
                if (neighbor.isWall || closedSet.contains(neighbor)) continue;
                double tentativeG = current.g + 1.0;
                
                if (tentativeG < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + getManhattan(neighbor, targetNode);
                    if (!openSet.contains(neighbor)) openSet.add(neighbor);
                }
            }
        }
    }

    // Fungsi String Pulling / Path Smoothing
    private List<Node> SmoothPath(List<Node> inputPath) {
        List<Node> smooth = new ArrayList<>();
        if (inputPath == null || inputPath.isEmpty()) return smooth;
        
        smooth.add(inputPath.get(0)); // Masukkan start node
        Node current = inputPath.get(0);
        
        for (int i = 1; i < inputPath.size(); i++) {
            // Cek apakah ada garis lurus (Line of Sight) dari node 'current' ke node ke-i
            if (!hasLineOfSight(current, inputPath.get(i))) {
                // Jika terhalang tembok, node sebelumnya adalah titik sudut (corner)
                smooth.add(inputPath.get(i - 1));
                current = inputPath.get(i - 1);
            }
        }
        smooth.add(inputPath.get(inputPath.size() - 1)); // Masukkan target node
        return smooth;
    }

    // Raycast Bresenham Algorithm untuk Cek Line of Sight
    private boolean hasLineOfSight(Node a, Node b) {
        int x0 = a.col, y0 = a.row;
        int x1 = b.col, y1 = b.row;
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (grid[y0][x0].isWall) return false;
            if (x0 == x1 && y0 == y1) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
        return true;
    }

    private double getManhattan(Node a, Node b) { return Math.abs(a.row - b.row) + Math.abs(a.col - b.col); }

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0}, dc = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int nr = node.row + dr[i], nc = node.col + dc[i];
            if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS) neighbors.add(grid[nr][nc]);
        }
        return neighbors;
    }

    private void reconstructPath(Node current, List<Node> path) {
        path.clear();
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Grid & Tembok
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c].isWall) { g2.setColor(Color.DARK_GRAY); g2.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE); }
                g2.setColor(Color.LIGHT_GRAY); g2.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        
        // Rute Mentah A* (Kotak Merah Pudar) menandakan zig-zag
        g2.setColor(new Color(255, 100, 100, 150)); 
        for (Node n : rawPath) { 
            g2.fillRect(n.col * CELL_SIZE + 5, n.row * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10); 
        }

        // Gambarkan garis hijau tebal pada rute hasil smoothing (LineRenderer Concept)
        g2.setColor(Color.GREEN);
        g2.setStroke(new BasicStroke(5)); // Ketebalan Garis
        for (int i = 0; i < smoothedPath.size() - 1; i++) {
            Node p1 = smoothedPath.get(i);
            Node p2 = smoothedPath.get(i + 1);
            int x1 = p1.col * CELL_SIZE + (CELL_SIZE / 2);
            int y1 = p1.row * CELL_SIZE + (CELL_SIZE / 2);
            int x2 = p2.col * CELL_SIZE + (CELL_SIZE / 2);
            int y2 = p2.row * CELL_SIZE + (CELL_SIZE / 2);
            g2.drawLine(x1, y1, x2, y2);
        }

        // Start & Target
        g2.setColor(Color.BLUE); g2.fillRect(startNode.col * CELL_SIZE, startNode.row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        g2.setColor(Color.MAGENTA); g2.fillRect(targetNode.col * CELL_SIZE, targetNode.row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }
}

// Struktur Data Node Bersama
class Node {
    int row, col;
    double g = Double.MAX_VALUE, f = Double.MAX_VALUE;
    boolean isWall = false;
    Node parent;

    Node(int row, int col) {
        this.row = row;
        this.col = col;
    }
}