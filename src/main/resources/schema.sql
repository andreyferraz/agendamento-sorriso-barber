CREATE TABLE IF NOT EXISTS servico (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL,
    duracao INTEGER NOT NULL
);