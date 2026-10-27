package br.com.julia.gerenciadortarefas.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InicializadorBD {

    public static void criarTabelas() throws SQLException {

        String sqlTarefa = """
            CREATE TABLE IF NOT EXISTS tarefa (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo         TEXT NOT NULL,
                descricao      TEXT,
                prioridade     TEXT NOT NULL CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA')),
                prazo          TEXT,
                status         TEXT NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'CONCLUIDA')),
                data_criacao   TEXT NOT NULL,
                data_conclusao TEXT,
                data_exclusao  TEXT
            )
            """;

        String sqlEtapa = """
            CREATE TABLE IF NOT EXISTS etapa (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                tarefa_id INTEGER NOT NULL,
                descricao TEXT NOT NULL,
                ordem     INTEGER NOT NULL,
                FOREIGN KEY (tarefa_id) REFERENCES tarefa(id)
            )
            """;

        String sqlReagendamento = """
            CREATE TABLE IF NOT EXISTS reagendamento (
                id                 INTEGER PRIMARY KEY AUTOINCREMENT,
                tarefa_id          INTEGER NOT NULL,
                prazo_anterior     TEXT,
                prazo_novo         TEXT,
                data_reagendamento TEXT NOT NULL,
                FOREIGN KEY (tarefa_id) REFERENCES tarefa(id)
            )
            """;

        try (Connection conexao = ConexaoBD.getConexao();
             Statement comando = conexao.createStatement()) {
            comando.execute(sqlTarefa);
            comando.execute(sqlEtapa);
            comando.execute(sqlReagendamento);
        }
    }
}