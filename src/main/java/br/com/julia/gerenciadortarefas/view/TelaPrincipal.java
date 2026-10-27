package br.com.julia.gerenciadortarefas.view;

import br.com.julia.gerenciadortarefas.model.Tarefa;
import br.com.julia.gerenciadortarefas.service.TarefaService;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TelaPrincipal extends JFrame {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final TarefaService service = new TarefaService();
    private List<Tarefa> tarefasExibidas = new ArrayList<>(); // mesma ordem das linhas da tabela

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"ID", "Título", "Prioridade", "Dificuldade", "Etapas", "Prazo", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    public TelaPrincipal() {
        super("Gerenciador de Tarefas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        montarTela();
        carregarTarefas();
        setSize(950, 500);
        setLocationRelativeTo(null);
    }

    private void montarTela() {
        JButton botaoNova = new JButton("Nova tarefa");
        JButton botaoEditar = new JButton("Editar");
        JButton botaoReagendar = new JButton("Reagendar");
        JButton botaoAtualizar = new JButton("Atualizar lista");

        botaoNova.addActionListener(e -> abrirCadastro());
        botaoEditar.addActionListener(e -> abrirEdicao());
        botaoReagendar.addActionListener(e -> reagendar());
        botaoAtualizar.addActionListener(e -> carregarTarefas());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(botaoNova);
        barra.add(botaoEditar);
        barra.add(botaoReagendar);
        barra.add(botaoAtualizar);

        tabela.setRowHeight(24);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setMaxWidth(50);
        tabela.getColumnModel().getColumn(4).setMaxWidth(60);

        add(barra, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }

    private void carregarTarefas() {
        modeloTabela.setRowCount(0);
        try {
            tarefasExibidas = service.listarAtivas();
            for (Tarefa t : tarefasExibidas) {
                modeloTabela.addRow(new Object[]{
                    t.getId(),
                    t.getTitulo(),
                    t.getPrioridade(),
                    t.getDificuldade(),
                    t.getEtapas().size(),
                    textoPrazo(t),
                    t.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar tarefas:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String textoPrazo(Tarefa t) {
        if (t.getPrazo() == null) {
            return "Sem prazo";
        }
        String texto = t.getPrazo().format(FORMATO_DATA);
        if (t.isReagendada()) {
            texto += " (reagendada " + t.getReagendamentos().size() + "x)";
        }
        return texto;
    }

    // Retorna a tarefa da linha selecionada, ou null (com aviso) se nada estiver selecionado
    private Tarefa tarefaSelecionada() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma tarefa na tabela.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return tarefasExibidas.get(linha);
    }

    private void abrirCadastro() {
        new TelaTarefa(this).setVisible(true);
        carregarTarefas();
    }

    private void abrirEdicao() {
        Tarefa tarefa = tarefaSelecionada();
        if (tarefa == null) {
            return;
        }
        new TelaTarefa(this, tarefa).setVisible(true);
        carregarTarefas(); // relê do banco: descarta qualquer alteração que não foi salva
    }

    private void reagendar() {
        Tarefa tarefa = tarefaSelecionada();
        if (tarefa == null) {
            return;
        }

        String prazoAtual = tarefa.getPrazo() == null ? "sem prazo" : tarefa.getPrazo().format(FORMATO_DATA);
        String texto = JOptionPane.showInputDialog(this,
                "Tarefa: " + tarefa.getTitulo()
                + "\nPrazo atual: " + prazoAtual
                + "\n\nNovo prazo (dd/mm/aaaa):",
                "Reagendar tarefa", JOptionPane.QUESTION_MESSAGE);

        if (texto == null) {
            return; // clicou em Cancelar
        }

        LocalDate novoPrazo = null;
        texto = texto.trim();
        if (!texto.isEmpty()) {
            try {
                novoPrazo = LocalDate.parse(texto, FORMATO_DATA);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Data inválida. Use o formato dd/mm/aaaa (ex.: 30/11/2026).",
                        "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            service.reagendar(tarefa, novoPrazo);
            JOptionPane.showMessageDialog(this,
                     "Prazo atualizado para " + tarefa.getPrazo().format(FORMATO_DATA) + ".",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao reagendar:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
        carregarTarefas();
    }
}