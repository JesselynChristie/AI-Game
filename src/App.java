
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class App extends JFrame {

    // Ukuran grid sesuai PPT
    final int BARIS = 10;
    final int KOLOM = 10;

    // Isi kotak
    final int JALAN = 0;
    final int TEMBOK = 1;
    final int RAWA = 2;
    final int ASPAL = 3;

    int[][] map = new int[BARIS][KOLOM];
    JLabel[][] kotak = new JLabel[BARIS][KOLOM];

    int startBaris;
    int startKolom;
    int goalBaris;
    int goalKolom;

    int ujiAktif = 1;

    ArrayList<Point> jalur = new ArrayList<Point>();

    JLabel lblInfo;
    JLabel lblHasil;

    public App() {

        setTitle("Tugas 1 - AI in Games");
        setSize(820, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelUtama = new JPanel(new BorderLayout(10, 10));
        panelUtama.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Judul
        JLabel judul = new JLabel("Tugas 1 - AI in Games", SwingConstants.CENTER);
        judul.setFont(new Font("Arial", Font.BOLD, 22));
        panelUtama.add(judul, BorderLayout.NORTH);

        // Grid 10x10
        JPanel panelGrid = new JPanel(new GridLayout(BARIS, KOLOM, 2, 2));
        panelGrid.setBackground(Color.DARK_GRAY);
        panelGrid.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        for (int i = 0; i < BARIS; i++) {
            for (int j = 0; j < KOLOM; j++) {

                kotak[i][j] = new JLabel("", SwingConstants.CENTER);
                kotak[i][j].setOpaque(true);
                kotak[i][j].setBackground(Color.WHITE);
                kotak[i][j].setFont(new Font("Arial", Font.BOLD, 14));

                panelGrid.add(kotak[i][j]);
            }
        }

        panelUtama.add(panelGrid, BorderLayout.CENTER);

        // Panel bawah
        JPanel panelBawah = new JPanel();
        panelBawah.setLayout(new BoxLayout(panelBawah, BoxLayout.Y_AXIS));

        JPanel panelTombol = new JPanel();

        JButton btnUji1 = new JButton("Uji 1");
        JButton btnUji2 = new JButton("Uji 2");
        JButton btnUji3 = new JButton("Uji 3");
        JButton btnJalankan = new JButton("Jalankan Dijkstra");

        panelTombol.add(btnUji1);
        panelTombol.add(btnUji2);
        panelTombol.add(btnUji3);
        panelTombol.add(btnJalankan);

        panelBawah.add(panelTombol);

        lblInfo = new JLabel(" ", SwingConstants.CENTER);
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblHasil = new JLabel(" ", SwingConstants.CENTER);
        lblHasil.setFont(new Font("Arial", Font.BOLD, 14));
        lblHasil.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelBawah.add(lblInfo);
        panelBawah.add(Box.createVerticalStrut(5));
        panelBawah.add(lblHasil);

        JLabel legend = new JLabel(
                "Hijau = Start | Merah = Goal | Kuning = Jalur | Abu tua = Tembok | Biru = Rawa (5) | Abu muda = Aspal (1)",
                SwingConstants.CENTER
        );
        legend.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelBawah.add(Box.createVerticalStrut(8));
        panelBawah.add(legend);

        panelUtama.add(panelBawah, BorderLayout.SOUTH);

        add(panelUtama);

        // Tombol Uji 1
        btnUji1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buatUji1();
            }
        });

        // Tombol Uji 2
        btnUji2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buatUji2();
            }
        });

        // Tombol Uji 3
        btnUji3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buatUji3();
            }
        });

        // Tombol Dijkstra
        btnJalankan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dijkstra();
            }
        });

        buatUji1();
    }

    // Mengosongkan seluruh map
    void kosongkanMap() {

        for (int i = 0; i < BARIS; i++) {
            for (int j = 0; j < KOLOM; j++) {
                map[i][j] = JALAN;
            }
        }

        jalur.clear();
        lblHasil.setText(" ");
    }

    // uji 1
    void buatUji1() {

        kosongkanMap();
        ujiAktif = 1;

        // Sesuai PPT
        startBaris = 1;
        startKolom = 1;

        goalBaris = 8;
        goalKolom = 8;

        lblInfo.setText(
                "Uji 1 - Grid polos | Start (1,1) | Goal (8,8) | Lurus = 1 | Diagonal = 1.414"
        );

        tampilkanMap();
    }

    // uji 2
    void buatUji2() {

        kosongkanMap();
        ujiAktif = 2;

        // Start berada di dalam ceruk U
        startBaris = 6;
        startKolom = 4;

        // Goal berada di luar U
        goalBaris = 1;
        goalKolom = 8;

        // Tembok kiri
        for (int i = 3; i <= 8; i++) {
            map[i][2] = TEMBOK;
        }

        // Tembok kanan
        for (int i = 3; i <= 8; i++) {
            map[i][6] = TEMBOK;
        }

        // Tembok bawah
        for (int j = 2; j <= 6; j++) {
            map[8][j] = TEMBOK;
        }

        lblInfo.setText(
                "Uji 2 - Tembok U-Shaped | Bot harus keluar dari ceruk dan menuju Goal"
        );

        tampilkanMap();
    }

    //uji 3
    void buatUji3() {

        kosongkanMap();
        ujiAktif = 3;

        startBaris = 5;
        startKolom = 1;

        goalBaris = 5;
        goalKolom = 8;

        // Jalur lurus dibuat rawa
        // Rawa memiliki Cost = 5 sesuai PPT
        for (int j = 2; j <= 7; j++) {
            map[5][j] = RAWA;
        }

        // Jalur memutar dibuat ASPAL
        // Aspal memiliki Cost = 1 sesuai PPT
        for (int j = 1; j <= 8; j++) {
            map[4][j] = ASPAL;
        }

        lblInfo.setText(
                "Uji 3 - Biru = Rawa (5) | Abu muda = Aspal (1) | Cari biaya paling rendah"
        );

        tampilkanMap();
    }

    // Menampilkan grid
    void tampilkanMap() {

        for (int i = 0; i < BARIS; i++) {
            for (int j = 0; j < KOLOM; j++) {

                kotak[i][j].setText("");
                kotak[i][j].setForeground(Color.BLACK);

                if (map[i][j] == JALAN) {
                    kotak[i][j].setBackground(Color.WHITE);
                }

                if (map[i][j] == TEMBOK) {
                    kotak[i][j].setBackground(Color.GRAY);
                    kotak[i][j].setText("X");
                }

                if (map[i][j] == RAWA) {
                    kotak[i][j].setBackground(new Color(160, 210, 255));
                    kotak[i][j].setText("5");
                }

                if (map[i][j] == ASPAL) {
                    kotak[i][j].setBackground(new Color(220, 220, 220));
                    kotak[i][j].setText("1");
                }
            }
        }

        // Warna jalur hasil Dijkstra
        for (int i = 0; i < jalur.size(); i++) {

            Point p = jalur.get(i);

            if (!(p.x == startBaris && p.y == startKolom)
                    && !(p.x == goalBaris && p.y == goalKolom)) {

                kotak[p.x][p.y].setBackground(Color.YELLOW);
                kotak[p.x][p.y].setText("•");
            }
        }

        // Start
        kotak[startBaris][startKolom].setBackground(new Color(130, 220, 130));
        kotak[startBaris][startKolom].setText("S");

        // Goal
        kotak[goalBaris][goalKolom].setBackground(new Color(255, 130, 130));
        kotak[goalBaris][goalKolom].setText("G");
    }

    //algoritma dijkstra
    void dijkstra() {

        double[][] jarak = new double[BARIS][KOLOM];
        boolean[][] dikunjungi = new boolean[BARIS][KOLOM];

        int[][] sebelumBaris = new int[BARIS][KOLOM];
        int[][] sebelumKolom = new int[BARIS][KOLOM];

        // Nilai awal
        for (int i = 0; i < BARIS; i++) {
            for (int j = 0; j < KOLOM; j++) {

                jarak[i][j] = 999999;
                dikunjungi[i][j] = false;

                sebelumBaris[i][j] = -1;
                sebelumKolom[i][j] = -1;
            }
        }

        jarak[startBaris][startKolom] = 0;

        // Maksimal ada 100 kotak
        for (int langkah = 0; langkah < BARIS * KOLOM; langkah++) {

            int barisSekarang = -1;
            int kolomSekarang = -1;
            double jarakTerkecil = 999999;

            // Cari kotak yang jaraknya paling kecil
            for (int i = 0; i < BARIS; i++) {
                for (int j = 0; j < KOLOM; j++) {

                    if (dikunjungi[i][j] == false
                            && jarak[i][j] < jarakTerkecil) {

                        jarakTerkecil = jarak[i][j];
                        barisSekarang = i;
                        kolomSekarang = j;
                    }
                }
            }

            // Tidak ada kotak lagi
            if (barisSekarang == -1) {
                break;
            }

            dikunjungi[barisSekarang][kolomSekarang] = true;

            // Sudah sampai Goal
            if (barisSekarang == goalBaris
                    && kolomSekarang == goalKolom) {

                break;
            }

            int[] arahBaris;
            int[] arahKolom;

            if (ujiAktif == 3) {

                // Uji 3 menggunakan gerakan lurus
                // supaya cost medan tetap jelas: Aspal 1 dan Rawa 5
                arahBaris = new int[]{-1, 1, 0, 0};
                arahKolom = new int[]{0, 0, -1, 1};

            } else {

                // Uji 1 dan 2 dapat bergerak lurus dan diagonal
                arahBaris = new int[]{-1, 1, 0, 0, -1, -1, 1, 1};
                arahKolom = new int[]{0, 0, -1, 1, -1, 1, -1, 1};
            }

            for (int a = 0; a < arahBaris.length; a++) {

                int barisBaru = barisSekarang + arahBaris[a];
                int kolomBaru = kolomSekarang + arahKolom[a];

                // Cek batas grid
                if (barisBaru < 0 || barisBaru >= BARIS
                        || kolomBaru < 0 || kolomBaru >= KOLOM) {

                    continue;
                }

                // Tembok tidak boleh dilewati
                if (map[barisBaru][kolomBaru] == TEMBOK) {
                    continue;
                }

                double biaya;

                if (ujiAktif == 3) {

                    if (map[barisBaru][kolomBaru] == RAWA) {
                        biaya = 5;     // Rawa
                    } else {
                        biaya = 1;     // Aspal / jalan biasa
                    }

                } else {

                    if (arahBaris[a] != 0 && arahKolom[a] != 0) {
                        biaya = 1.414; // Diagonal sesuai PPT
                    } else {
                        biaya = 1;     // Lurus
                    }
                }

                double jarakBaru
                        = jarak[barisSekarang][kolomSekarang] + biaya;

                // Relaksasi
                if (jarakBaru < jarak[barisBaru][kolomBaru]) {

                    jarak[barisBaru][kolomBaru] = jarakBaru;

                    sebelumBaris[barisBaru][kolomBaru] = barisSekarang;
                    sebelumKolom[barisBaru][kolomBaru] = kolomSekarang;
                }
            }
        }

        // Kalau Goal tidak bisa dicapai
        if (jarak[goalBaris][goalKolom] == 999999) {
            lblHasil.setText("Jalur tidak ditemukan.");
            return;
        }

        // Membuat kembali jalur dari Goal ke Start
        jalur.clear();

        int b = goalBaris;
        int k = goalKolom;

        while (b != -1 && k != -1) {

            jalur.add(0, new Point(b, k));

            if (b == startBaris && k == startKolom) {
                break;
            }

            int bSebelum = sebelumBaris[b][k];
            int kSebelum = sebelumKolom[b][k];

            b = bSebelum;
            k = kSebelum;
        }

        tampilkanMap();

        int jumlahLangkah = jalur.size() - 1;

        // Hasil sesuai masing-masing uji
        if (ujiAktif == 1) {

            lblHasil.setText(
                    "Hasil Uji 1: Jalur diagonal terpendek ditemukan | "
                    + jumlahLangkah + " langkah | Total = "
                    + String.format("%.3f", jarak[goalBaris][goalKolom])
            );

        } else if (ujiAktif == 2) {

            lblHasil.setText(
                    "Hasil Uji 2: Bot berhasil keluar dari ceruk U | "
                    + jumlahLangkah + " langkah | Total = "
                    + String.format("%.3f", jarak[goalBaris][goalKolom])
            );

        } else {

            lblHasil.setText(
                    "Hasil Uji 3: Bot memilih rute memutar di aspal | "
                    + "Total = "
                    + String.format("%.3f", jarak[goalBaris][goalKolom])
            );
        }
    }

    public static void main(String[] args) {

        App app = new App();
        app.setVisible(true);
    }
}
