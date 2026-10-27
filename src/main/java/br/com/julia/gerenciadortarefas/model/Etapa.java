package br.com.julia.gerenciadortarefas.model;

public class Etapa {

    private Integer id;
    private String descricao;
    private int ordem;

    public Etapa() {
    }

    public Etapa(String descricao, int ordem) {
        this.descricao = descricao;
        this.ordem = ordem;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
}