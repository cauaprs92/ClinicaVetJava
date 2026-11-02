<div align="center">

# 🐾 ClinicaVet

### Sistema de agenda de clínica veterinária com farmácia

<br/>

[![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white)](https://developer.mozilla.org/docs/Web/HTML)
[![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white)](https://developer.mozilla.org/docs/Web/CSS)
[![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black)](https://developer.mozilla.org/docs/Web/JavaScript)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org)
[![POO](https://img.shields.io/badge/POO-4B8BBE?style=flat-square)](#-conceitos-de-poo-no-app-de-terminal)
[![Deploy](https://img.shields.io/badge/Deploy-Vercel-000000?style=flat-square&logo=vercel)](https://vercel.com)

</div>

---

## 📋 Sobre o projeto

O **ClinicaVet** começou como um sistema de terminal em **Java** para praticar **Programação Orientada a Objetos**: uma agenda de 10 horários onde dá para agendar, consultar, editar e cancelar consultas de cães e gatos.

Agora ele também tem uma **interface web** (pasta `public/`), com as mesmas regras da versão de terminal e mais algumas: status da consulta, farmácia com estoque e pagamento no fim do atendimento.

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

> Os dados ficam salvos apenas no navegador de quem usa (`localStorage`). Não há banco de dados nem compartilhamento entre usuários.

---

## 🧱 Estrutura do projeto

```
ClinicaVet/
├── public/                   # Site (estático, pronto para a Vercel)
│   ├── index.html            # Home: ocupação da agenda, consultas por status, indicadores e agenda de hoje
│   ├── agenda.html           # Os 10 horários do dia, por turno
│   ├── consultas.html        # Agendar, atender, finalizar e cancelar
│   ├── farmacia.html         # Lançar itens na consulta e ver o estoque
│   ├── app.js                # Regras da agenda, das consultas e da farmácia (compartilhado)
│   └── css/style.css         # Tema visual (claro e escuro)
│
└── console-app/              # Versão original em Java puro para terminal (referência de estudo)
```

O site e o app de terminal seguem as mesmas regras: 10 horários por dia (08:00 às 12:00 e 14:00 às 18:00), um paciente por horário e animais do tipo cachorro (com porte) ou gato (com temperamento).

---

## 🚀 Como abrir

**Na sua máquina:** abra o arquivo `public/index.html` no navegador. Não precisa instalar nada.

**Na Vercel:** importe o repositório em [vercel.com/new](https://vercel.com/new). A pasta `public/` é servida como site estático, sem etapa de build.

---

## 🖥️ App de terminal

A primeira versão do projeto continua em `console-app/`:

```bash
javac console-app/*.java
java -cp console-app Main
```

```text
====================================
        CLINICA VETERINARIA
====================================

1 - Agendar consulta
2 - Consultar agenda
3 - Editar consulta
4 - Cancelar consulta
5 - Sair
```

### 🧠 Conceitos de POO no app de terminal

| Conceito | Onde aparece |
| --- | --- |
| 🔒 **Encapsulamento** | Atributos `private` com acesso por `get` e `set` (`Animal`, `Consulta`) |
| 🧬 **Herança** | `Cachorro extends Animal` e `Gato extends Animal` |
| 🎭 **Abstração** | `public abstract class Animal`, que não pode ser instanciada |
| 🔄 **Polimorfismo** | `Animal paciente` guarda um `Cachorro` ou um `Gato`, e `emitirSom()` responde "Au Au!" ou "Miau!" |
| ♻️ **Sobrescrita** | `@Override` em `emitirSom()`, `getTipo()` e `getCaracteristica()` |

| Classe | Responsabilidade |
| --- | --- |
| `Main` | Menu e interação com o usuário |
| `Animal` | Classe abstrata base dos pacientes |
| `Cachorro` | Paciente canino (porte) |
| `Gato` | Paciente felino (temperamento) |
| `Consulta` | Um agendamento: paciente, horário e sintomas |
| `AgendaManager` | A agenda do dia, em um vetor `Consulta[10]` |

---

## 👨‍💻 Desenvolvedor

**Matheus Ricardo**

GitHub: [@mateusricardodev](https://github.com/mateusricardodev)

---

<p align="center">
  🐾 <strong>ClinicaVet</strong> — Organizando o cuidado com quem não pode falar. 🐾
</p>
