# 🐾 System Clinical

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/POO-4B8BBE?style=for-the-badge" alt="POO">
  <img src="https://img.shields.io/badge/Status-Em%20desenvolvimento-yellow?style=for-the-badge" alt="Status">
</p>

<p align="center">
  <strong>Sistema de controle e gerenciamento de uma clínica veterinária desenvolvido em Java.</strong>
</p>

---

## 📋 Sobre o projeto

O **System Clinical** é um sistema desenvolvido em **Java** com foco no gerenciamento de uma clínica veterinária.

O projeto foi desenvolvido como atividade acadêmica para colocar em prática os principais conceitos de **Programação Orientada a Objetos (POO)**, simulando uma aplicação para organização de consultas e informações dos animais atendidos pela clínica.

A aplicação permite controlar os horários disponíveis, realizar agendamentos, consultar a agenda, editar consultas e cancelar atendimentos.

O projeto também utiliza conceitos como **encapsulamento, herança, abstração e polimorfismo**, proporcionando uma aplicação prática dos fundamentos da orientação a objetos.

---

## 🎯 Objetivos

* Desenvolver um sistema funcional para uma clínica veterinária;
* Praticar conceitos fundamentais de Java;
* Aplicar os princípios da Programação Orientada a Objetos;
* Trabalhar com classes e objetos;
* Implementar herança e polimorfismo;
* Desenvolver um sistema de gerenciamento de consultas;
* Praticar validações e estruturas de controle.

---

## ⚙️ Funcionalidades

### 📅 Gerenciamento de consultas

* [x] Agendamento de consultas;
* [x] Consulta da agenda;
* [x] Edição de consultas;
* [x] Cancelamento de consultas;
* [x] Controle de horários disponíveis;
* [x] Validação de horários ocupados;
* [x] Identificação do tipo de animal;
* [x] Registro de informações do tutor;
* [x] Registro dos sintomas/queixas do animal.

### 🐶🐱 Animais

O sistema trabalha com diferentes tipos de animais através de uma estrutura de herança.

Atualmente são contemplados:

* 🐶 **Cachorro**
* 🐱 **Gato**

Cada tipo de animal possui suas próprias características e comportamento.

---

## 🧠 Conceitos de POO utilizados

Um dos principais objetivos do projeto é aplicar os fundamentos da **Programação Orientada a Objetos**.

### 🔒 Encapsulamento

Os atributos das classes são protegidos utilizando modificadores de acesso, principalmente `private`, com acesso controlado através de métodos.

```java
private String nome;
private int idade;
```

### 🧬 Herança

A classe base `Animal` é utilizada como estrutura para os diferentes tipos de animais.

```java
public class Cachorro extends Animal
```

```java
public class Gato extends Animal
```

### 🎭 Abstração

A classe `Animal` representa uma abstração dos animais atendidos pela clínica.

```java
public abstract class Animal
```

### 🔄 Polimorfismo

O sistema permite trabalhar com diferentes tipos de animais através de uma referência da classe base:

```java
Animal paciente;
```

Assim, o mesmo sistema pode trabalhar com objetos `Cachorro` ou `Gato`.

### ♻️ Sobrescrita de métodos

Cada animal pode possuir seu próprio comportamento através de `@Override`.

Exemplo:

```java
@Override
public String emitirSom() {
    return "Au Au";
}
```

---

## 🏗️ Estrutura do projeto

```text
system-clinical/
│
├── src/
│   ├── Main.java
│   ├── Animal.java
│   ├── Cachorro.java
│   ├── Gato.java
│   ├── Consulta.java
│   └── AgendaManager.java
│
└── README.md
```

### 📁 Principais classes

| Classe          | Responsabilidade                        |
| --------------- | --------------------------------------- |
| `Main`          | Inicialização e interação com o sistema |
| `Animal`        | Classe abstrata base para os animais    |
| `Cachorro`      | Representação de pacientes caninos      |
| `Gato`          | Representação de pacientes felinos      |
| `Consulta`      | Representação dos agendamentos          |
| `AgendaManager` | Gerenciamento da agenda da clínica      |

---

## 🗓️ Funcionamento da agenda

A agenda possui **10 horários disponíveis** para os atendimentos.

Os horários são armazenados utilizando um vetor de objetos `Consulta`:

```java
Consulta[] agenda = new Consulta[10];
```

Cada posição representa um horário da agenda.

O sistema verifica se o horário está disponível antes de realizar um novo agendamento.

Exemplo:

```text
========== AGENDA ==========

1 - 08:00 - Disponível
2 - 09:00 - Rex
3 - 10:00 - Mingau
4 - 11:00 - Disponível
5 - 13:00 - Disponível
...
```

---

## 🖥️ Exemplo de utilização

Ao iniciar o sistema, o usuário encontra um menu de gerenciamento:

```text
====================================
       CLÍNICA VETERINÁRIA
====================================

1 - Agendar consulta
2 - Consultar agenda
3 - Editar consulta
4 - Cancelar consulta
5 - Sair

Escolha uma opção:
```

### Exemplo de agendamento

```text
Escolha o tipo de animal:

1 - Cachorro
2 - Gato

Opção: 1

Nome do animal: Rex
Idade: 5
Nome do dono: João
Telefone: 99999-9999
Porte: Grande
Sintomas: Febre

Consulta agendada com sucesso!
```

---

## 🛠️ Tecnologias

O projeto utiliza principalmente:

* ☕ **Java**
* 🧱 **Programação Orientada a Objetos**
* 📦 Classes e objetos
* 🔒 Encapsulamento
* 🧬 Herança
* 🎭 Abstração
* 🔄 Polimorfismo
* 📚 Vetores
* ⌨️ `Scanner`
* 🔀 Estruturas condicionais
* 🔁 Estruturas de repetição

---

## ▶️ Como executar

### 1. Clone o repositório

```bash
git clone https://github.com/mateusricardodev/system-clinical.git
```

### 2. Entre na pasta

```bash
cd system-clinical
```

### 3. Compile o projeto

Caso os arquivos estejam diretamente dentro de `src`:

```bash
javac src/*.java
```

### 4. Execute

```bash
java -cp src Main
```

> Dependendo da IDE utilizada, também é possível executar diretamente a classe `Main`.

---

## 💻 IDEs recomendadas

O projeto pode ser executado em qualquer ambiente com suporte a Java, como:

* IntelliJ IDEA
* Eclipse
* Visual Studio Code
* NetBeans

---

## 📚 Contexto acadêmico

Este projeto foi desenvolvido com finalidade **educacional**, tendo como principal objetivo praticar os conceitos apresentados durante as aulas de **Programação Orientada a Objetos em Java**.

A implementação busca manter uma estrutura simples e compatível com os conteúdos estudados, priorizando a compreensão dos fundamentos de POO em vez da utilização de frameworks ou arquiteturas avançadas.

---

## 🚀 Possíveis melhorias futuras

Algumas funcionalidades podem ser adicionadas futuramente:

* [ ] Persistência de dados;
* [ ] Banco de dados;
* [ ] Cadastro de veterinários;
* [ ] Cadastro de funcionários;
* [ ] Histórico de consultas;
* [ ] Prontuário dos animais;
* [ ] Sistema de login;
* [ ] Interface gráfica;
* [ ] Relatórios de atendimentos;
* [ ] Controle financeiro da clínica.

---

## 👨‍💻 Desenvolvedor

**Matheus Ricardo**

GitHub: [@mateusricardodev](https://github.com/mateusricardodev)

---

<p align="center">
  🐾 <strong>System Clinical</strong> — Organizando o cuidado com quem não pode falar. 🐾
</p>
