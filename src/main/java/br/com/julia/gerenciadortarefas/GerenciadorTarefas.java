package br.com.julia.gerenciadortarefas;

import br.com.julia.gerenciadortarefas.dao.InicializadorBD;
import br.com.julia.gerenciadortarefas.view.TelaPrincipal;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GerenciadorTarefas {

    public static void main(String[] args) {
        // Usa o visual do Windows nas telas
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // se falhar, mantém o visual padrão do Java
        }

        try {
            InicializadorBD.criarTabelas();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Não foi possível abrir o banco:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Telas Swing devem ser criadas na "thread de eventos" do Swing
        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }
}