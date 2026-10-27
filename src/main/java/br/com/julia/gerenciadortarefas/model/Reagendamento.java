package br.com.julia.gerenciadortarefas.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reagendamento {

    private Integer id;
    private LocalDate prazoAnterior;
    private LocalDate prazoNovo;
    private LocalDateTime dataReagendamento;

    public Reagendamento() {
    }

    public Reagendamento(LocalDate prazoAnterior, LocalDate prazoNovo, LocalDateTime dataReagendamento) {
        this.prazoAnterior = prazoAnterior;
        this.prazoNovo = prazoNovo;
        this.dataReagendamento = dataReagendamento;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getPrazoAnterior() { return prazoAnterior; }
    public void setPrazoAnterior(LocalDate prazoAnterior) { this.prazoAnterior = prazoAnterior; }

    public LocalDate getPrazoNovo() { return prazoNovo; }
    public void setPrazoNovo(LocalDate prazoNovo) { this.prazoNovo = prazoNovo; }

    public LocalDateTime getDataReagendamento() { return dataReagendamento; }
    public void setDataReagendamento(LocalDateTime dataReagendamento) { this.dataReagendamento = dataReagendamento; }
}