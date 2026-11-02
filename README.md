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

> [!IMPORTANT]
> Com o **MySQL ligado** (no XAMPP, botão **Start**), abra o **terminal na pasta do projeto** e rode:
>
> ```powershell
> .\mvnw.cmd spring-boot:run
> ```
>
> Quando aparecer `Banco de dados pronto`, acesse **http://localhost:8082**.
>
> É a primeira vez? Siga antes o [tutorial completo](#-tutorial-como-rodar-localmente), que prepara o Java e o banco.

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

## 💻 Tutorial: como rodar localmente

Passo a passo para Windows. Todos os comandos são digitados no **terminal aberto na pasta do projeto** (veja como abrir no [passo 3](#passo-3--baixe-o-projeto-e-abra-o-terminal-na-pasta-dele)). Os passos 1 a 6 são feitos **uma vez só**; depois, para usar o sistema no dia a dia, basta o [passo 7](#passo-7--rode-o-projeto).

### Passo 1 · Instale os programas

| Programa | Para quê | Onde baixar |
| --- | --- | --- |
| **Git** | Baixar o projeto | [git-scm.com](https://git-scm.com/downloads) |
| **JDK 25** | Rodar o servidor Java | [Adoptium Temurin 25](https://adoptium.net/temurin/releases/?version=25) |
| **XAMPP** | O banco de dados MySQL | [apachefriends.org](https://www.apachefriends.org/pt_br/index.html) |
| **MySQL Workbench** (opcional) | Ver as tabelas e rodar os scripts | [dev.mysql.com/downloads/workbench](https://dev.mysql.com/downloads/workbench/) |

No XAMPP, o MySQL usa o usuário `root` **sem senha**, na porta **3306**. Se preferir o [MySQL Community Server](https://dev.mysql.com/downloads/mysql/) em vez do XAMPP, **anote a senha** que você criar para o `root`: ela vai ser usada no passo 6.

### Passo 2 · Aponte o JAVA_HOME para o Java 25

O `mvnw.cmd` usa a variável `JAVA_HOME` para achar o Java. Abra um terminal e veja se ela já existe:

```powershell
echo $env:JAVA_HOME
```

Se aparecer o caminho de um JDK 25, pule para o passo 3. Se aparecer vazio, rode o comando abaixo trocando o caminho pela pasta onde o JDK 25 foi instalado:

```powershell
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot"
```

> 💡 No instalador do Adoptium, a opção **Set JAVA_HOME variable** faz isso por você. E se você usa o VS Code com a extensão **Language Support for Java (Red Hat)**, ela já traz um JDK 25 (ajuste o número da versão da extensão):
> ```powershell
> setx JAVA_HOME "$env:USERPROFILE\.vscode\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64"
> ```

O `setx` só vale para terminais abertos depois dele: **feche o terminal e abra outro**. No VS Code, feche e abra o **VS Code inteiro**, senão o terminal dele continua sem o `JAVA_HOME`.

### Passo 3 · Baixe o projeto e abra o terminal na pasta dele

```powershell
git clone https://github.com/cauaprs92/ClinicaVetJava.git
cd ClinicaVetJava
```

Para abrir o terminal já na pasta do projeto depois:

- **Pelo Explorador de Arquivos:** abra a pasta `ClinicaVetJava`, clique com o botão direito em um espaço vazio e escolha **Abrir no Terminal**.
- **Pelo VS Code:** abra a pasta do projeto (*Arquivo → Abrir Pasta…*) e vá em *Terminal → Novo Terminal*.

A pasta certa é a que tem os arquivos `mvnw.cmd` e `pom.xml`.

### Passo 4 · Ligue o MySQL

Abra o **XAMPP Control Panel** e clique em **Start** na linha **MySQL**. Ela fica verde quando o banco está no ar.

> Com o MySQL Community Server não precisa: ele liga sozinho com o Windows (serviço **MySQL80**).

### Passo 5 · Crie o banco `clinicavet`

Os scripts do banco ficam na pasta [`docs/`](docs/). Rode os dois, nesta ordem:

| Arquivo | O que faz |
| --- | --- |
| [`docs/codigo.mysql`](docs/codigo.mysql) | Cria o banco `clinicavet` e as tabelas |
| [`docs/insert.mysql`](docs/insert.mysql) | Insere os 10 horários, os itens da farmácia e consultas de exemplo |

**Pelo terminal**, na pasta do projeto (o `mysql` do XAMPP fica em `C:\xampp\mysql\bin`):

```powershell
C:\xampp\mysql\bin\mysql -u root -e "source docs/codigo.mysql"
C:\xampp\mysql\bin\mysql -u root -e "source docs/insert.mysql"
```

**Ou pelo MySQL Workbench:** conecte no servidor local, vá em *File → Open SQL Script…*, abra o `docs/codigo.mysql` e clique no raio (*Execute*). Depois faça o mesmo com o `docs/insert.mysql`.

> Se você pular este passo, o servidor cria o banco, as tabelas e os dados de exemplo sozinho na primeira vez que iniciar, usando esses mesmos arquivos.

### Passo 6 · Crie o arquivo com a senha do banco

Na pasta do projeto, copie o arquivo de modelo:

```powershell
copy banco.example.properties banco.properties
```

**Com o XAMPP, não precisa mudar nada**: o modelo já vem com o `root` sem senha. Com o MySQL Community Server, abra o `banco.properties` e coloque a senha do passo 1 em `clinicavet.bd.senha=`.

```properties
clinicavet.bd.url=jdbc:mysql://localhost:3306/clinicavet
clinicavet.bd.usuario=root
clinicavet.bd.senha=
```

O `banco.properties` fica fora do git, então sua senha não vai para o GitHub.

### Passo 7 · Rode o projeto

1. Confira se o MySQL está ligado no XAMPP (passo 4).
2. Abra o **terminal na pasta do projeto** (passo 3).
3. Rode:

> [!IMPORTANT]
> ```powershell
> .\mvnw.cmd spring-boot:run
> ```

Na primeira vez demora um pouco, porque o Maven baixa as dependências. O servidor está pronto quando aparecer:

```
Banco de dados pronto em jdbc:mysql://localhost:3306/clinicavet
```

> No Linux ou macOS, o comando é `./mvnw spring-boot:run`.

### Passo 8 · Abra o site

Acesse **http://localhost:8082**. Tudo o que você fizer fica gravado no MySQL. Dá para ver as tabelas pelo MySQL Workbench ou pelo phpMyAdmin do XAMPP (`http://localhost/phpmyadmin`), no banco `clinicavet`.

O botão **Restaurar dados de exemplo**, no rodapé do site, apaga tudo e roda o `docs/insert.mysql` de novo.

### Para desligar e usar de novo

- **Desligar:** no terminal do servidor, aperte **Ctrl + C**. Depois, se quiser, clique em **Stop** no MySQL do XAMPP. Os dados continuam salvos no banco.
- **Usar de novo:** ligue o MySQL no XAMPP, abra o terminal na pasta do projeto e rode outra vez `.\mvnw.cmd spring-boot:run`.

> A JavaHotel usa a porta 8081 e a ClinicaVet a 8082, então os dois projetos podem rodar ao mesmo tempo.

### Se algo der errado

| Mensagem | O que fazer |
| --- | --- |
| `O termo '.\mvnw.cmd' não é reconhecido` | O terminal não está na pasta do projeto. Abra o terminal na pasta `ClinicaVetJava`, a que tem o `mvnw.cmd` (passo 3). |
| `Error: JAVA_HOME not found in your environment` | O `JAVA_HOME` não existe. Faça o passo 2 e abra um terminal novo. |
| `Error: JAVA_HOME is set to an invalid directory` | O `JAVA_HOME` aponta para uma pasta que não existe. Refaça o passo 2 com o caminho certo e abra um terminal novo. |
| `release version 25 not supported` ou erro de versão do Java | O `JAVA_HOME` aponta para um Java antigo. Refaça o passo 2 com o JDK 25. |
| `Operador '<' reservado para uso futuro` | O terminal é o PowerShell, que não aceita o `<`. Rode os scripts com `-e "source ..."`, como no passo 5. |
| *Não foi possível conectar ao MySQL* | O MySQL está desligado. Faça o passo 4. |
| *O banco de dados não existe* ou *As tabelas do banco de dados não existem* | Rode o `docs/codigo.mysql` (passo 5). |
| *Usuário ou senha do MySQL incorretos* | Confira a senha no `banco.properties` (passo 6). |
| `Duplicate entry` ao rodar o `insert.mysql` | Os dados de exemplo já foram inseridos. Para recomeçar, use o botão **Restaurar dados de exemplo** no site. |
| No XAMPP, o MySQL não liga (*Port 3306 in use*) | Já existe outro MySQL usando a porta 3306. Use esse, ou pare o serviço dele em *Serviços* do Windows. |
| *Port 8082 was already in use* | O servidor já está rodando em outro terminal. Use esse ou feche ele. |
| Na página: *Não foi possível carregar os dados do banco de dados* | O servidor não está rodando, ou o banco está com problema. Veja a mensagem no terminal do servidor. |

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
