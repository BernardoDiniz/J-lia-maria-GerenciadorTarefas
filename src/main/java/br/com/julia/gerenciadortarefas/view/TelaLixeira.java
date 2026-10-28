package br.com.julia.gerenciadortarefas.view;

import br.com.julia.gerenciadortarefas.model.Tarefa;
import br.com.julia.gerenciadortarefas.service.TarefaService;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TelaLixeira extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TarefaService service = new TarefaService();
    private List<Tarefa> tarefasExibidas = new ArrayList<>();

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"ID", "Título", "Prioridade", "Etapas", "Status", "Excluída em"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    public TelaLixeira(Frame dono) {
        super(dono, "Lixeira - tarefas excluídas", true);
        montarTela();
        carregarTarefas();
        setSize(750, 350);
        setLocationRelativeTo(dono);
    }

    private void montarTela() {
        JLabel aviso = new JLabel("  As tarefas excluídas continuam gravadas e podem ser restauradas.");
        aviso.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));

        tabela.setRowHeight(24);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setMaxWidth(50);
        tabela.getColumnModel().getColumn(3).setMaxWidth(60);
        tabela.setDefaultRenderer(Object.class,
                new RenderizadorTarefa(linha -> tarefasExibidas.get(linha)));

        JButton botaoRestaurar = new JButton("Restaurar");
        JButton botaoFechar = new JButton("Fechar");
        botaoRestaurar.addActionListener(e -> restaurar());
        botaoFechar.addActionListener(e -> dispose());

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotoes.add(botaoRestaurar);
        painelBotoes.add(botaoFechar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(aviso, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(tabela), BorderLayout.CENTER);
        getContentPane().add(painelBotoes, BorderLayout.SOUTH);
    }

    private void carregarTarefas() {
        modeloTabela.setRowCount(0);
        try {
            tarefasExibidas = service.listarExcluidas();
            for (Tarefa t : tarefasExibidas) {
                modeloTabela.addRow(new Object[]{
                    t.getId(),
                    t.getTitulo(),
                    t.getPrioridade(),
                    t.getEtapas().size(),
                    t.getStatus(),
                    t.getDataExclusao().format(FORMATO_DATA_HORA)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar a lixeira:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurar() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma tarefa na lixeira.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Tarefa tarefa = tarefasExibidas.get(linha);

        try {
            service.restaurar(tarefa);
            JOptionPane.showMessageDialog(this,
                    "Tarefa \"" + tarefa.getTitulo() + "\" restaurada.\nEla voltou para a lista principal.",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao restaurar:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
        carregarTarefas();
    }
}
