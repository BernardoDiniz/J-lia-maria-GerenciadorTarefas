package br.com.julia.gerenciadortarefas.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Tarefa {

    private Integer id;
    private String titulo;
    private String descricao;
    private Prioridade prioridade;
    private LocalDate prazo;               // opcional: pode ser null
    private StatusTarefa status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataConclusao;
    private LocalDateTime dataExclusao;    // null = ativa; preenchida = excluída
    private List<Etapa> etapas = new ArrayList<>();
    private List<Reagendamento> reagendamentos = new ArrayList<>();

    public Tarefa() {
        this.status = StatusTarefa.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
    }

    // ===================== Regras de negócio =====================

    public Dificuldade getDificuldade() {
        return Dificuldade.classificar(etapas.size());
    }

    public void adicionarEtapa(String descricaoEtapa) {
        etapas.add(new Etapa(descricaoEtapa, etapas.size() + 1));
    }

    public void reagendar(LocalDate novoPrazo) {
        reagendamentos.add(new Reagendamento(prazo, novoPrazo, LocalDateTime.now()));
        this.prazo = novoPrazo;
    }

    public void concluir() {
        this.status = StatusTarefa.CONCLUIDA;
        this.dataConclusao = LocalDateTime.now();
    }

    public void excluir() {
        this.dataExclusao = LocalDateTime.now();
    }

    public void restaurar() {
        this.dataExclusao = null;
    }

    public boolean isConcluida() { return status == StatusTarefa.CONCLUIDA; }
    public boolean isExcluida() { return dataExclusao != null; }
    public boolean isReagendada() { return !reagendamentos.isEmpty(); }

    // ===================== Getters e setters =====================

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }

    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }

    public StatusTarefa getStatus() { return status; }
    public void setStatus(StatusTarefa status) { this.status = status; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }

    public LocalDateTime getDataExclusao() { return dataExclusao; }
    public void setDataExclusao(LocalDateTime dataExclusao) { this.dataExclusao = dataExclusao; }

    public List<Etapa> getEtapas() { return etapas; }
    public void setEtapas(List<Etapa> etapas) { this.etapas = etapas; }

    public List<Reagendamento> getReagendamentos() { return reagendamentos; }
    public void setReagendamentos(List<Reagendamento> reagendamentos) { this.reagendamentos = reagendamentos; }
}