package br.com.julia.gerenciadortarefas.view;

import br.com.julia.gerenciadortarefas.model.Dificuldade;
import br.com.julia.gerenciadortarefas.model.Prioridade;
import br.com.julia.gerenciadortarefas.model.Tarefa;
import br.com.julia.gerenciadortarefas.service.TarefaService;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import javax.swing.*;

public class TelaTarefa extends JDialog {

    // STRICT: recusa datas que não existem, como 31/02/2026
    private static final DateTimeFormatter FORMATO_DATA
            = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final TarefaService service = new TarefaService();
    private boolean salvou = false;

    private final JTextField campoTitulo = new JTextField(30);
    private final JTextArea campoDescricao = new JTextArea(3, 30);
    private final JComboBox<Prioridade> comboPrioridade = new JComboBox<>(Prioridade.values());
    private final JTextField campoPrazo = new JTextField(10);
    private final JTextField campoNovaEtapa = new JTextField(22);
    private final DefaultListModel<String> modeloEtapas = new DefaultListModel<>();
    private final JList<String> listaEtapas = new JList<>(modeloEtapas);
    private final JLabel rotuloDificuldade = new JLabel();

    public TelaTarefa(Frame dono) {
        super(dono, "Nova tarefa", true); // true = modal (bloqueia a tela de trás)
        montarTela();
        comboPrioridade.setSelectedIndex(-1); // obriga o usuário a escolher
        atualizarDificuldade();
        pack();
        setLocationRelativeTo(dono);
    }

    private void montarTela() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        int linha = 0;
        adicionarLinha(formulario, c, linha++, "Título*:", campoTitulo);

        campoDescricao.setLineWrap(true);
        campoDescricao.setWrapStyleWord(true);
        adicionarLinha(formulario, c, linha++, "Descrição:", new JScrollPane(campoDescricao));

        adicionarLinha(formulario, c, linha++, "Prioridade*:", comboPrioridade);

        JPanel painelPrazo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        painelPrazo.add(campoPrazo);
        painelPrazo.add(new JLabel("  dd/mm/aaaa (vazio = sem prazo)"));
        adicionarLinha(formulario, c, linha++, "Prazo:", painelPrazo);

        JButton botaoAdicionar = new JButton("Adicionar");
        JPanel painelNovaEtapa = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        painelNovaEtapa.add(campoNovaEtapa);
        painelNovaEtapa.add(botaoAdicionar);
        adicionarLinha(formulario, c, linha++, "Nova etapa:", painelNovaEtapa);

        JButton botaoRemover = new JButton("Remover etapa selecionada");
        listaEtapas.setVisibleRowCount(6);
        JPanel painelLista = new JPanel(new BorderLayout(4, 4));
        painelLista.add(new JScrollPane(listaEtapas), BorderLayout.CENTER);
        painelLista.add(botaoRemover, BorderLayout.SOUTH);
        adicionarLinha(formulario, c, linha++, "Etapas*:", painelLista);

        adicionarLinha(formulario, c, linha++, "Dificuldade:", rotuloDificuldade);

        // Eventos (o que acontece ao clicar)
        botaoAdicionar.addActionListener(e -> adicionarEtapa());
        campoNovaEtapa.addActionListener(e -> adicionarEtapa()); // Enter também adiciona
        botaoRemover.addActionListener(e -> removerEtapa());

        JButton botaoSalvar = new JButton("Salvar");
        JButton botaoCancelar = new JButton("Cancelar");
        botaoSalvar.addActionListener(e -> salvar());
        botaoCancelar.addActionListener(e -> dispose());
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotoes.add(botaoCancelar);
        painelBotoes.add(botaoSalvar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(formulario, BorderLayout.CENTER);
        getContentPane().add(painelBotoes, BorderLayout.SOUTH);
    }

    private void adicionarLinha(JPanel painel, GridBagConstraints c, int linha,
            String rotulo, Component campo) {
        c.gridx = 0;
        c.gridy = linha;
        c.weightx = 0;
        painel.add(new JLabel(rotulo), c);
        c.gridx = 1;
        c.weightx = 1;
        painel.add(campo, c);
    }

    private void adicionarEtapa() {
        String texto = campoNovaEtapa.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite a descrição da etapa.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modeloEtapas.addElement(texto);
        campoNovaEtapa.setText("");
        campoNovaEtapa.requestFocus();
        atualizarDificuldade();
    }

    private void removerEtapa() {
        int indice = listaEtapas.getSelectedIndex();
        if (indice < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma etapa na lista.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modeloEtapas.remove(indice);
        atualizarDificuldade();
    }

    // Mostra a dificuldade ao vivo, conforme o usuário adiciona/remove etapas
    private void atualizarDificuldade() {
        int qtd = modeloEtapas.size();
        if (qtd == 0) {
            rotuloDificuldade.setText("— (adicione etapas)");
        } else {
            rotuloDificuldade.setText(Dificuldade.classificar(qtd) + " (" + qtd + " etapa(s))");
        }
    }

    private void salvar() {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(campoTitulo.getText().trim());
        String descricao = campoDescricao.getText().trim();
        tarefa.setDescricao(descricao.isEmpty() ? null : descricao);
        tarefa.setPrioridade((Prioridade) comboPrioridade.getSelectedItem());

        String textoPrazo = campoPrazo.getText().trim();
        if (!textoPrazo.isEmpty()) {
            try {
                tarefa.setPrazo(LocalDate.parse(textoPrazo, FORMATO_DATA));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Prazo inválido. Use o formato dd/mm/aaaa (ex.: 30/11/2026).",
                        "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        for (int i = 0; i < modeloEtapas.size(); i++) {
            tarefa.adicionarEtapa(modeloEtapas.get(i));
        }

        try {
            service.cadastrar(tarefa);
            salvou = true;
            JOptionPane.showMessageDialog(this,
                    "Tarefa cadastrada com sucesso! (id " + tarefa.getId() + ")",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar no banco:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSalvou() {
        return salvou;
    }
}
