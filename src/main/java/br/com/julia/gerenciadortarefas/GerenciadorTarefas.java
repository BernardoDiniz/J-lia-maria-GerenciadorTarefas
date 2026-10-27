package br.com.julia.gerenciadortarefas;

import br.com.julia.gerenciadortarefas.dao.InicializadorBD;
import br.com.julia.gerenciadortarefas.dao.TarefaDAO;
import br.com.julia.gerenciadortarefas.model.Prioridade;
import br.com.julia.gerenciadortarefas.model.Tarefa;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class GerenciadorTarefas {

    public static void main(String[] args) {
        try {
            InicializadorBD.criarTabelas();
            TarefaDAO dao = new TarefaDAO();

            // 1) Cadastrar uma tarefa COM prazo
            Tarefa t1 = new Tarefa();
            t1.setTitulo("Entregar Projeto Integrado");
            t1.setDescricao("Relatorio final no AVA");
            t1.setPrioridade(Prioridade.ALTA);
            t1.setPrazo(LocalDate.of(2026, 11, 30));
            t1.adicionarEtapa("Requisitos");
            t1.adicionarEtapa("Modelagem");
            t1.adicionarEtapa("Prototipo");
            t1.adicionarEtapa("Codigo");
            t1.adicionarEtapa("Testes");
            dao.inserir(t1);
            System.out.println("Tarefa salva com id " + t1.getId());

            // 2) Cadastrar uma tarefa SEM prazo
            Tarefa t2 = new Tarefa();
            t2.setTitulo("Organizar a mesa");
            t2.setPrioridade(Prioridade.BAIXA);
            t2.adicionarEtapa("Separar papeis");
            dao.inserir(t2);
            System.out.println("Tarefa salva com id " + t2.getId());

            // 3) Ler de volta do banco
            List<Tarefa> tarefas = dao.listarAtivas();
            System.out.println("\n=== Tarefas no banco: " + tarefas.size() + " ===");
            for (Tarefa t : tarefas) {
                System.out.println("#" + t.getId() + " | " + t.getTitulo()
                        + " | Prioridade: " + t.getPrioridade()
                        + " | Etapas: " + t.getEtapas().size()
                        + " | Dificuldade: " + t.getDificuldade()
                        + " | Prazo: " + (t.getPrazo() == null ? "sem prazo" : t.getPrazo())
                        + " | Status: " + t.getStatus());
            }
        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}