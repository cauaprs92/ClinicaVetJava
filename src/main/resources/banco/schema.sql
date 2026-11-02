-- ==========================================================
-- ClinicaVet · estrutura do banco de dados (PostgreSQL)
-- Executado toda vez que o servidor inicia: só cria o que ainda não existe.
-- ==========================================================

-- Os 10 horários da agenda (5 de manhã e 5 à tarde), os mesmos do app de terminal.
CREATE TABLE IF NOT EXISTS horario (
    numero  INTEGER    PRIMARY KEY CHECK (numero > 0),
    hora    TIME       NOT NULL UNIQUE,
    turno   VARCHAR(5) NOT NULL CHECK (turno IN ('manha', 'tarde'))
);

-- Tutores (donos dos animais). O e-mail identifica a pessoa: uma nova consulta
-- com o mesmo e-mail reaproveita o cadastro (e atualiza nome e telefone).
CREATE TABLE IF NOT EXISTS tutor (
    id        SERIAL PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    email     VARCHAR(160) NOT NULL UNIQUE,
    telefone  VARCHAR(40)  NOT NULL
);

-- Pacientes. Uma tabela só para as duas espécies (herança "tabela única"):
-- cachorro tem porte, gato tem temperamento. O mesmo tutor não tem dois
-- animais com o mesmo nome, então o histórico do paciente fica junto.
CREATE TABLE IF NOT EXISTS animal (
    id            SERIAL PRIMARY KEY,
    tutor_id      INTEGER     NOT NULL REFERENCES tutor (id),
    nome          VARCHAR(80) NOT NULL,
    especie       VARCHAR(10) NOT NULL CHECK (especie IN ('cachorro', 'gato')),
    idade         INTEGER     NOT NULL CHECK (idade BETWEEN 0 AND 40),
    porte         VARCHAR(12),
    temperamento  VARCHAR(12),
    UNIQUE (tutor_id, nome),
    CHECK ((especie = 'cachorro' AND porte IS NOT NULL AND temperamento IS NULL)
        OR (especie = 'gato' AND temperamento IS NOT NULL AND porte IS NULL))
);

-- Consultas. O código mostrado no site (#0001) é o id com 4 dígitos.
-- Cada horário de cada dia recebe no máximo uma consulta.
CREATE TABLE IF NOT EXISTS consulta (
    id             SERIAL PRIMARY KEY,
    horario        INTEGER      NOT NULL REFERENCES horario (numero),
    data           DATE         NOT NULL,
    animal_id      INTEGER      NOT NULL REFERENCES animal (id),
    status         VARCHAR(12)  NOT NULL DEFAULT 'agendada'
                   CHECK (status IN ('agendada', 'atendimento', 'finalizada')),
    sintomas       VARCHAR(500) NOT NULL,
    criada_em      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    iniciada_em    TIMESTAMPTZ,
    finalizada_em  TIMESTAMPTZ,
    UNIQUE (data, horario)
);

CREATE INDEX IF NOT EXISTS idx_consulta_status ON consulta (status);
CREATE INDEX IF NOT EXISTS idx_consulta_animal ON consulta (animal_id);

-- Itens da farmácia (vacinas e medicamentos) e o estoque atual.
CREATE TABLE IF NOT EXISTS produto (
    id          SERIAL PRIMARY KEY,
    nome        VARCHAR(80)   NOT NULL UNIQUE,
    preco       NUMERIC(10,2) NOT NULL CHECK (preco >= 0),
    quantidade  INTEGER       NOT NULL CHECK (quantidade >= 0)
);

-- Cada item lançado na conta de uma consulta. O preço fica gravado como era
-- no momento do lançamento. O total da farmácia de uma consulta é a soma destas linhas.
CREATE TABLE IF NOT EXISTS consumo (
    id              SERIAL PRIMARY KEY,
    consulta_id     INTEGER       NOT NULL REFERENCES consulta (id) ON DELETE CASCADE,
    produto_id      INTEGER       NOT NULL REFERENCES produto (id),
    quantidade      INTEGER       NOT NULL CHECK (quantidade > 0),
    preco_unitario  NUMERIC(10,2) NOT NULL CHECK (preco_unitario >= 0),
    lancado_em      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_consumo_consulta ON consumo (consulta_id);
