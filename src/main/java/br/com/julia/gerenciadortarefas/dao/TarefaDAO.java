package br.com.julia.gerenciadortarefas.dao;

import br.com.julia.gerenciadortarefas.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TarefaDAO {

    // ===================== CADASTRAR =====================

    public void inserir(Tarefa tarefa) throws SQLException {
        String sqlTarefa = """
            INSERT INTO tarefa (titulo, descricao, prioridade, prazo, status, data_criacao)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        String sqlEtapa = "INSERT INTO etapa (tarefa_id, descricao, ordem) VALUES (?, ?, ?)";

        try (Connection conexao = ConexaoBD.getConexao()) {
            conexao.setAutoCommit(false); // inicia uma transação
            try {
                // 1) Grava a tarefa
                try (PreparedStatement ps = conexao.prepareStatement(sqlTarefa)) {
                    ps.setString(1, tarefa.getTitulo());
                    ps.setString(2, tarefa.getDescricao());
                    ps.setString(3, tarefa.getPrioridade().name());
                    ps.setString(4, paraTexto(tarefa.getPrazo()));
                    ps.setString(5, tarefa.getStatus().name());
                    ps.setString(6, paraTexto(tarefa.getDataCriacao()));
                    ps.executeUpdate();
                }

                // 2) Descobre o id que o banco gerou para essa tarefa
                try (Statement st = conexao.createStatement();
                     ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    rs.next();
                    tarefa.setId(rs.getInt(1));
                }

                // 3) Grava as etapas ligadas a esse id
                try (PreparedStatement ps = conexao.prepareStatement(sqlEtapa)) {
                    for (Etapa etapa : tarefa.getEtapas()) {
                        ps.setInt(1, tarefa.getId());
                        ps.setString(2, etapa.getDescricao());
                        ps.setInt(3, etapa.getOrdem());
                        ps.executeUpdate();
                    }
                }

                conexao.commit();   // confirma tudo de uma vez
            } catch (SQLException e) {
                conexao.rollback(); // desfaz tudo se algo falhou
                throw e;
            }
        }
    }

    // ===================== LISTAR =====================

    public List<Tarefa> listarAtivas() throws SQLException {
        String sql = "SELECT * FROM tarefa WHERE data_exclusao IS NULL ORDER BY id";
        List<Tarefa> tarefas = new ArrayList<>();

        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tarefa t = montarTarefa(rs);
                t.setEtapas(buscarEtapas(conexao, t.getId()));
                t.setReagendamentos(buscarReagendamentos(conexao, t.getId()));
                tarefas.add(t);
            }
        }
        return tarefas;
    }

    // ===================== Métodos auxiliares =====================

    private Tarefa montarTarefa(ResultSet rs) throws SQLException {
        Tarefa t = new Tarefa();
        t.setId(rs.getInt("id"));
        t.setTitulo(rs.getString("titulo"));
        t.setDescricao(rs.getString("descricao"));
        t.setPrioridade(Prioridade.valueOf(rs.getString("prioridade")));
        t.setPrazo(paraData(rs.getString("prazo")));
        t.setStatus(StatusTarefa.valueOf(rs.getString("status")));
        t.setDataCriacao(paraDataHora(rs.getString("data_criacao")));
        t.setDataConclusao(paraDataHora(rs.getString("data_conclusao")));
        t.setDataExclusao(paraDataHora(rs.getString("data_exclusao")));
        return t;
    }

    private List<Etapa> buscarEtapas(Connection conexao, int tarefaId) throws SQLException {
        String sql = "SELECT * FROM etapa WHERE tarefa_id = ? ORDER BY ordem";
        List<Etapa> etapas = new ArrayList<>();
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, tarefaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Etapa e = new Etapa(rs.getString("descricao"), rs.getInt("ordem"));
                    e.setId(rs.getInt("id"));
                    etapas.add(e);
                }
            }
        }
        return etapas;
    }

    private List<Reagendamento> buscarReagendamentos(Connection conexao, int tarefaId) throws SQLException {
        String sql = "SELECT * FROM reagendamento WHERE tarefa_id = ? ORDER BY id";
        List<Reagendamento> lista = new ArrayList<>();
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, tarefaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reagendamento r = new Reagendamento(
                            paraData(rs.getString("prazo_anterior")),
                            paraData(rs.getString("prazo_novo")),
                            paraDataHora(rs.getString("data_reagendamento")));
                    r.setId(rs.getInt("id"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    // Conversões entre Java e texto do banco (null continua null)
    private static String paraTexto(LocalDate data) {
        return data == null ? null : data.toString();
    }

    private static String paraTexto(LocalDateTime dataHora) {
        return dataHora == null ? null : dataHora.truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private static LocalDate paraData(String texto) {
        return texto == null ? null : LocalDate.parse(texto);
    }

    private static LocalDateTime paraDataHora(String texto) {
        return texto == null ? null : LocalDateTime.parse(texto);
    }
}