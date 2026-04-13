CREATE TABLE IF NOT EXISTS servico (
    id TEXT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL,
    duracao INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS agendamento (
    id TEXT PRIMARY KEY,
    servico_id TEXT NOT NULL,
    nome_cliente VARCHAR(255) NOT NULL,
    telefone_cliente VARCHAR(20) NOT NULL,
    data_hora_inicio TIMESTAMP NOT NULL,
    data_hora_fim TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    google_event_id VARCHAR(255),
    FOREIGN KEY (servico_id) REFERENCES servico(id)
);