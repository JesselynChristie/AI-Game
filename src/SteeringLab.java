import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class SteeringLab extends JPanel implements ActionListener {
    private Timer timer;
    private Agent agent;
    private Vector2D target;
    private ArrayList<Vector2D> path;
    private int currentPathIndex = 0;
    
    private enum Mode { ARRIVE, WANDER, PATH_FOLLOWING }
    private Mode currentMode = Mode.ARRIVE;

    public SteeringLab() {
        this.setFocusable(true);
        this.setBackground(Color.DARK_GRAY);
        
        // Inisialisasi Agen
        agent = new Agent(400, 300);
        target = new Vector2D(400, 300);
        
        // Setup 5 Waypoint (Simulasi Rute A*)[cite: 1]
        path = new ArrayList<>();
        path.add(new Vector2D(100, 100));
        path.add(new Vector2D(700, 150));
        path.add(new Vector2D(650, 450));
        path.add(new Vector2D(200, 500));
        path.add(new Vector2D(400, 300)); 

        // Mouse Listener untuk memindahkan target di mode Arrive
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentMode == Mode.ARRIVE) {
                    target.set(e.getX(), e.getY());
                }
            }
        });

        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        agent.position.set(400, 300);
        agent.velocity.set(0, 0);
        currentPathIndex = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (currentMode == Mode.ARRIVE) {
            // Gambar target
            g2d.setColor(Color.RED);
            g2d.fillOval((int)target.x - 5, (int)target.y - 5, 10, 10);
            g2d.setColor(new Color(255, 0, 0, 50));
            // Slowing radius divisualisasikan
            g2d.drawOval((int)target.x - 100, (int)target.y - 100, 200, 200);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Klik untuk memindahkan target Arrive", 10, 20);
        } 
        else if (currentMode == Mode.PATH_FOLLOWING) {
            g2d.setColor(Color.CYAN);
            for (int i = 0; i < path.size(); i++) {
                Vector2D p = path.get(i);
                g2d.fillOval((int)p.x - 5, (int)p.y - 5, 10, 10);
                g2d.drawString("P" + i, (int)p.x + 10, (int)p.y);
                if (i < path.size() - 1) {
                    Vector2D next = path.get(i + 1);
                    g2d.drawLine((int)p.x, (int)p.y, (int)next.x, (int)next.y);
                }
            }
        }

        // Gambar Agen
        agent.draw(g2d, currentMode == Mode.WANDER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (currentMode) {
            case ARRIVE:
                // Uji 1: Tuning Arrive & Orbiting dengan parameter tinggi agar responsif[cite: 1]
                agent.arrive(target, 100); 
                break;
            case WANDER:
                // Uji 2: Wander Circle Gizmo, dimodifikasi agar gerak zigzag
                agent.wander();
                break;
            case PATH_FOLLOWING:
                // Uji 3: Path Following rute 5 waypoint dan deselerasi di akhir[cite: 1]
                if (currentPathIndex < path.size()) {
                    Vector2D currentTarget = path.get(currentPathIndex);
                    double dist = agent.position.distance(currentTarget);
                    
                    if (dist < 30) {
                        currentPathIndex++;
                    } else {
                        if (currentPathIndex == path.size() - 1) {
                            agent.arrive(currentTarget, 100); // Deselerasi di titik terakhir[cite: 1]
                        } else {
                            agent.seek(currentTarget); // Belokan mulus antar titik
                        }
                    }
                } else {
                    agent.velocity.multiply(0.9); // Berhenti setelah rute selesai
                }
                break;
        }

        agent.update();
        checkBorders();
        repaint();
    }

    private void checkBorders() {
        if (agent.position.x < 0) agent.position.x = getWidth();
        if (agent.position.x > getWidth()) agent.position.x = 0;
        if (agent.position.y < 0) agent.position.y = getHeight();
        if (agent.position.y > getHeight()) agent.position.y = 0;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Steering Behaviors Lab");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        
        SteeringLab panel = new SteeringLab();
        
        JPanel uiPanel = new JPanel();
        JButton btnArrive = new JButton("1. Arrive");
        JButton btnWander = new JButton("2. Wander (Zigzag)");
        JButton btnPath = new JButton("3. Path Following (Fake A*)");
        
        btnArrive.addActionListener(e -> panel.setMode(Mode.ARRIVE));
        btnWander.addActionListener(e -> panel.setMode(Mode.WANDER));
        btnPath.addActionListener(e -> panel.setMode(Mode.PATH_FOLLOWING));
        
        uiPanel.add(btnArrive);
        uiPanel.add(btnWander);
        uiPanel.add(btnPath);
        
        frame.add(uiPanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // --- KELAS AGEN ---
    class Agent {
        Vector2D position;
        Vector2D velocity;
        Vector2D acceleration;
        double maxSpeed = 10; // Dari nilai eksperimen[cite: 1]
        double maxForce = 1.5; // Menggunakan rasio dari parameter bereksperimen maxForce[cite: 1]
        
        // Parameter Wander
        double wanderTheta = 0;

        Agent(double x, double y) {
            position = new Vector2D(x, y);
            velocity = new Vector2D(0, -2);
            acceleration = new Vector2D(0, 0);
        }

        void update() {
            velocity.add(acceleration);
            velocity.limit(maxSpeed);
            position.add(velocity);
            acceleration.multiply(0);
        }

        void applyForce(Vector2D force) {
            acceleration.add(force);
        }

        void seek(Vector2D target) {
            Vector2D desired = Vector2D.subtract(target, position);
            desired.normalize();
            desired.multiply(maxSpeed);
            Vector2D steer = Vector2D.subtract(desired, velocity);
            steer.limit(maxForce);
            applyForce(steer);
        }

        void arrive(Vector2D target, double slowingRadius) {
            Vector2D desired = Vector2D.subtract(target, position);
            double d = desired.magnitude();
            desired.normalize();
            
            if (d < slowingRadius) {
                double m = (d / slowingRadius) * maxSpeed;
                desired.multiply(m);
            } else {
                desired.multiply(maxSpeed);
            }
            
            Vector2D steer = Vector2D.subtract(desired, velocity);
            steer.limit(maxForce);
            applyForce(steer);
        }

        void wander() {
            double wanderR = 50;         // Radius untuk wander circle gizmo[cite: 1]
            double wanderD = 100;        // Jarak gizmo dari agen
            double change = 1.5;         // Nilai perubahan drastis untuk efek zigzag

            wanderTheta += (Math.random() * 2 - 1) * change; 

            Vector2D circleCenter = velocity.copy();
            circleCenter.normalize();
            circleCenter.multiply(wanderD);
            circleCenter.add(position);

            double heading = velocity.heading();
            Vector2D circleOffset = new Vector2D(
                wanderR * Math.cos(wanderTheta + heading),
                wanderR * Math.sin(wanderTheta + heading)
            );

            Vector2D target = Vector2D.add(circleCenter, circleOffset);
            seek(target);
        }

        void draw(Graphics2D g2d, boolean drawGizmo) {
            double theta = velocity.heading();
            
            if (drawGizmo) {
                // Menggambar Wander Circle Gizmo[cite: 1]
                Vector2D circleCenter = velocity.copy();
                circleCenter.normalize();
                circleCenter.multiply(100);
                circleCenter.add(position);
                g2d.setColor(new Color(0, 255, 0, 100));
                g2d.drawOval((int)circleCenter.x - 50, (int)circleCenter.y - 50, 100, 100);
            }

            g2d.setColor(Color.YELLOW);
            AffineTransform old = g2d.getTransform();
            g2d.translate(position.x, position.y);
            g2d.rotate(theta);
            
            // Gambar segitiga
            Polygon p = new Polygon();
            p.addPoint(15, 0);
            p.addPoint(-10, -10);
            p.addPoint(-10, 10);
            g2d.fill(p);
            
            g2d.setTransform(old);
        }
    }

    // --- HELPER VECTOR2D ---
    static class Vector2D {
        double x, y;

        Vector2D(double x, double y) { this.x = x; this.y = y; }
        
        void set(double x, double y) { this.x = x; this.y = y; }
        
        void add(Vector2D v) { x += v.x; y += v.y; }
        
        static Vector2D add(Vector2D v1, Vector2D v2) { return new Vector2D(v1.x + v2.x, v1.y + v2.y); }
        
        static Vector2D subtract(Vector2D v1, Vector2D v2) { return new Vector2D(v1.x - v2.x, v1.y - v2.y); }
        
        void multiply(double n) { x *= n; y *= n; }
        
        double magnitude() { return Math.sqrt(x * x + y * y); }
        
        void normalize() {
            double m = magnitude();
            if (m != 0 && m != 1) { multiply(1 / m); }
        }
        
        void limit(double max) {
            if (magnitude() > max) { normalize(); multiply(max); }
        }
        
        double distance(Vector2D v) {
            double dx = x - v.x; double dy = y - v.y;
            return Math.sqrt(dx * dx + dy * dy);
        }
        
        double heading() { return Math.atan2(y, x); }
        
        Vector2D copy() { return new Vector2D(x, y); }
    }
}