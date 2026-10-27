package br.com.julia.gerenciadortarefas.service;

import br.com.julia.gerenciadortarefas.dao.TarefaDAO;
import br.com.julia.gerenciadortarefas.model.Reagendamento;
import br.com.julia.gerenciadortarefas.model.Tarefa;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TarefaService {

    private final TarefaDAO dao = new TarefaDAO();

    public void cadastrar(Tarefa tarefa) throws SQLException {
        validar(tarefa);
        dao.inserir(tarefa);
    }

    public void editar(Tarefa tarefa) throws SQLException {
        if (tarefa.isConcluida()) {
            throw new IllegalArgumentException("Tarefas concluídas não podem ser editadas.");
        }
        validar(tarefa);
        dao.atualizar(tarefa);
    }

    public void reagendar(Tarefa tarefa, LocalDate novoPrazo) throws SQLException {
        if (novoPrazo == null) {
            throw new IllegalArgumentException("Informe o novo prazo.");
        }
        if (tarefa.isConcluida()) {
            throw new IllegalArgumentException("Tarefas concluídas não podem ser reagendadas.");
        }
        if (novoPrazo.equals(tarefa.getPrazo())) {
            throw new IllegalArgumentException("O novo prazo é igual ao prazo atual.");
        }
        if (novoPrazo.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("O novo prazo não pode ser uma data que já passou.");
        }
        Reagendamento registro = tarefa.reagendar(novoPrazo);
        dao.atualizarPrazo(tarefa, registro);
    }

    public List<Tarefa> listarAtivas() throws SQLException {
        return dao.listarAtivas();
    }

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