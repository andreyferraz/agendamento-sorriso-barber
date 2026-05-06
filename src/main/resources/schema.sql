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
    barbeiro_id TEXT NOT NULL,
    nome_cliente VARCHAR(255) NOT NULL,
    telefone_cliente VARCHAR(20) NOT NULL,
    data_hora_inicio TIMESTAMP NOT NULL,
    data_hora_fim TIMESTAMP NOT NULL,
    status TEXT NOT NULL,
    google_event_id VARCHAR(255),
    FOREIGN KEY (servico_id) REFERENCES servico(id),
    FOREIGN KEY (barbeiro_id) REFERENCES barbeiro(id)
);
CREATE TABLE IF NOT EXISTS transacao_financeira (
    id TEXT PRIMARY KEY,
    tipo TEXT NOT NULL,
    agendamento_id TEXT NOT NULL,
    barbeiro_id TEXT NOT NULL,
    valor NUMERIC NOT NULL,
    data TEXT NOT NULL,
    descricao TEXT,
    FOREIGN KEY (agendamento_id) REFERENCES agendamento(id),
    FOREIGN KEY (barbeiro_id) REFERENCES barbeiro(id)
);
CREATE TABLE IF NOT EXISTS produto (
    id TEXT PRIMARY KEY,
    nome TEXT NOT NULL,
    descricao TEXT,
    preco NUMERIC NOT NULL,
    url_imagem TEXT,
    estoque INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS usuario_admin (
    id TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    senha_hash TEXT NOT NULL,
    foto_url TEXT
);
CREATE TABLE IF NOT EXISTS barbeiro (
    id TEXT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    username TEXT,
    telefone VARCHAR(20),
    email VARCHAR(255),
    foto_url TEXT,
    senha_hash TEXT NOT NULL,
    comissao_percentual DECIMAL(10, 2) NOT NULL,
    horario_inicio_atendimento TEXT,
    horario_fim_atendimento TEXT,
    horarios_segunda TEXT,
    horarios_terca TEXT,
    horarios_quarta TEXT,
    horarios_quinta TEXT,
    horarios_sexta TEXT,
    horarios_sabado TEXT,
    horarios_domingo TEXT,
    horario_segunda_inicio TEXT,
    horario_segunda_fim TEXT,
    horario_terca_inicio TEXT,
    horario_terca_fim TEXT,
    horario_quarta_inicio TEXT,
    horario_quarta_fim TEXT,
    horario_quinta_inicio TEXT,
    horario_quinta_fim TEXT,
    horario_sexta_inicio TEXT,
    horario_sexta_fim TEXT,
    horario_sabado_inicio TEXT,
    horario_sabado_fim TEXT,
    horario_domingo_inicio TEXT,
    horario_domingo_fim TEXT
);