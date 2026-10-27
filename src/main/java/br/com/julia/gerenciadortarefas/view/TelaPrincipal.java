package br.com.julia.gerenciadortarefas.view;

import br.com.julia.gerenciadortarefas.model.Tarefa;
import br.com.julia.gerenciadortarefas.service.TarefaService;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TelaPrincipal extends JFrame {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TarefaService service = new TarefaService();

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"ID", "Título", "Prioridade", "Dificuldade", "Etapas", "Prazo", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false; // a tabela é só para visualizar
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    public TelaPrincipal() {
        super("Gerenciador de Tarefas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        montarTela();
        carregarTarefas();
        setSize(900, 500);
        setLocationRelativeTo(null); // centraliza na tela
    }

    private void montarTela() {
        JButton botaoNova = new JButton("Nova tarefa");
        JButton botaoAtualizar = new JButton("Atualizar lista");
        botaoNova.addActionListener(e -> abrirCadastro());
        botaoAtualizar.addActionListener(e -> carregarTarefas());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(botaoNova);
        barra.add(botaoAtualizar);

        tabela.setRowHeight(24);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setMaxWidth(50);
        tabela.getColumnModel().getColumn(4).setMaxWidth(60);

        add(barra, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }

    // Consulta o banco e preenche a tabela
    private void carregarTarefas() {
        modeloTabela.setRowCount(0); // limpa a tabela
        try {
            List<Tarefa> tarefas = service.listarAtivas();
            for (Tarefa t : tarefas) {
                modeloTabela.addRow(new Object[]{
                    t.getId(),
                    t.getTitulo(),
                    t.getPrioridade(),
                    t.getDificuldade(),
                    t.getEtapas().size(),
                    t.getPrazo() == null ? "Sem prazo" : t.getPrazo().format(FORMATO_DATA),
                    t.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar tarefas:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirCadastro() {
        TelaTarefa tela = new TelaTarefa(this);
        tela.setVisible(true);   // por ser modal, o código espera aqui até a janela fechar
        if (tela.isSalvou()) {
            carregarTarefas();   // recarrega para mostrar a tarefa nova
        }
    }
}