
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class App extends JFrame {

    static final Color BG = new Color(15, 20, 31);
    static final Color PANEL = new Color(27, 35, 49);
    static final Color ACCENT = new Color(62, 190, 238);
    static final Color TEXT = new Color(233, 240, 247);

    static final Random RAND = new Random();

    static JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(TEXT);
        return l;
    }

    static JPanel vertical() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    static JPanel controls(String title) {
        JPanel p = vertical();
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.setBackground(PANEL);
        p.setOpaque(true);
        p.setPreferredSize(new Dimension(245, 600));

        JLabel t = label(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 17f));
        p.add(t);
        p.add(Box.createVerticalStrut(14));
        return p;
    }

    static JSlider slider(
            JPanel target, String name,
            int min, int max, int value) {

        JLabel l = label(name + ": " + value);
        target.add(l);

        JSlider s = new JSlider(min, max, value);
        s.setOpaque(false);
        s.addChangeListener(e ->
                l.setText(name + ": " + s.getValue()));

        target.add(s);
        target.add(Box.createVerticalStrut(9));
        return s;
    }

    static JButton button(String name, Runnable r) {
        JButton b = new JButton(name);
        b.setFocusPainted(false);
        b.addActionListener(e -> r.run());
        return b;
    }

    static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    static class Boid {
        double x, y, vx, vy;

        Boid(double x, double y) {
            this.x = x;
            this.y = y;

            double a = RAND.nextDouble() * Math.PI * 2;
            vx = Math.cos(a) * 1.7;
            vy = Math.sin(a) * 1.7;
        }
    }

    abstract static class Canvas extends JPanel {
        long last = System.nanoTime();
        double fps = 60;

        Canvas() {
            setBackground(BG);

            new javax.swing.Timer(16, e -> {
                if (!isShowing()) {
                    last = System.nanoTime();
                    return;
                }

                long n = System.nanoTime();
                double dt = (n - last) / 1e6;
                last = n;

                if (dt > 0) {
                    fps = fps * .9 + 1000 / dt * .1;
                }

                tick();
                repaint();
            }).start();
        }

        abstract void tick();

        void stats(Graphics2D g, String s) {
            g.setColor(TEXT);
            g.setFont(new Font(
                    "SansSerif", Font.PLAIN, 13));
            g.drawString(s, 18, 24);
        }
    }

    // ====================================
    // LAB 1 & LAB 2 - BOIDS SIMULATION
    // ====================================

    static class Flock extends Canvas {

        final List<Boid> boids = new ArrayList<>();

        double ws = 1.3;
        double wa = .7;
        double wc = .5;
        double rSep = 18;
        double rNeigh = 65;

        boolean run = true;
        boolean hash = false;

        int desired = 55;
        long checks = 0;
        double cpuMs = 0;

        Flock(int count) {
            desired = count;
            reset();
        }

        void reset() {
            boids.clear();

            for (int i = 0; i < desired; i++) {
                boids.add(new Boid(
                        40 + RAND.nextDouble() * 780,
                        60 + RAND.nextDouble() * 460
                ));
            }
        }

        static long key(int x, int y) {
            return ((long) x << 32)
                    ^ (y & 0xffffffffL);
        }

        void step(int w, int h) {
            long start = System.nanoTime();
            checks = 0;

            int n = boids.size();
            double[] ax = new double[n];
            double[] ay = new double[n];

            Map<Long, List<Integer>> cells = null;
            int size = (int) Math.max(1, rNeigh);

            if (hash) {
                cells = new HashMap<>();

                for (int i = 0; i < n; i++) {
                    Boid b = boids.get(i);

                    long k = key(
                            (int) (b.x / size),
                            (int) (b.y / size)
                    );

                    cells.computeIfAbsent(
                            k, z -> new ArrayList<>()
                    ).add(i);
                }
            }

            for (int i = 0; i < n; i++) {

                Boid b = boids.get(i);

                double sx = 0, sy = 0;
                double avx = 0, avy = 0;
                double cx = 0, cy = 0;
                int near = 0;

                Iterable<Integer> candidates;

                if (hash) {
                    List<Integer> indices =
                            new ArrayList<>();

                    int gx = (int) (b.x / size);
                    int gy = (int) (b.y / size);

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {

                            List<Integer> bucket =
                                    cells.get(key(
                                            gx + dx,
                                            gy + dy
                                    ));

                            if (bucket != null) {
                                indices.addAll(bucket);
                            }
                        }
                    }

                    candidates = indices;

                } else {
                    List<Integer> indices =
                            new ArrayList<>(n);

                    for (int j = 0; j < n; j++) {
                        indices.add(j);
                    }

                    candidates = indices;
                }

                for (int j : candidates) {
                    if (j == i) continue;

                    checks++;

                    Boid o = boids.get(j);

                    double dx = b.x - o.x;
                    double dy = b.y - o.y;
                    double d2 = dx * dx + dy * dy;

                    if (d2 < rNeigh * rNeigh
                            && d2 > 1e-8) {

                        near++;
                        avx += o.vx;
                        avy += o.vy;
                        cx += o.x;
                        cy += o.y;

                        if (d2 < rSep * rSep) {
                            sx += dx / d2;
                            sy += dy / d2;
                        }
                    }
                }

                if (near > 0) {
                    ax[i] =
                            ws * sx
                            + wa * (avx / near - b.vx) * .12
                            + wc * (cx / near - b.x) * .01;

                    ay[i] =
                            ws * sy
                            + wa * (avy / near - b.vy) * .12
                            + wc * (cy / near - b.y) * .01;
                }
            }

            for (int i = 0; i < n; i++) {

                Boid b = boids.get(i);

                b.vx += clamp(ax[i], -.15, .15);
                b.vy += clamp(ay[i], -.15, .15);

                double v = Math.hypot(b.vx, b.vy);

                if (v < .1) {
                    b.vx = 1;
                    b.vy = 0;
                    v = 1;
                }

                double target = 2.1;

                b.vx = b.vx / v * target;
                b.vy = b.vy / v * target;

                b.x += b.vx;
                b.y += b.vy;

                if (b.x < 8) {
                    b.x = 8;
                    b.vx = Math.abs(b.vx);
                }

                if (b.x > w - 8) {
                    b.x = w - 8;
                    b.vx = -Math.abs(b.vx);
                }

                if (b.y < 40) {
                    b.y = 40;
                    b.vy = Math.abs(b.vy);
                }

                if (b.y > h - 8) {
                    b.y = h - 8;
                    b.vy = -Math.abs(b.vy);
                }
            }

            cpuMs = (System.nanoTime() - start) / 1e6;
        }

        @Override
        void tick() {
            if (run && getWidth() > 40
                    && getHeight() > 60) {

                step(getWidth(), getHeight());
            }
        }

        @Override
        protected void paintComponent(Graphics gg) {
            super.paintComponent(gg);

            Graphics2D g = (Graphics2D) gg.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(ACCENT);

            for (Boid b : boids) {

                AffineTransform old = g.getTransform();

                g.translate(b.x, b.y);
                g.rotate(
                        Math.atan2(b.vy, b.vx)
                        + Math.PI / 2
                );

                Path2D p = new Path2D.Double();
                p.moveTo(0, -6);
                p.lineTo(-4, 5);
                p.lineTo(4, 5);
                p.closePath();

                g.fill(p);
                g.setTransform(old);
            }

            stats(g,
                    boids.size() + " boids"
                    + "  |  " + (hash
                            ? "Spatial Hash Grid"
                            : "Brute Force")
                    + "  |  FPS "
                    + String.format("%.0f", fps)
                    + "  |  CPU "
                    + String.format("%.2f", cpuMs)
                    + " ms  |  Checks " + checks
            );

            g.dispose();
        }
    }

    // ====================================
    // LAB 1 & LAB 2 - USER INTERFACE
    // ====================================

    JPanel flockTab(boolean benchmark) {

        Flock sim = new Flock(benchmark ? 2000 : 55);

        JPanel side = controls(
                benchmark
                        ? "LAB 2 - Spatial Grid"
                        : "LAB 1 - Boids Sandbox"
        );

        if (!benchmark) {

            JSlider count = slider(
                    side, "Agents", 10, 250, 55);

            count.addChangeListener(e -> {
                sim.desired = count.getValue();
                sim.reset();
            });

            JSlider ws = slider(
                    side, "Ws - Separation", 0, 300, 130);

            JSlider wa = slider(
                    side, "Wa - Alignment", 0, 300, 70);

            JSlider wc = slider(
                    side, "Wc - Cohesion", 0, 300, 50);

            ws.addChangeListener(e ->
                    sim.ws = ws.getValue() / 100.0);

            wa.addChangeListener(e ->
                    sim.wa = wa.getValue() / 100.0);

            wc.addChangeListener(e ->
                    sim.wc = wc.getValue() / 100.0);

            JSlider sep = slider(
                    side, "r_sep", 8, 60, 18);

            JSlider neigh = slider(
                    side, "r_neigh", 20, 140, 65);

            sep.addChangeListener(e ->
                    sim.rSep = sep.getValue());

            neigh.addChangeListener(e ->
                    sim.rNeigh = neigh.getValue());

            side.add(label("Pantau stabil vs runtuh"));

        } else {

            JComboBox<String> mode =
                    new JComboBox<>(new String[]{
                            "Brute Force O(N^2)",
                            "Spatial Hash Grid"
                    });

            mode.addActionListener(e ->
                    sim.hash =
                            mode.getSelectedIndex() == 1);

            side.add(label(
                    "Metode pencarian tetangga"));

            side.add(mode);
            side.add(Box.createVerticalStrut(12));

            side.add(label("2000 boids simultan"));
            side.add(Box.createVerticalStrut(12));

            side.add(label("Bandingkan CPU ms / FPS"));
            side.add(Box.createVerticalStrut(12));

            side.add(label("Grid: pencarian 9 cell"));
            side.add(Box.createVerticalStrut(12));

            side.add(label(
                    "Kinerja bergantung kepadatan"));
        }

        side.add(Box.createVerticalStrut(20));

        side.add(button("Pause / Resume",
                () -> sim.run = !sim.run));

        side.add(Box.createVerticalStrut(9));

        side.add(button("Reset", sim::reset));

        return layout(side, sim);
    }

    // ====================================
    // LAB 3 - TACTICAL SQUAD FORMATION
    // ====================================

    static class Squad extends Canvas {

        static class Unit {
            double x, y;

            Unit(double x, double y) {
                this.x = x;
                this.y = y;
            }
        }

        List<Unit> units = new ArrayList<>();

        double leaderX = 430;
        double leaderY = 480;
        double speed = 1.5;
        double formationError = 0;

        boolean run = true;
        String state = "WEDGE";

        Squad() {
            reset();
        }

        void reset() {
            units.clear();

            leaderX = 430;
            leaderY = 480;

            for (int i = 0; i < 4; i++) {

                units.add(new Unit(
                        leaderX
                        + (i % 2 == 0 ? -1 : 1) * 35,

                        leaderY + 45 + (i / 2) * 40
                ));
            }
        }

        @Override
        void tick() {
            if (!run) return;

            int w = Math.max(600, getWidth());
            int h = Math.max(450, getHeight());

            double wallY = h * .49;
            double gapX = w * .55;
            double gapHalf = 48;

            leaderX = gapX;
            leaderY -= speed;

            if (leaderY < 25) {
                leaderY = h - 25;

                for (Unit u : units) {
                    u.y += h - 50;
                }
            }

            boolean choke =
                    Math.abs(leaderY - wallY) < 125;

            state = choke
                    ? "CHOKE - COLUMN"
                    : "WEDGE";

            double err = 0;

            for (int i = 0; i < units.size(); i++) {

                Unit u = units.get(i);

                int rank = i / 2 + 1;
                int sign = i % 2 == 0 ? -1 : 1;

                double targetX = choke
                        ? leaderX
                        : leaderX + sign * rank * 30;

                double targetY = leaderY
                        + (choke
                        ? (i + 1) * 23
                        : rank * 30);

                double dx = targetX - u.x;
                double dy = targetY - u.y;

                double vx = clamp(
                        dx * .09,
                        -speed * 2,
                        speed * 2
                );

                double vy = clamp(
                        dy * .09,
                        -speed * 2,
                        speed * 2
                );

                u.x += vx;
                u.y += vy;

                // Collision sederhana dengan dinding
                if (Math.abs(u.y - wallY) < 9
                        && (u.x < gapX - gapHalf + 9
                        || u.x > gapX + gapHalf - 9)) {

                    u.y = wallY
                            + (u.y > wallY ? 9 : -9);
                }

                err += Math.hypot(dx, dy);
            }

            formationError = err / 4;
        }

        @Override
        protected void paintComponent(Graphics gg) {
            super.paintComponent(gg);

            Graphics2D g = (Graphics2D) gg.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = getWidth();
            int h = getHeight();

            int yy = (int) (h * .49);
            int cx = (int) (w * .55);

            // Dinding dengan celah tengah
            g.setColor(new Color(92, 105, 124));

            g.fillRoundRect(
                    0, yy - 12,
                    cx - 48, 24, 5, 5
            );

            g.fillRoundRect(
                    cx + 48, yy - 12,
                    w - cx - 48, 24, 5, 5
            );

            // Area choke
            g.setColor(new Color(30, 180, 140));
            g.setStroke(new BasicStroke(2));

            g.drawRect(cx - 48, yy - 12, 96, 24);

            // Leader
            g.setColor(new Color(248, 173, 55));

            g.fillOval(
                    (int) leaderX - 10,
                    (int) leaderY - 10,
                    20, 20
            );

            // Anggota squad
            g.setColor(new Color(38, 215, 159));

            for (Unit u : units) {
                g.fillOval(
                        (int) u.x - 8,
                        (int) u.y - 8,
                        16, 16
                );
            }

            stats(g,
                    "Leader + 4 units"
                    + "  |  " + state
                    + "  |  Error "
                    + String.format("%.1f",
                            formationError)
                    + " px  |  FPS "
                    + String.format("%.0f", fps)
            );

            g.dispose();
        }
    }

    JPanel squadTab() {

        Squad s = new Squad();

        JPanel side = controls(
                "LAB 3 - Squad Formation"
        );

        side.add(label("Leader + 4 anggota"));
        side.add(Box.createVerticalStrut(12));

        side.add(label("Wedge > Choke > Wedge"));
        side.add(Box.createVerticalStrut(12));

        side.add(label("Auto re-assign formasi"));
        side.add(Box.createVerticalStrut(20));

        JSlider speed = slider(
                side, "Speed", 1, 5, 2);

        speed.addChangeListener(e ->
                s.speed = speed.getValue());

        side.add(button(
                "Pause / Resume",
                () -> s.run = !s.run
        ));

        side.add(Box.createVerticalStrut(10));

        side.add(button("Reset", s::reset));
        side.add(Box.createVerticalStrut(20));

        side.add(label(
                "Amati celah dan error posisi"));

        return layout(side, s);
    }

    // ====================================
    // MAIN APPLICATION
    // ====================================

    JPanel layout(JPanel side, JPanel canvas) {

        JPanel p = new JPanel(
                new BorderLayout(0, 0)
        );

        p.add(side, BorderLayout.WEST);
        p.add(canvas, BorderLayout.CENTER);

        return p;
    }

    public App() {

        super("AI for Games - Multi-Agent Lab");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setSize(1120, 720);

        setMinimumSize(
                new Dimension(840, 540)
        );

        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab(
                "01 - Boids Tuning",
                flockTab(false)
        );

        tabs.addTab(
                "02 - Spatial Grid (2000)",
                flockTab(true)
        );

        tabs.addTab(
                "03 - Squad Formation",
                squadTab()
        );

        setContentPane(tabs);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() ->
                new App().setVisible(true)
        );
    }
}
