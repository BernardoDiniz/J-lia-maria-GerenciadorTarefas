package br.com.julia.gerenciadortarefas.view;

import br.com.julia.gerenciadortarefas.model.Tarefa;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.function.IntFunction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

// Pinta cada linha da tabela conforme a situação da tarefa
public class RenderizadorTarefa extends DefaultTableCellRenderer {

    public static final Color COR_CONCLUIDA = new Color(212, 237, 218);  // verde claro
    public static final Color COR_REAGENDADA = new Color(255, 236, 179); // laranja claro
    public static final Color COR_EXCLUIDA = new Color(230, 230, 230);   // cinza claro
    private static final Color TEXTO_EXCLUIDA = new Color(110, 110, 110); // cinza escuro

    // Função que devolve a tarefa de uma linha (cada tela informa a sua lista)
    private final IntFunction<Tarefa> tarefaDaLinha;

    public RenderizadorTarefa(IntFunction<Tarefa> tarefaDaLinha) {
        this.tarefaDaLinha = tarefaDaLinha;
    }

    @Override
    public Component getTableCellRendererComponent(JTable tabela, Object valor,
            boolean selecionada, boolean comFoco, int linha, int coluna) {

        Component celula = super.getTableCellRendererComponent(
                tabela, valor, selecionada, comFoco, linha, coluna);

        if (selecionada) {
            return celula; // mantém o azul padrão da linha selecionada
        }

        Tarefa tarefa = tarefaDaLinha.apply(linha);
        Color fundo = tabela.getBackground();
        Color texto = tabela.getForeground();
        Font fonte = tabela.getFont();

        if (tarefa.isExcluida()) {
            fundo = COR_EXCLUIDA;
            texto = TEXTO_EXCLUIDA;
            fonte = fonte.deriveFont(Font.ITALIC);
        } else if (tarefa.isConcluida()) {
            fundo = COR_CONCLUIDA;
        } else if (tarefa.isReagendada()) {
            fundo = COR_REAGENDADA;
        }

        celula.setBackground(fundo);
        celula.setForeground(texto);
        celula.setFont(fonte);
        return celula;
    }

    // Cria um quadradinho colorido com texto para a legenda
    public static JLabel criarItemLegenda(String texto, Color cor) {
        JLabel item = new JLabel("  " + texto + "  ");
        item.setOpaque(true);
        item.setBackground(cor);
        item.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return item;
    }
}