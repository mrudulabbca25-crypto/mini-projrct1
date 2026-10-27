/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package dbms;

/**
 *
 * @author RVUW283
 */


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Dbms extends JFrame {

    private static final String URL =
        "jdbc:mysql://localhost:3306/game_collection";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private JTextField idField, nameField, genreField;
    private JTextField platformField, yearField, ratingField;
    private JTable gameTable;
    private DefaultTableModel model;

    public Dbms() {
        setTitle("Game Collection Management System");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
            "GAME COLLECTION MANAGEMENT SYSTEM",
            SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(25, 75, 130));
        title.setBorder(BorderFactory.createEmptyBorder(
            15, 5, 15, 5
        ));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder(
            "Game Details"
        ));

        idField = new JTextField();
        idField.setEditable(false);
        nameField = new JTextField();
        genreField = new JTextField();
        platformField = new JTextField();
        yearField = new JTextField();
        ratingField = new JTextField();

        
        form.add(new JLabel("Game Name:"));
        form.add(nameField);
        form.add(new JLabel("Genre:"));
        form.add(genreField);
        form.add(new JLabel("Platform:"));
        form.add(platformField);
        form.add(new JLabel("Release Year:"));
        form.add(yearField);
        form.add(new JLabel("Rating (0-10):"));
        form.add(ratingField);

        JPanel buttons = new JPanel(new GridLayout(5, 1, 8, 8));

        JButton addButton = new JButton("Add Game");
        JButton updateButton = new JButton("Update Game");
        JButton deleteButton = new JButton("Delete Game");
        JButton clearButton = new JButton("Clear Fields");
        JButton refreshButton = new JButton("View Games");

        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        buttons.add(refreshButton);

        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setPreferredSize(new Dimension(330, 0));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(
            5, 10, 10, 5
        ));
        leftPanel.add(form, BorderLayout.CENTER);
        leftPanel.add(buttons, BorderLayout.SOUTH);

        model = new DefaultTableModel(
            new String[]{"ID", "Game Name", "Genre",
                "Platform", "Year", "Rating"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        gameTable = new JTable(model);
        gameTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
        gameTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(gameTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            "Game Collection"
        ));

        add(leftPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addGame());
        updateButton.addActionListener(e -> updateGame());
        deleteButton.addActionListener(e -> deleteGame());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> loadGames());

        gameTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()
                    && gameTable.getSelectedRow() != -1) {
                int row = gameTable.getSelectedRow();

                idField.setText(model.getValueAt(row, 0).toString());
                nameField.setText(model.getValueAt(row, 1).toString());
                genreField.setText(model.getValueAt(row, 2).toString());
                platformField.setText(model.getValueAt(row, 3).toString());
                yearField.setText(model.getValueAt(row, 4).toString());
                ratingField.setText(model.getValueAt(row, 5).toString());
            }
        });

        loadGames();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private boolean validateFields() {
        try {
            if (nameField.getText().trim().isEmpty()
                    || genreField.getText().trim().isEmpty()
                    || platformField.getText().trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Please fill in the game name, genre and platform."
                );
            }

            int year = Integer.parseInt(yearField.getText().trim());
            double rating = Double.parseDouble(
                ratingField.getText().trim()
            );

            if (year < 1950 || year > 2100) {
                throw new IllegalArgumentException(
                    "Enter a valid release year."
                );
            }

            if (!Double.isFinite(rating)
                    || rating < 0 || rating > 10) {
                throw new IllegalArgumentException(
                    "Rating must be between 0 and 10."
                );
            }

            return true;

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Release year and rating must be numbers.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }

        return false;
    }

    private void addGame() {
        if (!validateFields()) return;

        String sql = "INSERT INTO games " +
            "(game_name, genre, platform, release_year, rating) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nameField.getText().trim());
            ps.setString(2, genreField.getText().trim());
            ps.setString(3, platformField.getText().trim());
            ps.setInt(4, Integer.parseInt(yearField.getText().trim()));
            ps.setDouble(5, Double.parseDouble(
                ratingField.getText().trim()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Game added successfully!");
            clearFields();
            loadGames();

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loadGames() {
        model.setRowCount(0);

        String sql = "SELECT * FROM games ORDER BY game_id";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("game_id"),
                    rs.getString("game_name"),
                    rs.getString("genre"),
                    rs.getString("platform"),
                    rs.getInt("release_year"),
                    rs.getDouble("rating")
                });
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void updateGame() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Select a game from the table first.");
            return;
        }

        if (!validateFields()) return;

        String sql = "UPDATE games SET game_name=?, genre=?, " +
            "platform=?, release_year=?, rating=? WHERE game_id=?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nameField.getText().trim());
            ps.setString(2, genreField.getText().trim());
            ps.setString(3, platformField.getText().trim());
            ps.setInt(4, Integer.parseInt(yearField.getText().trim()));
            ps.setDouble(5, Double.parseDouble(
                ratingField.getText().trim()));
            ps.setInt(6, Integer.parseInt(idField.getText()));

            int rows = ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                rows > 0 ? "Game updated successfully!"
                         : "Game not found.");

            clearFields();
            loadGames();

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void deleteGame() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Select a game from the table first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this game?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM games WHERE game_id=?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idField.getText()));

            int rows = ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                rows > 0 ? "Game deleted successfully!"
                         : "Game not found.");

            clearFields();
            loadGames();

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        genreField.setText("");
        platformField.setText("");
        yearField.setText("");
        ratingField.setText("");
        gameTable.clearSelection();
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Database error: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Dbms gui = new Dbms();
            gui.setVisible(true);
        });
    }
}

