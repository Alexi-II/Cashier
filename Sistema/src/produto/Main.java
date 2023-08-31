package produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends JFrame {
    private JTabbedPane tabbedPane;
    private EstoquePanel estoquePanel;
    private CalculoPanel calculoPanel;

    private DefaultTableModel tableModel;

    public Main(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
        setTitle("Sistema de Estoque e Vendas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        layoutComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        estoquePanel = new EstoquePanel(tableModel);
        calculoPanel = new CalculoPanel(tableModel);

        tabbedPane.addTab("Estoque", estoquePanel);
        tabbedPane.addTab("Realizar Compra", calculoPanel);
    }

    private void layoutComponents() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Nome", "Preço", "Código de Barras", "Quantidade", "Data de Validade", "Data de Reposição"}, 0);

            JFrame frame = new Main(tableModel);
            frame.setVisible(true);
        });
    }

}

class EstoquePanel extends JPanel {
    protected DefaultTableModel tableModel;
    private JTable table;
    private JButton addButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton updateButton; // Novo botão para atualizar a tabela

    public EstoquePanel(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
        setLayout(new BorderLayout());

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        addButton = new JButton("Adicionar Item");
        deleteButton = new JButton("Apagar Item");
        saveButton = new JButton("Salvar Itens");
        updateButton = new JButton("Atualizar Tabela"); // Inicializa o botão

        addButton.addActionListener(e -> addNewItem());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        saveButton.addActionListener(e -> saveItemsToFile());
        updateButton.addActionListener(e -> updateTable()); // Define a ação do botão de atualização

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(updateButton); // Adiciona o botão de atualização

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadItemsFromFile();
        verificarValidadeProdutos();
        verificarEstoqueBaixo();
        removerItensVencidos();
    }
private void updateTable() {
    tableModel.setRowCount(0);
    loadItemsFromFile();
    verificarValidadeProdutos();
    verificarEstoqueBaixo();
    removerItensVencidos();
}

private void addNewItem() {
    String nome = JOptionPane.showInputDialog(this, "Nome do Produto:");
    String precoStr = JOptionPane.showInputDialog(this, "Preço:");
    double preco = Double.parseDouble(precoStr);
    long codigoBarras = Long.parseLong(JOptionPane.showInputDialog(this, "Código de Barras:"));
    int quantidade = Integer.parseInt(JOptionPane.showInputDialog(this, "Quantidade:"));
    String dataValidade = JOptionPane.showInputDialog(this, "Data de Validade (dd/mm/aaaa):");
    String dataReposicao = JOptionPane.showInputDialog(this, "Data de Reposição (dd/mm/aaaa):");

    tableModel.addRow(new Object[]{nome, preco, codigoBarras, quantidade, dataValidade, dataReposicao});
}

    private void deleteSelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
        }
    }

    private void saveItemsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("banco.txt"))) {
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    writer.print(tableModel.getValueAt(row, col));
                    if (col < tableModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
            JOptionPane.showMessageDialog(this, "Itens salvos com sucesso.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar itens: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
private void loadItemsFromFile() {
    try (BufferedReader reader = new BufferedReader(new FileReader("banco.txt"))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            tableModel.addRow(parts);
        }
    } catch (IOException e) {
        // Arquivo não encontrado ou erro de leitura, não faz nada
    }
}


    private void verificarValidadeProdutos() {
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String validadeStr = (String) tableModel.getValueAt(row, 4);
            try {
                Date validade = sdf.parse(validadeStr);
                long diff = validade.getTime() - currentDate.getTime();
                long days = diff / (24 * 60 * 60 * 1000);

                if (days <= 5) {
                    String nome = (String) tableModel.getValueAt(row, 0);
                    JOptionPane.showMessageDialog(this, "Atenção! O produto '" + nome + "' está perto da validade.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            } catch (ParseException e) {
                // Ignorar formatos inválidos de data
            }
        }
    }

    private void verificarEstoqueBaixo() {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            int quantidade = Integer.parseInt((String) tableModel.getValueAt(row, 3));
            String nome = (String) tableModel.getValueAt(row, 0);

            if (quantidade < 5) {
                JOptionPane.showMessageDialog(this, "Atenção! O produto '" + nome + "' possui estoque abaixo de 5 unidades.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void removerItensVencidos() {
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (int row = tableModel.getRowCount() - 1; row >= 0; row--) {
            String validadeStr = (String) tableModel.getValueAt(row, 4);
            try {
                Date validade = sdf.parse(validadeStr);
                if (validade.before(currentDate)) {
                    tableModel.removeRow(row);
                }
            } catch (ParseException e) {
                // Ignorar formatos inválidos de data
            }
        }
    }
}

class CalculoPanel extends JPanel {
    private JTextField codigoBarraField;
    private JTextField quantidadeField;
    private JButton realizarCompraButton;
    private JTextArea resultadoArea;

    private DefaultTableModel tableModel;

    public CalculoPanel(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
        setLayout(new BorderLayout());

        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        codigoBarraField = new JTextField();
        quantidadeField = new JTextField();
        realizarCompraButton = new JButton("Realizar Compra");
        resultadoArea = new JTextArea();
        resultadoArea.setEditable(false);

        realizarCompraButton.addActionListener(e -> realizarCompra());
    }

    private void layoutComponents() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        // Código de Barras
        JLabel codigoLabel = new JLabel("Código de Barras:");
        codigoBarraField.setPreferredSize(new Dimension(200, 25));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.LINE_END;
        inputPanel.add(codigoLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.LINE_START;
        inputPanel.add(codigoBarraField, constraints);

        // Quantidade de Itens
        JLabel quantidadeLabel = new JLabel("Quantidade de Itens:");
        quantidadeField.setPreferredSize(new Dimension(50, 25));
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.LINE_END;
        inputPanel.add(quantidadeLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        inputPanel.add(quantidadeField, constraints);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(realizarCompraButton, BorderLayout.CENTER);

        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.add(centerPanel, BorderLayout.NORTH);
        lowerPanel.add(new JScrollPane(resultadoArea), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(lowerPanel, BorderLayout.CENTER);
    }

private void realizarCompra() {
    String codigoBarraStr = codigoBarraField.getText();
    if (codigoBarraStr.isEmpty()) {
        showError("Informe o código de barras.");
        return;
    }
    
    long codigoBarra = Long.parseLong(codigoBarraStr);

    int quantidade;
    try {
        quantidade = Integer.parseInt(quantidadeField.getText()); // Corrigir aqui
    } catch (NumberFormatException e) {
        showError("Quantidade inválida.");
        return;
    }

        Produto produto = buscarProduto(codigoBarra);
        if (produto == null) {
            showError("Produto não encontrado.");
            return;
        }

        if (produto.getQuantidade() < quantidade) {
            showError("Quantidade insuficiente em estoque.");
            return;
        }

        double precoTotal = produto.getPrecoUnitario() * quantidade;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String horaCompra = sdf.format(new Date());

        String resultado = "Preço Unitário: " + produto.getPrecoUnitario() + "\n"
                + "Validade: " + produto.getValidade() + "\n"
                + "Preço Total: " + precoTotal + "\n"
                + "Hora da Compra: " + horaCompra;

        resultadoArea.setText(resultado);

        int response = JOptionPane.showConfirmDialog(this, "Deseja realizar a compra?", "Compra", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            realizarVenda(codigoBarra, quantidade);
        }
    }

private void realizarVenda(long codigoBarra, int quantidadeComprada) {
    Produto produto = buscarProduto(codigoBarra);
    if (produto == null) {
        showError("Produto não encontrado.");
        return;
    }
    double precoTotalCompra = produto.getPrecoUnitario() * quantidadeComprada;
    produto.decrementarQuantidade(quantidadeComprada);

    atualizarEstoqueAposVenda(codigoBarra, quantidadeComprada);

    String compraInfo = "Produto Comprado: " + produto.getNome() + "\n"
            + "Quantidade Comprada: " + quantidadeComprada + "\n"
            + "Preço Total da Compra: " + precoTotalCompra + "\n";

    saveCompraToFile(compraInfo);

    JOptionPane.showMessageDialog(this, "Compra registrada com sucesso:\n\n" + compraInfo);
}

private void atualizarEstoqueAposVenda(long codigoBarra, int quantidadeComprada) {
    for (int row = 0; row < tableModel.getRowCount(); row++) {
        long codigoBarras = Long.parseLong(tableModel.getValueAt(row, 2).toString()); // Corrigir aqui
        if (codigoBarras == codigoBarra) {
            int quantidadeAtual = Integer.parseInt(tableModel.getValueAt(row, 3).toString()); // Corrigir aqui
            int novaQuantidade = quantidadeAtual - quantidadeComprada;
            tableModel.setValueAt(Integer.toString(novaQuantidade), row, 3);
            break;
        }
    }
}

    private void saveCompraToFile(String compraInfo) {
        String desktopPath = System.getProperty("user.home") + "/Desktop/";
        String fileName = "compras.txt";
        String filePath = desktopPath + fileName;

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
            writer.println(compraInfo);
            writer.println("-----");
        } catch (IOException e) {
            showError("Erro ao registrar a compra: " + e.getMessage());
        }
    }

private Produto buscarProduto(long codigoBarra) {
    for (int row = 0; row < tableModel.getRowCount(); row++) {
        long codigoBarras = Long.parseLong(tableModel.getValueAt(row, 2).toString()); // Corrigir aqui
        if (codigoBarras == codigoBarra) {
            String nome = (String) tableModel.getValueAt(row, 0);
            double preco = Double.parseDouble(tableModel.getValueAt(row, 1).toString()); // Corrigir aqui
            String validade = (String) tableModel.getValueAt(row, 4);
            int quantidade = Integer.parseInt(tableModel.getValueAt(row, 3).toString()); // Corrigir aqui
            return new Produto(nome, preco, validade, quantidade);
        }
    }
    return null;
}

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}

class Produto {
    private String nome;
    private double precoUnitario;
    private String validade;
    private int quantidade;

    public Produto(String nome, double precoUnitario, String validade, int quantidade) {
        this.nome = nome;
        this.precoUnitario = precoUnitario;
        this.validade = validade;
        this.quantidade = quantidade;
    }

    public String getNome() {
        return nome;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public String getValidade() {
        return validade;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void decrementarQuantidade(int quantidadeComprada) {
        quantidade -= quantidadeComprada;
    }
}
