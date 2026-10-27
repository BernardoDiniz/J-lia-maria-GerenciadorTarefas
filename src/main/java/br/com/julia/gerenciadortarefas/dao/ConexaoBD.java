package br.com.julia.gerenciadortarefas.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoBD {

    // Endereço do banco: o arquivo será criado na pasta do projeto
    private static final String URL = "jdbc:sqlite:gerenciador_tarefas.db";

    public static Connection getConexao() throws SQLException {
        Connection conexao = DriverManager.getConnection(URL);

        // O SQLite vem com a verificação de chaves estrangeiras desligada.
        // Este comando liga a verificação para esta conexão.
        try (Statement comando = conexao.createStatement()) {
            comando.execute("PRAGMA foreign_keys = ON");
        }
        return conexao;
    }
}