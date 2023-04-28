package org.example;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;

public class FileExplorer extends JFrame {

    private JPanel topPanel;
    private JPanel bottomPanel;
    private JTable table;
    private DefaultTableModel model;
    private JScrollPane scrollPane;
    private JLabel currentPathLabel;
    private JTextField pathField;
    private JButton goButton;
    private JButton upButton;
    private JButton newButton;
    private JButton deleteButton;
    private JButton renameButton;


    public FileExplorer() {
        super("Файловый менеджер");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        topPanel = new JPanel();
        bottomPanel = new JPanel();
        currentPathLabel = new JLabel("Текущий путь:");
        pathField = new JTextField(30);
        goButton = new JButton("Перейти");
        upButton = new JButton("Вверх");
        newButton = new JButton("Новый файл");
        deleteButton = new JButton("Удалить");
        renameButton = new JButton("Переименовать");

        model = new DefaultTableModel();
        model.addColumn("Имя файла");
        model.addColumn("Тип");
        model.addColumn("Размер");
        model.addColumn("Дата последнего изменения");
        table = new JTable(model);

        scrollPane = new JScrollPane(table);

        topPanel.add(currentPathLabel);
        topPanel.add(pathField);
        topPanel.add(goButton);
        topPanel.add(upButton);
        bottomPanel.add(newButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(renameButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        pathField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTable();
            }
        });

        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTable();
            }
        });

        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = pathField.getText();
                File file = new File(path);
                String parentPath = file.getParent();
                if (parentPath != null) {
                    pathField.setText(parentPath);
                    updateTable();
                }
            }
        });

        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                fileChooser.setDialogTitle("Создать новый файл или папку");
                int userSelection = fileChooser.showSaveDialog(FileExplorer.this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToCreate = fileChooser.getSelectedFile();
                    boolean success = false;
                    if (                fileToCreate.isDirectory()) {
                        success = fileToCreate.mkdir();
                    } else {
                        try {
                            success = fileToCreate.createNewFile();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    if (success) {
                        updateTable();
                    } else {
                        System.out.println("Failed to create new file/folder");
                    }
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = table.getSelectedRows();
                if (selectedRows.length == 0) {
                    System.out.println("No item selected");
                    return;
                }
                int option = JOptionPane.showConfirmDialog(FileExplorer.this, "Are you sure to delete selected items?", "Confirmation", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        String fileName = (String) model.getValueAt(selectedRows[i], 0);
                        String filePath = pathField.getText() + File.separator + fileName;
                        File file = new File(filePath);
                        if (file.isDirectory()) {
                            deleteFolder(file);
                        } else {
                            file.delete();
                        }
                    }
                    updateTable();
                }
            }
        });

        renameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    System.out.println("No item selected");
                    return;
                }
                String oldName = (String) model.getValueAt(selectedRow, 0);
                String newPath = pathField.getText() + File.separator + oldName;
                File oldFile = new File(newPath);
                String newName = JOptionPane.showInputDialog(FileExplorer.this, "Enter new name:", oldName);
                if (newName == null || newName.trim().equals("")) {
                    System.out.println("Invalid name");
                    return;
                }
                newPath = pathField.getText() + File.separator + newName;
                File newFile = new File(newPath);
                boolean success = oldFile.renameTo(newFile);
                if (success) {
                    updateTable();
                } else {
                    System.out.println("Failed to rename file/folder");
                }
            }
        });

        updateTable();
    }

    private void updateTable() {
        String path = pathField.getText();
        File file = new File(path);
        if (file.isDirectory()) {
            model.setRowCount(0);
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    String type = f.isDirectory() ? "Folder" : getFileExtension(name);
                    long size = f.length();
                    String modified = new Date(f.lastModified()).toString();
                    Object[] row = {name, type, size, modified};
                    model.addRow(row);
                }
            }
        } else {
            System.out.println("Invalid directory path");
        }
    }

    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0 && index < fileName.length() - 1) {
            return fileName.substring(index + 1).toUpperCase();
        }
        return "Unknown";
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        FileExplorer explorer = new FileExplorer();

        explorer.setVisible(true);
    }
}

class ProcessMonitor extends Thread {
    private JTextArea output;
    private volatile boolean stopped = false;

    public ProcessMonitor(JTextArea output) {
        this.output = output;
    }

    public void stopMonitoring() {
        stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                String line;
                Process process = Runtime.getRuntime().exec("ps -e");
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = input.readLine()) != null) {
                    output.append(line + "\n");
                }
                input.close();
                output.setCaretPosition(output.getDocument().getLength());
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class Terminal extends JFrame {
    private JTextArea output;
    private JTextField input;
    private Process process;
    private ProcessBuilder builder;

    public Terminal() {
        setTitle("Терминал");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        output = new JTextArea();
        output.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(output);
        add(scrollPane, BorderLayout.CENTER);

        input = new JTextField();
        input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String command = input.getText();
                    output.append("$ " + command + "\n");
                    if (command.equals("clear")) {
                        output.setText("");
                        input.setText("");
                        return;
                    }
                    process = builder.command("bash", "-c", command).start();
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = inputReader.readLine()) != null) {
                        output.append(line + "\n");
                    }
                    inputReader.close();
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((line = errorReader.readLine()) != null) {
                        output.append(line + "\n");
                    }
                    errorReader.close();
                    output.setCaretPosition(output.getDocument().getLength());
                    input.setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        add(input, BorderLayout.SOUTH);

        builder = new ProcessBuilder();
        builder.redirectErrorStream(true);
    }

    @Override
    public void dispose() {
        if (process != null) {
            process.destroy();
        }
        super.dispose();
    }
}


        