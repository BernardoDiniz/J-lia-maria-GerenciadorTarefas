package br.com.julia.gerenciadortarefas.service;

import br.com.julia.gerenciadortarefas.dao.TarefaDAO;
import br.com.julia.gerenciadortarefas.model.Tarefa;
import java.sql.SQLException;
import java.util.List;

public class TarefaService {

    private final TarefaDAO dao = new TarefaDAO();

    public void cadastrar(Tarefa tarefa) throws SQLException {
        validar(tarefa);
        dao.inserir(tarefa);
    }

    public List<Tarefa> listarAtivas() throws SQLException {
        return dao.listarAtivas();
    }

    // Regras que precisam ser verdadeiras antes de gravar
    private void validar(Tarefa tarefa) {
        if (tarefa.getTitulo() == null || tarefa.getTitulo().isBlank()) {
            throw new IllegalArgumentException("Informe o título da tarefa.");
        }
        if (tarefa.getPrioridade() == null) {
            throw new IllegalArgumentException("Selecione a prioridade.");
        }
        if (tarefa.getEtapas().isEmpty()) {
            throw new IllegalArgumentException("Adicione pelo menos 1 etapa.");
        }
    }
}
