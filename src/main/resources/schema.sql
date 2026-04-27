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
    status TEXT NOT NULL,
    google_event_id VARCHAR(255),
    FOREIGN KEY (servico_id) REFERENCES servico(id)
);
CREATE TABLE IF NOT EXISTS transacao_financeira (
    id TEXT PRIMARY KEY,
    tipo TEXT NOT NULL,
    agendamento_id TEXT NOT NULL,
    valor NUMERIC NOT NULL,
    data TEXT NOT NULL,
    descricao TEXT,
    FOREIGN KEY (agendamento_id) REFERENCES agendamento(id)
);
CREATE TABLE IF NOT EXISTS produto (
    id TEXT PRIMARY KEY,
    nome TEXT NOT NULL,
    descricao TEXT,
    preco NUMERIC NOT NULL,
    url_imagem TEXT,
    estoque INTEGER NOT NULL,
    ativo INTEGER NOT NULL DEFAULT 1 CHECK (ativo IN (0,1))
);
CREATE TABLE IF NOT EXISTS usuario_admin (
    id TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    senha_hash TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS barbeiro (
    id TEXT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    email VARCHAR(255),
    senha_hash TEXT NOT NULL,
    comissao_percentual DECIMAL(10, 2) NOT NULL
);