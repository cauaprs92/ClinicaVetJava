<div align="center">

# 🐾 ClinicaVet

### Sistema de agenda de clínica veterinária com farmácia

<br/>

[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-JDBC-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com)
[![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white)](https://developer.mozilla.org/docs/Web/HTML)
[![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white)](https://developer.mozilla.org/docs/Web/CSS)
[![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black)](https://developer.mozilla.org/docs/Web/JavaScript)

</div>

---

## 📋 Sobre o projeto

O **ClinicaVet** roda **localmente**, na sua máquina: um servidor Java (Spring Boot) entrega o site em `http://localhost:8082` e grava tudo em um banco **MySQL**. Qualquer aba ou navegador do computador vê as mesmas consultas.

O projeto começou como um sistema de terminal em Java para praticar **Programação Orientada a Objetos**, que continua em `console-app/` como referência de estudo. Para rodar a versão web, siga o [passo a passo abaixo](#-rodando-localmente).

---

## ✨ O que dá pra fazer no site

- ✅ Agendar uma consulta em uma **data e horário**: a consulta ganha um **código** único (#0001, #0002…) e começa com o status **Agendada**
- ✅ Agendar em qualquer dia a partir de hoje: só aparecem os horários livres na data escolhida, e cada horário recebe no máximo uma consulta
- ✅ Cadastrar o **tutor** (nome, e-mail e telefone) e o **paciente**: cachorro (com porte) ou gato (com temperamento), com idade e sintomas
- ✅ Iniciar o atendimento no dia da consulta: o status passa para **Em atendimento**
- ✅ Finalizar a consulta confirmando o pagamento da farmácia: o status passa para **Finalizada** e ela fica no histórico do paciente
- ✅ Cancelar uma consulta agendada
- ✅ Editar os dados do tutor e do animal, ou remarcar data e horário
- ✅ Agendar o **retorno** de um paciente com os dados já preenchidos
- ✅ Telefone sempre no formato `(11) 90000-0000` (celular) ou `(11) 3000-0000` (fixo)
- ✅ Buscar consultas pelo código (`#0003`), animal, tutor, telefone ou data, filtrar por status, espécie e farmácia, e ordenar
- ✅ Lançar várias vacinas e medicamentos de uma vez em uma consulta em atendimento
- ✅ Ver a agenda de qualquer dia (livre, agendada, em atendimento ou finalizada) e o estoque da farmácia

```
Agendada ──iniciar atendimento──▶ Em atendimento ──finalizar (pagamento da farmácia)──▶ Finalizada
    └──cancelar consulta──▶ (removida)
```

---

## 💻 Rodando localmente

### 1. Instale o que precisa

| Programa | Para quê | Onde baixar |
| --- | --- | --- |
| **Git** | Baixar o projeto | [git-scm.com](https://git-scm.com/downloads) |
| **JDK 25** | Rodar o servidor Java | [Adoptium Temurin 25](https://adoptium.net/temurin/releases/?version=25) |
| **MySQL** | O banco de dados | [XAMPP](https://www.apachefriends.org/pt_br/index.html) (já vem com o MySQL) ou [MySQL Community Server](https://dev.mysql.com/downloads/mysql/) |
| **MySQL Workbench** (opcional) | Ver as tabelas e rodar os scripts | [dev.mysql.com/downloads/workbench](https://dev.mysql.com/downloads/workbench/) |

Sobre o MySQL:
- **XAMPP:** o usuário é `root` e vem **sem senha**. A porta é a padrão, **3306**.
- **MySQL Community Server:** **anote a senha** que você criar para o usuário `root`. Ela vai ser usada no passo 5.

Para conferir o Java, abra um terminal e rode `java -version`. Precisa aparecer **25**.

> 💡 Se aparecer outra versão, mas você usa o VS Code com a extensão **Language Support for Java (Red Hat)**, ela já traz um JDK 25. Aponte o `JAVA_HOME` para ele (no PowerShell, ajuste o número da versão da extensão):
> ```powershell
> $env:JAVA_HOME = "$env:USERPROFILE\.vscode\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64"
> ```
> Para não precisar repetir em todo terminal, use `setx JAVA_HOME "<o mesmo caminho>"` uma vez e abra um terminal novo.

### 2. Baixe o projeto

```bash
git clone https://github.com/cauaprs92/ClinicaVetJava.git
cd ClinicaVetJava
```

### 3. Ligue o MySQL

- **XAMPP:** abra o *XAMPP Control Panel* e clique em **Start** na linha **MySQL**.
- **MySQL Community Server:** ele já liga sozinho com o Windows (serviço **MySQL80**).

### 4. Crie o banco `clinicavet`

Os scripts do banco ficam na pasta [`docs/`](docs/). Rode os dois, nesta ordem:

| Arquivo | O que faz |
| --- | --- |
| [`docs/codigo.mysql`](docs/codigo.mysql) | Cria o banco `clinicavet` e as tabelas |
| [`docs/insert.mysql`](docs/insert.mysql) | Insere os 10 horários, os itens da farmácia e consultas de exemplo |

**Pelo MySQL Workbench:** conecte no servidor local, vá em *File → Open SQL Script…*, abra o `docs/codigo.mysql` e clique no raio (*Execute*). Depois faça o mesmo com o `docs/insert.mysql`.

**Ou pelo terminal** (na pasta do projeto; no XAMPP o `mysql` fica em `C:\xampp\mysql\bin`):
```bash
mysql -u root -p < docs/codigo.mysql
mysql -u root -p < docs/insert.mysql
```

> Se você pular este passo, o servidor cria o banco, as tabelas e os dados de exemplo sozinho na primeira vez que iniciar, usando esses mesmos arquivos.

### 5. Coloque a senha do banco

Copie o arquivo de modelo:

```bash
# Windows (PowerShell)
Copy-Item banco.example.properties banco.properties

# Linux / macOS
cp banco.example.properties banco.properties
```

Abra o `banco.properties` e coloque a senha do `root`. **No XAMPP, deixe a senha vazia**, como já vem no modelo:

```properties
clinicavet.bd.url=jdbc:mysql://localhost:3306/clinicavet
clinicavet.bd.usuario=root
clinicavet.bd.senha=
```

O `banco.properties` fica fora do git, então sua senha não vai para o GitHub.

### 6. Inicie o servidor

Na pasta do projeto:

```bash
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Na primeira vez demora um pouco, porque o Maven baixa as dependências. Espere aparecer:

```
Banco de dados pronto em jdbc:mysql://localhost:3306/clinicavet
```

### 7. Abra o site

Acesse **http://localhost:8082**. Tudo o que você fizer fica gravado no MySQL. Dá para ver as tabelas pelo MySQL Workbench (ou pelo phpMyAdmin do XAMPP, em `http://localhost/phpmyadmin`), no banco `clinicavet`.

Para desligar o servidor, aperte **Ctrl + C** no terminal. Os dados continuam salvos no banco; da próxima vez, é só ligar o MySQL e repetir o passo 6. O botão **Restaurar dados de exemplo**, no rodapé do site, apaga tudo e roda o `docs/insert.mysql` de novo.

> A JavaHotel usa a porta 8081 e a ClinicaVet a 8082, então os dois projetos podem rodar ao mesmo tempo.

### Se algo der errado

| Mensagem | O que fazer |
| --- | --- |
| *O banco de dados não existe* ou *As tabelas do banco de dados não existem* | Rode o `docs/codigo.mysql` (passo 4). |
| *Usuário ou senha do MySQL incorretos* | Confira a senha no `banco.properties` (passo 5). |
| *Não foi possível conectar ao MySQL* | O MySQL está desligado. Faça o passo 3. |
| No XAMPP, o MySQL não liga (*Port 3306 in use*) | Já existe outro MySQL usando a porta 3306. Use esse, ou pare o serviço dele em *Serviços* do Windows. |
| `Duplicate entry` ao rodar o `insert.mysql` | Os dados de exemplo já foram inseridos. Para recomeçar, use o botão **Restaurar dados de exemplo** no site. |
| *Port 8082 was already in use* | O servidor já está rodando em outro terminal. Use esse ou feche ele. |
| `release version 25 not supported` ou erro de versão do Java | O Maven está usando um Java antigo. Veja a dica do `JAVA_HOME` no passo 1. |
| Na página: *Não foi possível carregar os dados do banco de dados* | O servidor não está rodando, ou o banco está com problema. Veja a mensagem no terminal. |

---

## 🧱 Como funciona

```
Navegador (public/)            Servidor Java (Spring Boot)                        Banco
┌────────────────────┐  HTTP  ┌─────────────────────┐   ┌────────────────┐  JDBC  ┌────────────┐
│ index, agenda,     │ ─────▶ │ ClinicaApiController│ ─▶│ ClinicaService │ ─────▶ │   MySQL    │
│ consultas, farmácia│ ◀───── │   (/api/...)        │   │   (regras)     │  DAOs  │ clinicavet │
│ app.js             │  JSON  └─────────────────────┘   └────────────────┘        └────────────┘
└────────────────────┘
```

- **Navegador** (`public/app.js`): busca tudo em `GET /api/estado` e envia cada ação (agendar, iniciar atendimento, lançar itens…) para a API. Mostra a mensagem que o servidor devolve.
- **ClinicaService**: confere as regras da clínica (horário livre, data, telefone, espécie, estoque, pagamento) e faz cada operação dentro de uma **transação**: se algo falhar no meio, nada é gravado (`rollback`). Antes de alterar, pega uma trava do MySQL (`GET_LOCK`), para que duas pessoas não agendem o mesmo horário ao mesmo tempo.
- **DAOs** (`com.clinica.dao`): todo o acesso ao banco, com JDBC puro: `DriverManager`, `Connection`, `PreparedStatement` e `ResultSet`. Os erros de banco (`SQLException`) viram mensagens claras para a tela.

### Tabelas

| Tabela     | Guarda                                                                          |
| ---------- | ------------------------------------------------------------------------------- |
| `horario`  | Os 10 horários do dia e o turno de cada um (manhã ou tarde)                     |
| `tutor`    | Nome, e-mail e telefone (o e-mail identifica a pessoa)                          |
| `animal`   | Nome, espécie, idade e porte (cachorro) ou temperamento (gato) de cada paciente |
| `consulta` | Horário, data, paciente, status, sintomas e as datas de início e fim            |
| `produto`  | Vacinas e medicamentos da farmácia, preço e estoque                             |
| `consumo`  | Cada item lançado na conta de uma consulta, com o preço do momento              |

Cada horário de cada dia recebe no máximo uma consulta (`UNIQUE (data, horario)`). O total da farmácia de uma consulta é a soma das linhas de `consumo`. A estrutura completa está em [`docs/codigo.mysql`](docs/codigo.mysql) e os dados de exemplo em [`docs/insert.mysql`](docs/insert.mysql).

### API

| Método   | Rota                             | O que faz                                    |
| -------- | -------------------------------- | -------------------------------------------- |
| `GET`    | `/api/estado`                    | Horários, consultas e itens da farmácia      |
| `POST`   | `/api/consultas`                 | Nova consulta (Agendada)                     |
| `PUT`    | `/api/consultas/{id}`            | Edita tutor, paciente, sintomas, data e hora |
| `POST`   | `/api/consultas/{id}/iniciar`    | Agendada → Em atendimento                    |
| `POST`   | `/api/consultas/{id}/finalizar`  | Em atendimento → Finalizada                  |
| `DELETE` | `/api/consultas/{id}`            | Cancela uma consulta agendada                |
| `POST`   | `/api/consumos`                  | Lança itens da farmácia                      |
| `POST`   | `/api/redefinir`                 | Restaura os dados de exemplo                 |

As alterações respondem `{ "ok": true, "msg": "..." }`; quando uma regra não é atendida, `{ "ok": false, "msg": "..." }` com status 400.

---

## 📁 Estrutura do projeto

```
ClinicaVetJava/
├── docs/
│   ├── codigo.mysql                 # Cria o banco clinicavet e as tabelas
│   └── insert.mysql                 # Horários, farmácia e consultas de exemplo
├── public/                          # Site (HTML, CSS e app.js)
├── src/main/java/com/clinica/
│   ├── controller/                  # API REST (ClinicaApiController) e tratamento de erros
│   ├── service/                     # Regras da clínica e transações (ClinicaService)
│   ├── dao/                         # JDBC: ConexaoBD, InicializadorBD e um DAO por tabela
│   └── model/                       # Animal, Cachorro, Gato, Tutor, Consulta, Horario...
├── src/main/resources/
│   └── application.properties       # Porta e configuração do banco
├── banco.example.properties         # Modelo do arquivo com a senha do banco
└── console-app/                     # Primeira versão, em Java puro para terminal (referência de estudo)
```

---

## 🧠 Conceitos de POO

A versão web mantém a mesma estrutura de classes do app de terminal:

| Conceito | Onde aparece |
| --- | --- |
| 🔒 **Encapsulamento** | Atributos `private` com acesso por `get` e `set` (`Animal`, `Consulta`, `Tutor`) |
| 🧬 **Herança** | `Cachorro extends Animal` (porte) e `Gato extends Animal` (temperamento) |
| 🎭 **Abstração** | `public abstract class Animal`, que não pode ser instanciada |
| 🔄 **Polimorfismo** | `Consulta.paciente` é um `Animal` que guarda um `Cachorro` ou um `Gato`; `Animal.criar(...)` devolve a subclasse certa a partir da coluna `especie` |
| ♻️ **Sobrescrita** | `@Override` em `getEspecie()`, `getTipo()`, `getCaracteristica()` e `emitirSom()` |

No banco, cachorros e gatos ficam na mesma tabela `animal` (herança "tabela única"): a coluna `especie` diz qual é, e cada um preenche só a sua característica.

### App de terminal

A primeira versão do projeto continua em `console-app/`:

```bash
javac console-app/*.java
java -cp console-app Main
```

---

## 👨‍💻 Desenvolvedor

**Matheus Ricardo**

GitHub: [@mateusricardodev](https://github.com/mateusricardodev)

---

<p align="center">
  🐾 <strong>ClinicaVet</strong> — Organizando o cuidado com quem não pode falar. 🐾
</p>
