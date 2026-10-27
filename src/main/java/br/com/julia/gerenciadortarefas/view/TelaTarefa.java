package br.com.julia.gerenciadortarefas.view;

import br.com.julia.gerenciadortarefas.model.Dificuldade;
import br.com.julia.gerenciadortarefas.model.Etapa;
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

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final TarefaService service = new TarefaService();
    private final Tarefa tarefaEditada; // null = cadastro novo; preenchida = edição
    private boolean salvou = false;

    private final JTextField campoTitulo = new JTextField(30);
    private final JTextArea campoDescricao = new JTextArea(3, 30);
    private final JComboBox<Prioridade> comboPrioridade = new JComboBox<>(Prioridade.values());
    private final JTextField campoPrazo = new JTextField(10);
    private final JLabel rotuloAjudaPrazo = new JLabel("  dd/mm/aaaa (vazio = sem prazo)");
    private final JTextField campoNovaEtapa = new JTextField(22);
    private final DefaultListModel<String> modeloEtapas = new DefaultListModel<>();
    private final JList<String> listaEtapas = new JList<>(modeloEtapas);
    private final JLabel rotuloDificuldade = new JLabel();

    // Construtor para CADASTRO
    public TelaTarefa(Frame dono) {
        this(dono, null);
    }

    // Construtor para EDIÇÃO
    public TelaTarefa(Frame dono, Tarefa tarefa) {
        super(dono, tarefa == null ? "Nova tarefa" : "Editar tarefa #" + tarefa.getId(), true);
        this.tarefaEditada = tarefa;
        montarTela();
        if (tarefa == null) {
            comboPrioridade.setSelectedIndex(-1); // obriga a escolher
        } else {
            preencherCampos(tarefa);
        }
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
        painelPrazo.add(rotuloAjudaPrazo);
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

        botaoAdicionar.addActionListener(e -> adicionarEtapa());
        campoNovaEtapa.addActionListener(e -> adicionarEtapa());
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

    // Preenche os campos com os dados da tarefa que será editada
    private void preencherCampos(Tarefa t) {
        campoTitulo.setText(t.getTitulo());
        campoDescricao.setText(t.getDescricao() == null ? "" : t.getDescricao());
        comboPrioridade.setSelectedItem(t.getPrioridade());
        campoPrazo.setText(t.getPrazo() == null ? "" : t.getPrazo().format(FORMATO_DATA));
        campoPrazo.setEnabled(false); // prazo só muda pelo Reagendar
        rotuloAjudaPrazo.setText("  para alterar, use o botão Reagendar");
        for (Etapa etapa : t.getEtapas()) {
            modeloEtapas.addElement(etapa.getDescricao());
        }
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

    private void atualizarDificuldade() {
        int qtd = modeloEtapas.size();
        if (qtd == 0) {
            rotuloDificuldade.setText("— (adicione etapas)");
        } else {
            rotuloDificuldade.setText(Dificuldade.classificar(qtd) + " (" + qtd + " etapa(s))");
        }
    }

    private void salvar() {
        boolean novaTarefa = (tarefaEditada == null);
        Tarefa tarefa = novaTarefa ? new Tarefa() : tarefaEditada;

        tarefa.setTitulo(campoTitulo.getText().trim());
        String descricao = campoDescricao.getText().trim();
        tarefa.setDescricao(descricao.isEmpty() ? null : descricao);
        tarefa.setPrioridade((Prioridade) comboPrioridade.getSelectedItem());

        // O prazo só é lido do campo no cadastro (na edição ele está bloqueado)
        if (novaTarefa) {
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
        }

        tarefa.getEtapas().clear();
        for (int i = 0; i < modeloEtapas.size(); i++) {
            tarefa.adicionarEtapa(modeloEtapas.get(i));
        }

        try {
            if (novaTarefa) {
                service.cadastrar(tarefa);
                JOptionPane.showMessageDialog(this,
                        "Tarefa cadastrada com sucesso! (id " + tarefa.getId() + ")",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                service.editar(tarefa);
                JOptionPane.showMessageDialog(this,
                        "Tarefa #" + tarefa.getId() + " atualizada com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            salvou = true;
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