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
        String sql = """
            INSERT INTO tarefa (titulo, descricao, prioridade, prazo, status, data_criacao)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conexao = ConexaoBD.getConexao()) {
            conexao.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                    ps.setString(1, tarefa.getTitulo());
                    ps.setString(2, tarefa.getDescricao());
                    ps.setString(3, tarefa.getPrioridade().name());
                    ps.setString(4, paraTexto(tarefa.getPrazo()));
                    ps.setString(5, tarefa.getStatus().name());
                    ps.setString(6, paraTexto(tarefa.getDataCriacao()));
                    ps.executeUpdate();
                }

                try (Statement st = conexao.createStatement();
                     ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    rs.next();
                    tarefa.setId(rs.getInt(1));
                }

                inserirEtapas(conexao, tarefa);
                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    // ===================== EDITAR =====================

    public void atualizar(Tarefa tarefa) throws SQLException {
        String sql = "UPDATE tarefa SET titulo = ?, descricao = ?, prioridade = ? WHERE id = ?";

        try (Connection conexao = ConexaoBD.getConexao()) {
            conexao.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                    ps.setString(1, tarefa.getTitulo());
                    ps.setString(2, tarefa.getDescricao());
                    ps.setString(3, tarefa.getPrioridade().name());
                    ps.setInt(4, tarefa.getId());
                    ps.executeUpdate();
                }

                // Substitui as etapas antigas pela lista atual
                try (PreparedStatement ps = conexao.prepareStatement(
                        "DELETE FROM etapa WHERE tarefa_id = ?")) {
                    ps.setInt(1, tarefa.getId());
                    ps.executeUpdate();
                }
                inserirEtapas(conexao, tarefa);

                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    // ===================== REAGENDAR =====================

    public void atualizarPrazo(Tarefa tarefa, Reagendamento registro) throws SQLException {
        try (Connection conexao = ConexaoBD.getConexao()) {
            conexao.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conexao.prepareStatement(
                        "UPDATE tarefa SET prazo = ? WHERE id = ?")) {
                    ps.setString(1, paraTexto(tarefa.getPrazo()));
                    ps.setInt(2, tarefa.getId());
                    ps.executeUpdate();
                }

                // Só grava histórico se havia um prazo anterior
                if (registro != null) {
                    String sql = """
                        INSERT INTO reagendamento (tarefa_id, prazo_anterior, prazo_novo, data_reagendamento)
                        VALUES (?, ?, ?, ?)
                        """;
                    try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                        ps.setInt(1, tarefa.getId());
                        ps.setString(2, paraTexto(registro.getPrazoAnterior()));
                        ps.setString(3, paraTexto(registro.getPrazoNovo()));
                        ps.setString(4, paraTexto(registro.getDataReagendamento()));
                        ps.executeUpdate();
                    }
                }

                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

        // ===================== CONCLUIR =====================

    public void marcarConcluida(Tarefa tarefa) throws SQLException {
        String sql = "UPDATE tarefa SET status = ?, data_conclusao = ? WHERE id = ?";
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, tarefa.getStatus().name());
            ps.setString(2, paraTexto(tarefa.getDataConclusao()));
            ps.setInt(3, tarefa.getId());
            ps.executeUpdate();
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

    private void inserirEtapas(Connection conexao, Tarefa tarefa) throws SQLException {
        String sql = "INSERT INTO etapa (tarefa_id, descricao, ordem) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            for (Etapa etapa : tarefa.getEtapas()) {
                ps.setInt(1, tarefa.getId());
                ps.setString(2, etapa.getDescricao());
                ps.setInt(3, etapa.getOrdem());
                ps.executeUpdate();
            }
        }
    }

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