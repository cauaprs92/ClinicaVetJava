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

O projeto começou como um sistema de terminal em Java para praticar **Programação Orientada a Objetos**, que continua em `console-app/` como referência de estudo.

---

## ▶️ Para rodar o projeto

O banco fica no **MySQL Server 8.0**, ligado e desligado pelo painel do **XAMPP**.

> [!IMPORTANT]
> Com o **MySQL ligado** (no painel do XAMPP, botão **Start** do MySQL), abra o **terminal na pasta do projeto** e rode:
>
> ```powershell
> .\mvnw.cmd spring-boot:run
> ```
>
> Quando aparecer `Banco de dados pronto`, acesse **http://localhost:8082**.
>
> Para ver as tabelas, use o botão **Admin** do MySQL no XAMPP (phpMyAdmin).

### Preparar uma máquina nova

1. Instale o **JDK 25** marcando a opção que configura o `JAVA_HOME`.
2. Instale o **MySQL Server 8.0** (serviço do Windows `MySQL80`, porta 3306). O projeto usa o usuário `root` sem senha; se o seu tiver senha, coloque-a no `banco.properties` (modelo em `banco.example.properties`).
3. Instale o **XAMPP** e faça o botão MySQL do painel controlar o MySQL Server 8.0: com o painel fechado, abra o `C:\xampp\xampp-control.ini` **como administrador** e adicione no final:

   ```ini
   [ServiceNames]
   MySQL=MySQL80
   ```

   No painel, **não desmarque a caixa `Svc`** do MySQL: ela remove o serviço do MySQL Server 8.0 do Windows.
4. Para o phpMyAdmin não mostrar o aviso `Access denied for user 'pma'`, rode no MySQL o script `C:\xampp\phpMyAdmin\sql\create_tables.sql` e depois:

   ```sql
   CREATE USER 'pma'@'localhost' IDENTIFIED WITH mysql_native_password BY '';
   GRANT SELECT, INSERT, UPDATE, DELETE ON phpmyadmin.* TO 'pma'@'localhost';
   ```

O banco `clinicavet` e os dados de exemplo são criados sozinhos na primeira vez que o servidor roda.

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

**Cauã Rodrigo**

GitHub: [@cauaprs92](https://github.com/cauaprs92)

---

<p align="center">
  🐾 <strong>ClinicaVet</strong> — Organizando o cuidado com quem não pode falar. 🐾
</p>
