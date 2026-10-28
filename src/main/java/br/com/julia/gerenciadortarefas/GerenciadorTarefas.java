package br.com.julia.gerenciadortarefas;

import br.com.julia.gerenciadortarefas.dao.InicializadorBD;
import br.com.julia.gerenciadortarefas.view.TelaPrincipal;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GerenciadorTarefas {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // se falhar, mantém o visual padrão do Java
        }

        // Textos dos botões padrão das caixas de mensagem em português
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");

        try {
            InicializadorBD.criarTabelas();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Não foi possível abrir o banco:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }
}