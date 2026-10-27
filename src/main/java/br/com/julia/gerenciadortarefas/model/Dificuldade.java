package br.com.julia.gerenciadortarefas.model;

public enum Dificuldade {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta");

    private final String descricao;

    Dificuldade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // Regra de negócio: 1 a 3 etapas = baixa; 4 a 6 = média; 7 ou mais = alta
    public static Dificuldade classificar(int quantidadeEtapas) {
        if (quantidadeEtapas < 1) {
            throw new IllegalArgumentException("A tarefa precisa ter pelo menos 1 etapa.");
        }
        if (quantidadeEtapas <= 3) {
            return BAIXA;
        }
        if (quantidadeEtapas <= 6) {
            return MEDIA;
        }
        return ALTA;
    }

    @Override
    public String toString() {
        return descricao;
    }
}