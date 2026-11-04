/* ==========================================================
   ClinicaVet — telas da agenda, das consultas e da farmácia
   Os dados ficam no banco MySQL. Este arquivo busca tudo pela
   API do servidor Java (/api/estado) e envia cada alteração para ela;
   quem valida as regras e grava no banco é o Java (ClinicaService).
   Cada página chama sua função pelo atributo data-page do <body>.

   Ciclo de uma consulta:
     agendada    → (iniciar atendimento)    → atendimento
     atendimento → (finalizar + pagamento)  → finalizada
     agendada    → (cancelar consulta)      → removida
   A agenda tem 10 horários por dia: 5 de manhã e 5 à tarde, os mesmos
   do app de terminal. Cada horário de cada dia recebe no máximo uma
   consulta, e só dá para agendar em horários que ainda não passaram.
   ========================================================== */

const ESTOQUE_REFERENCIA = 20;
const ESTOQUE_BAIXO = 5;
const API = "api"; // mesmo endereço do site: http://localhost:8082/api
const SEM_CONEXAO = "Sem conexão com o servidor da ClinicaVet. Verifique se ele está rodando.";

// As chaves são as gravadas nos dados; os rótulos são o que aparece na tela.
const STATUS = { agendada: "Agendada", atendimento: "Em atendimento", finalizada: "Finalizada" };
const TURNOS = { manha: "Manhã", tarde: "Tarde" };
const ESPECIES = { cachorro: "Cachorro", gato: "Gato" };

/** O que cada espécie tem de próprio (como Cachorro.porte e Gato.temperamento no Java). */
const CARACTERISTICAS = {
    cachorro: { campo: "porte", rotulo: "Porte", opcoes: ["Pequeno", "Médio", "Grande"] },
    gato: { campo: "temperamento", rotulo: "Temperamento", opcoes: ["Dócil", "Arredio", "Brincalhão"] },
};

/* ---------- Datas (texto "AAAA-MM-DD", no fuso do navegador) ---------- */

const doisDigitos = (n) => String(n).padStart(2, "0");
const isoDoDia = (d) => `${d.getFullYear()}-${doisDigitos(d.getMonth() + 1)}-${doisDigitos(d.getDate())}`;
const hoje = () => isoDoDia(new Date());
const dataValida = (iso) => typeof iso === "string" && /^\d{4}-\d{2}-\d{2}$/.test(iso) && !Number.isNaN(Date.parse(iso));

/** Hora atual, "HH:MM". */
function agora() {
    const d = new Date();
    return `${doisDigitos(d.getHours())}:${doisDigitos(d.getMinutes())}`;
}

function paraData(iso) {
    const [a, m, d] = iso.split("-").map(Number);
    return new Date(a, m - 1, d);
}

function somarDias(iso, dias) {
    const d = paraData(iso);
    d.setDate(d.getDate() + dias);
    return isoDoDia(d);
}

const diasEntre = (de, ate) => Math.round((Date.parse(ate) - Date.parse(de)) / 864e5);

/** "31/10", ou "31/10/27" quando o ano é outro. */
function dataCurta(iso) {
    const [a, m, d] = iso.split("-");
    return a === hoje().slice(0, 4) ? `${d}/${m}` : `${d}/${m}/${a.slice(2)}`;
}

const formatoSemana = new Intl.DateTimeFormat("pt-BR", { weekday: "short" });
const formatoExtenso = new Intl.DateTimeFormat("pt-BR", { weekday: "long", day: "numeric", month: "long" });
const formatoExtensoAno = new Intl.DateTimeFormat("pt-BR", { weekday: "long", day: "numeric", month: "long", year: "numeric" });
const maiuscula = (t) => t.charAt(0).toUpperCase() + t.slice(1);

/** "ter, 03/11" */
const diaCurto = (iso) => `${formatoSemana.format(paraData(iso)).replace(".", "")}, ${dataCurta(iso)}`;

/** "Terça-feira, 3 de novembro" (com o ano quando é outro). */
function diaPorExtenso(iso) {
    const formato = iso.slice(0, 4) === hoje().slice(0, 4) ? formatoExtenso : formatoExtensoAno;
    return maiuscula(formato.format(paraData(iso)));
}

/* ---------- Estado (vem do banco de dados) ---------- */

/**
 * Cópia do que está no banco: horários, consultas (com paciente, tutor, código e total
 * da farmácia), itens da farmácia e as vendas do último ano (para o gráfico da Home).
 * É substituída por inteiro a cada carregarEstado().
 */
let estado = { horarios: [], consultas: [], produtos: [], vendas: [] };

/** Busca no servidor tudo o que as páginas mostram. Lança um Error com a mensagem do problema. */
async function carregarEstado() {
    let resposta;
    try {
        resposta = await fetch(`${API}/estado`, { cache: "no-store" });
    } catch (e) {
        throw new Error(SEM_CONEXAO);
    }
    const dados = await resposta.json().catch(() => null);
    if (!resposta.ok || !dados || !Array.isArray(dados.consultas)) {
        throw new Error((dados && dados.msg) || `O servidor respondeu com erro (${resposta.status}).`);
    }
    estado = dados;
}

/* ---------- Consultas ---------- */

const ativa = (c) => c.status !== "finalizada";
/** Os horários do dia, como estão gravados no banco. */
const horarios = () => estado.horarios;
const buscarHorario = (n) => horarios().find((h) => h.numero === n) || null;
const horarioValido = (n) => buscarHorario(n) !== null;
/** "08:00" */
const hora = (n) => (buscarHorario(n) || { hora: "--:--" }).hora;
const turnoDe = (n) => (buscarHorario(n) || { turno: "manha" }).turno;
/** Turnos que existem, na ordem do dia. */
const turnos = () => [...new Set(horarios().map((h) => h.turno))];
const buscarConsulta = (id) => estado.consultas.find((c) => c.id === id) || null;
const comStatus = (status) => estado.consultas.filter((c) => c.status === status);
const porCodigo = (a, b) => parseInt(a.codigo, 10) - parseInt(b.codigo, 10);
const porData = (a, b) => a.data.localeCompare(b.data) || hora(a.horario).localeCompare(hora(b.horario));

/** Consulta marcada naquele horário do dia (cada horário recebe no máximo uma), ou null. */
function ocupante(data, numero, ignorarId = null) {
    return estado.consultas.find((c) => c.data === data && c.horario === numero && c.id !== ignorarId) || null;
}

/** Horário que já começou, ou dia que já passou: não dá mais para agendar nele. */
const horarioPassou = (data, numero) => data < hoje() || (data === hoje() && hora(numero) < agora());

/** Horários livres no dia que ainda não passaram. */
function horariosDisponiveis(data, ignorarId = null) {
    return horarios().map((h) => h.numero).filter((n) => !ocupante(data, n, ignorarId) && !horarioPassou(data, n));
}

/**
 * Situação de cada horário de um dia:
 *  - agendada, atendimento ou finalizada: o status da consulta marcada nele;
 *  - livre:     sem consulta, ainda dá para agendar;
 *  - encerrado: sem consulta, mas o horário já passou.
 */
function agendaDoDia(dia) {
    return horarios().map((h) => {
        const consulta = ocupante(dia, h.numero);
        const situacao = consulta ? consulta.status : horarioPassou(dia, h.numero) ? "encerrado" : "livre";
        return { ...h, consulta, situacao };
    });
}

const consultasDoDia = (dia) => estado.consultas.filter((c) => c.data === dia).sort(porData);

/** Primeiro dia, a partir de hoje, com algum horário livre. */
function primeiroDiaLivre() {
    let dia = hoje();
    for (let i = 0; i < 365; i++, dia = somarDias(dia, 1)) {
        if (horariosDisponiveis(dia).length) return dia;
    }
    return hoje();
}

/** Valida a data de uma consulta; devolve a mensagem de erro ou null. */
function erroDeData(data) {
    if (!dataValida(data)) return "Informe a data da consulta.";
    if (data < hoje()) return "A consulta não pode ser em uma data que já passou.";
    return null;
}

/** Valida a data e o horário de uma consulta; devolve a mensagem de erro ou null. */
function erroDeAgendamento(data, numero) {
    const problema = erroDeData(data);
    if (problema) return problema;
    if (!horarioValido(numero)) return "Escolha um horário válido.";
    if (horarioPassou(data, numero)) return `O horário das ${hora(numero)} de hoje já passou.`;
    return null;
}

const descreverOcupante = (c) =>
    `O horário das ${hora(c.horario)} de ${dataCurta(c.data)} já está com ${c.paciente.nome} (consulta #${c.codigo}).`;

// Minúsculas e sem acento, para "joao" achar "João".
const normalizar = (t) => String(t).normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase().trim();
const soDigitos = (t) => String(t).replace(/\D/g, "");

/** O mesmo animal do mesmo tutor (o e-mail identifica o tutor). */
const mesmoPaciente = (a, b) =>
    normalizar(a.tutor.email) === normalizar(b.tutor.email) && normalizar(a.nome) === normalizar(b.nome);

/** Outras consultas do mesmo paciente, da mais recente para a mais antiga. */
function historicoDoPaciente(c) {
    return estado.consultas
        .filter((x) => x.id !== c.id && mesmoPaciente(x.paciente, c.paciente))
        .sort(porData)
        .reverse();
}

/* ---------- Paciente ---------- */

const caracteristicaDe = (p) => CARACTERISTICAS[p.especie] || null;
const textoIdade = (n) => (n === 0 ? "menos de 1 ano" : n === 1 ? "1 ano" : `${n} anos`);
/** "Cachorro, 5 anos" */
const descricaoAnimal = (p) => `${ESPECIES[p.especie] || "Animal"}, ${textoIdade(p.idade)}`;

/* ---------- Telefone ---------- */

// DDD + número: 11 dígitos no celular "(11) 90000-0000" ou 10 no fixo "(11) 3000-0000".
const MAX_DIGITOS_TELEFONE = 11;
const ERRO_TELEFONE = "Informe um telefone válido, como (11) 90000-0000.";

/**
 * Formata enquanto a pessoa digita: guarda só os números (no máximo 11) e coloca
 * parênteses, espaço e hífen. O hífen só aparece quando há número depois dele,
 * para o Backspace funcionar normalmente.
 */
function formatarTelefone(texto) {
    const d = String(texto).replace(/\D/g, "").slice(0, MAX_DIGITOS_TELEFONE);
    if (!d) return "";
    if (d.length <= 2) return `(${d}`;
    const ddd = d.slice(0, 2);
    const resto = d.slice(2);
    if (resto.length <= 4) return `(${ddd}) ${resto}`;
    const corte = resto.length === 9 ? 5 : 4; // celular: 5 antes do hífen
    return `(${ddd}) ${resto.slice(0, corte)}-${resto.slice(corte)}`;
}

/** Celular "(11) 90000-0000" (começa com 9) ou fixo "(11) 3000-0000" (começa com 2 a 5), com DDD de 11 a 99. */
function telefoneValido(texto) {
    return /^\([1-9]\d\) (9\d{4}|[2-5]\d{3})-\d{4}$/.test(texto);
}

/* ---------- Alterações (enviadas para o servidor Java, que grava no banco) ---------- */

const erro = (msg) => ({ ok: false, msg });

/**
 * Envia uma alteração para a API. O Java confere as regras (horário livre, datas,
 * estoque...) e grava no MySQL; aqui só mostramos a mensagem que ele devolve.
 * Sempre que o servidor responde, os dados são recarregados do banco, para a tela
 * ficar igual ao que está gravado (inclusive mudanças feitas por outra pessoa).
 * Devolve { ok, msg }.
 */
async function enviar(metodo, caminho, corpo) {
    let resposta;
    try {
        resposta = await fetch(`${API}/${caminho}`, {
            method: metodo,
            headers: corpo === undefined ? {} : { "Content-Type": "application/json" },
            body: corpo === undefined ? undefined : JSON.stringify(corpo),
        });
    } catch (e) {
        return erro(SEM_CONEXAO);
    }
    const dados = await resposta.json().catch(() => null);
    const resultado = dados && typeof dados.msg === "string"
        ? { ok: resposta.ok && dados.ok === true, msg: dados.msg }
        : erro(`O servidor respondeu com erro (${resposta.status}).`);
    try { await carregarEstado(); } catch (e) { /* a tela atualiza na próxima vez */ }
    return resultado;
}

/**
 * Nova consulta com o status Agendada. O telefone é conferido aqui e de novo no servidor.
 * d = { horario, data, sintomas, tutor: { nome, email, telefone }, paciente: { nome, especie, idade, porte | temperamento } }
 */
async function agendar(d) {
    if (!telefoneValido(d.tutor.telefone)) return erro(ERRO_TELEFONE);
    return enviar("POST", "consultas", d);
}

/** O paciente chegou: o servidor só aceita no dia da consulta. */
const iniciarAtendimento = (id) => enviar("POST", `consultas/${id}/iniciar`);

const cancelarConsulta = (id) => enviar("DELETE", `consultas/${id}`);

/** Finaliza o atendimento. Com itens da farmácia, o servidor exige a confirmação do pagamento. */
const finalizarConsulta = (id, pagamentoConfirmado) =>
    enviar("POST", `consultas/${id}/finalizar`, { pagamentoConfirmado });

/**
 * Atualiza tutor, paciente, sintomas, data e horário. Com o paciente em atendimento,
 * a data e o horário não mudam.
 */
async function editarConsulta(id, d) {
    if (!telefoneValido(d.tutor.telefone)) return erro(ERRO_TELEFONE);
    return enviar("PUT", `consultas/${id}`, d);
}

/**
 * Lança vários itens da farmácia de uma vez na consulta em atendimento.
 * itens: [{ indice, quantidade }], com o índice do item em estado.produtos.
 * O servidor confere tudo antes; se algo falhar, nada é lançado.
 */
function registrarConsumo(consultaId, itens) {
    return enviar("POST", "consumos", {
        consulta: consultaId,
        itens: itens.map(({ indice, quantidade }) => ({ produtoId: estado.produtos[indice].id, quantidade })),
    });
}

/** Cadastra um item novo na farmácia. d = { nome, preco, quantidade } */
const cadastrarProduto = (d) => enviar("POST", "produtos", d);

/** Entrada de mercadoria: soma unidades ao estoque de um item que já existe. */
const reporEstoque = (produtoId, quantidade) => enviar("POST", `produtos/${produtoId}/entrada`, { quantidade });

/** Apaga consultas, animais e tutores do banco e restaura o estoque da farmácia. */
const redefinirDados = () => enviar("POST", "redefinir");

/* ---------- Utilidades ---------- */

// Ícones fixos (sem dados do usuário), por isso podem ir via innerHTML.
const ICONE_OLHO = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>';
const ICONE_LIXEIRA = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 6h18"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>';
const ICONE_CHECK = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="m8.5 12.5 2.5 2.5 4.5-5"/></svg>';

const formatoBRL = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const formatoDataHora = new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" });
const brl = (v) => formatoBRL.format(v);
const dataHora = (iso) => (iso ? formatoDataHora.format(new Date(iso)) : "—");

const $ = (id) => document.getElementById(id);
const parametro = (nome) => new URLSearchParams(location.search).get(nome);

function el(tag, texto, classe) {
    const e = document.createElement(tag);
    if (texto !== undefined && texto !== null) e.textContent = texto;
    if (classe) e.className = classe;
    return e;
}

function opcao(valor, texto) {
    const o = el("option", texto);
    o.value = String(valor);
    return o;
}

function seloStatus(status) {
    return el("span", STATUS[status], `selo-status ${status}`);
}

function preencherSelect(select, opcoes, vazio) {
    const atual = select.value;
    select.replaceChildren();
    if (!opcoes.length) {
        select.append(opcao("", vazio));
        select.disabled = true;
        return;
    }
    select.disabled = false;
    opcoes.forEach(([valor, texto]) => select.append(opcao(valor, texto)));
    if (opcoes.some(([valor]) => String(valor) === atual)) select.value = atual;
}

let timerToast;
function avisar(resultado) {
    let toast = $("toast");
    if (!toast) {
        toast = el("div", "", "toast");
        toast.id = "toast";
        toast.setAttribute("role", "status");
        toast.setAttribute("aria-live", "polite");
        document.body.append(toast);
    }
    toast.textContent = resultado.msg;
    toast.classList.toggle("erro", !resultado.ok);
    toast.classList.add("visivel");
    clearTimeout(timerToast);
    timerToast = setTimeout(() => toast.classList.remove("visivel"), 3200);
}

/** Desativa o botão enquanto espera o servidor, para o pedido não ser enviado duas vezes. */
async function aguardando(botao, acao) {
    botao.disabled = true;
    try {
        return await acao();
    } finally {
        botao.disabled = false;
    }
}

/** Liga um formulário a uma ação no servidor; limpa o formulário quando dá certo e re-renderiza. */
function ligar(form, acao, aoTerminar) {
    const botao = form.querySelector("button[type=submit]");
    let enviando = false;
    form.addEventListener("submit", async (ev) => {
        ev.preventDefault();
        if (enviando) return;
        enviando = true;
        const resultado = await aguardando(botao, acao);
        enviando = false;
        avisar(resultado);
        if (resultado.ok) form.reset();
        aoTerminar(resultado);
    });
}

/* ----- Formulário de consulta (o de nova consulta e o de edição têm os mesmos campos) ----- */

/** Troca as opções de porte (cachorro) ou temperamento (gato) conforme a espécie escolhida. */
function atualizarCaracteristica(form, valor) {
    const c = CARACTERISTICAS[form.elements.especie.value] || CARACTERISTICAS.cachorro;
    const select = form.elements.caracteristica;
    const atual = valor !== undefined ? valor : select.value;
    select.replaceChildren(...c.opcoes.map((o) => opcao(o, o)));
    if (c.opcoes.includes(atual)) select.value = atual;
    form.querySelector("[data-rotulo-caracteristica]").textContent = c.rotulo;
}

/** Preenche tutor e paciente com os dados de uma consulta. */
function preencherPaciente(form, c) {
    const p = c.paciente;
    const campos = form.elements;
    campos.tutor.value = p.tutor.nome;
    campos.email.value = p.tutor.email;
    campos.telefone.value = formatarTelefone(p.tutor.telefone);
    campos.animal.value = p.nome;
    campos.idade.value = p.idade;
    campos.especie.value = p.especie;
    const car = caracteristicaDe(p);
    atualizarCaracteristica(form, car ? p[car.campo] : undefined);
}

/** Lê o formulário no formato das regras: { horario, data, sintomas, tutor, paciente }. */
function lerFormulario(form) {
    const f = Object.fromEntries(new FormData(form));
    const texto = (v) => String(v === undefined ? "" : v).trim();
    const paciente = {
        nome: texto(f.animal),
        especie: f.especie,
        idade: texto(f.idade) === "" ? null : Number(f.idade),
    };
    const car = CARACTERISTICAS[f.especie];
    if (car) paciente[car.campo] = f.caracteristica;
    return {
        horario: parseInt(f.horario, 10),
        data: f.data,
        sintomas: texto(f.sintomas),
        tutor: { nome: texto(f.tutor), email: texto(f.email), telefone: texto(f.telefone) },
        paciente,
    };
}

/* ---------- Página: Home ---------- */

// Período escolhido em cada gráfico da Home (cada um tem o seu filtro).
const periodosGraficos = { ocupacao: "dia", vendas: "dia", indicadores: "dia" };

function paginaInicio() {
    document.querySelectorAll(".filtro-periodo").forEach((filtro) => {
        const grafico = filtro.dataset.grafico;
        periodosGraficos[grafico] = lerPeriodoSalvo(grafico);
        filtro.querySelectorAll("button").forEach((b) => {
            b.addEventListener("click", () => {
                periodosGraficos[grafico] = b.dataset.periodo;
                salvarPeriodo(grafico, b.dataset.periodo);
                renderInicio();
            });
        });
    });

    renderInicio();
    return renderInicio;
}

function renderInicio() {
    const dia = hoje();
    const doDia = consultasDoDia(dia);
    const agendadas = comStatus("agendada");

    $("data-hoje").textContent = diaPorExtenso(dia);

    // Cada gráfico com o seu período: marca o botão escolhido e mostra as datas.
    const periodos = {};
    document.querySelectorAll(".filtro-periodo").forEach((filtro) => {
        const grafico = filtro.dataset.grafico;
        const p = montarPeriodo(periodosGraficos[grafico]);
        periodos[grafico] = p;
        filtro.querySelectorAll("button").forEach((b) => {
            b.setAttribute("aria-pressed", String(b.dataset.periodo === p.chave));
        });
        $(`intervalo-${grafico}`).textContent = intervaloTexto(p.inicio, p.fim);
    });
    desenharOcupacao(periodos.ocupacao);
    desenharVendas(periodos.vendas);
    desenharIndicadores(periodos.indicadores);

    const lista = $("lista-hoje");
    lista.replaceChildren();
    if (!doDia.length) {
        const vazio = el("div", undefined, "vazio");
        const link = el("a", agendadas.length ? "Ver consultas agendadas" : "Agendar a primeira consulta", "botao");
        link.href = "consultas.html";
        vazio.append(el("p", "Nenhuma consulta marcada para hoje."), link);
        lista.append(vazio);
        return;
    }
    const ul = el("ul", undefined, "lista");
    for (const c of doDia) {
        const li = el("li");
        const quem = el("div");
        quem.append(
            el("span", c.paciente.nome, "nome"),
            el("span", `Consulta #${c.codigo} · ${descricaoAnimal(c.paciente)} · ${c.paciente.tutor.nome}`, "sub"),
        );
        li.append(el("span", hora(c.horario), "num"), quem, seloStatus(c.status));
        ul.append(li);
    }
    lista.append(ul);
}

/* ----- Período dos gráficos (Dia, Mês, 3 meses, 6 meses, 1 ano) ----- */

/** Cada período termina hoje. "texto" completa frases como "horários ocupados ___". */
const PERIODOS = {
    dia: { texto: "hoje" },
    mes: { texto: "nos últimos 30 dias" },
    "3m": { texto: "nos últimos 3 meses" },
    "6m": { texto: "nos últimos 6 meses" },
    ano: { texto: "nos últimos 12 meses" },
};
const CHAVE_PERIODO = "clinicavet.periodo."; // + "ocupacao", "vendas" ou "indicadores"
const MAX_REMEDIOS = 5; // linhas do gráfico de barras

function lerPeriodoSalvo(grafico) {
    try {
        const salvo = localStorage.getItem(CHAVE_PERIODO + grafico);
        return PERIODOS[salvo] ? salvo : "dia";
    } catch (e) {
        return "dia";
    }
}

function salvarPeriodo(grafico, chave) {
    try { localStorage.setItem(CHAVE_PERIODO + grafico, chave); } catch (e) { /* sem armazenamento: só não lembra */ }
}

/**
 * Datas do período, terminando hoje.
 *   Dia → hoje · Mês → 30 dias · 3/6 meses → 13/26 semanas · 1 ano → 12 meses do calendário
 */
function montarPeriodo(chave) {
    const fim = hoje();
    let inicio;
    if (chave === "ano") {
        const d = paraData(fim);
        inicio = isoDoDia(new Date(d.getFullYear(), d.getMonth() - 11, 1));
    } else {
        const dias = { dia: 1, mes: 30, "3m": 13 * 7, "6m": 26 * 7 }[chave];
        inicio = somarDias(fim, -(dias - 1));
    }
    return { chave, inicio, fim, dias: diasEntre(inicio, fim) + 1, texto: PERIODOS[chave].texto };
}

/** "Ter, 03/11" para um dia só; "05/10 a 03/11" para um intervalo. */
const intervaloTexto = (de, ate) => (de === ate ? maiuscula(diaCurto(de)) : `${dataCurta(de)} a ${dataCurta(ate)}`);

/** Unidades vendidas de cada remédio entre duas datas (pelo dia do lançamento, no fuso do navegador). */
function unidadesPorRemedio(de, ate) {
    const total = new Map();
    for (const v of estado.vendas || []) {
        const dia = isoDoDia(new Date(v.lancadoEm));
        if (dia >= de && dia <= ate) total.set(v.produtoId, (total.get(v.produtoId) || 0) + v.quantidade);
    }
    return total;
}

/* ----- Gráfico 1: ocupação da agenda no período ----- */

function desenharOcupacao(p) {
    const plural = (n, um, varios) => `${n} ${n === 1 ? um : varios}`;
    const total = horarios().length * p.dias;
    const doPeriodo = estado.consultas.filter((c) => c.data >= p.inicio && c.data <= p.fim);
    const ocupados = doPeriodo.length; // cada horário de cada dia recebe no máximo uma consulta
    const com = (status) => doPeriodo.filter((c) => c.status === status).length;
    const pct = total ? Math.round((ocupados / total) * 100) : 0;

    $("anel").style.setProperty("--p", pct);
    $("anel-pct").textContent = pct + "%";
    $("anel").setAttribute("aria-label", `Ocupação da agenda ${p.texto}: ${pct}%`);

    const partes = [];
    if (com("atendimento") || p.chave === "dia") partes.push(`${com("atendimento")} em atendimento`);
    partes.push(plural(com("agendada"), "agendada", "agendadas"));
    if (com("finalizada")) partes.push(plural(com("finalizada"), "finalizada", "finalizadas"));
    $("ocupacao-texto").replaceChildren(
        el("strong", `${ocupados} de ${total}`), ` horários ocupados ${p.texto}`,
        el("br"),
        partes.join(" · "),
    );
}

/* ----- Resumo: os 4 cards, com o período do filtro deles ----- */

/** O dia (no fuso do navegador) de um instante gravado no banco, ou null. */
const diaDe = (instante) => (instante ? isoDoDia(new Date(instante)) : null);
const dentro = (dia, p) => dia !== null && dia >= p.inicio && dia <= p.fim;

function desenharIndicadores(p) {
    const plural = (n, um, varios) => `${n} ${n === 1 ? um : varios}`;
    const doPeriodo = estado.consultas.filter((c) => dentro(c.data, p));
    const horariosNoPeriodo = horarios().length * p.dias;

    // Horários sem consulta (cada horário de cada dia recebe no máximo uma).
    $("ind-livres").textContent = String(horariosNoPeriodo - doPeriodo.length);
    $("ind-livres-sub").textContent = `de ${horariosNoPeriodo} ${p.texto}`;

    // Pacientes atendidos: consultas finalizadas no período.
    $("ind-concluidos").textContent = String(doPeriodo.filter((c) => c.status === "finalizada").length);
    $("ind-concluidos-sub").textContent = `finalizadas ${p.texto}`;

    // Consultas marcadas no período (pela data em que foram agendadas).
    $("ind-agendadas").textContent = String(estado.consultas.filter((c) => dentro(diaDe(c.criadaEm), p)).length);
    $("ind-agendadas-sub").textContent = `marcadas ${p.texto}`;

    // Total vendido na farmácia: quantidade × preço de cada item lançado no período.
    if (!Array.isArray(estado.vendas)) {
        $("ind-vendido").textContent = "—";
        $("ind-vendido-sub").textContent = "reinicie o servidor para ver";
        return;
    }
    const vendas = estado.vendas.filter((v) => dentro(diaDe(v.lancadoEm), p));
    const valor = vendas.reduce((s, v) => s + v.quantidade * Number(v.precoUnitario || 0), 0);
    const itens = vendas.reduce((s, v) => s + v.quantidade, 0);
    $("ind-vendido").textContent = brl(valor);
    $("ind-vendido-sub").textContent = `${plural(itens, "item", "itens")} ${p.texto}`;
}

/* ----- Gráfico 2: remédios mais vendidos (uma barra por remédio, no período do filtro) ----- */

/** Passo "redondo" do eixo (1, 2, 5, 10, 20, 50...) para no máximo 4 faixas. */
function passoDoEixo(maximo) {
    const alvo = maximo / 4;
    const potencia = 10 ** Math.floor(Math.log10(Math.max(alvo, 1)));
    for (const m of [1, 2, 5, 10]) {
        if (m * potencia >= alvo) return Math.max(1, m * potencia);
    }
    return 10 * potencia;
}

const unidadesVendidas = (n) => `${n} ${n === 1 ? "unidade vendida" : "unidades vendidas"}`;
const formatoParte = new Intl.NumberFormat("pt-BR", { style: "percent", maximumFractionDigits: 0 });

function desenharVendas(p) {
    const grade = $("barras");
    const dica = $("barras-dica");
    const vazio = $("barras-vazio");
    const semVendasDoServidor = !Array.isArray(estado.vendas);

    const vendidas = unidadesPorRemedio(p.inicio, p.fim);
    const total = [...vendidas.values()].reduce((s, n) => s + n, 0);

    // Do mais vendido para o menos vendido (empate: ordem alfabética).
    const remedios = [...vendidas]
        .map(([id, unidades]) => {
            const produto = estado.produtos.find((x) => x.id === id);
            return { nome: produto ? produto.nomeProduto : `Item #${id}`, unidades };
        })
        .sort((a, b) => b.unidades - a.unidades || a.nome.localeCompare(b.nome, "pt-BR"));
    const linhas = remedios.slice(0, MAX_REMEDIOS);

    $("vendas-total").textContent = total ? `${unidadesVendidas(total)} ${p.texto}` : "";
    grade.replaceChildren();
    dica.hidden = true;
    vazio.hidden = linhas.length > 0;
    vazio.textContent = semVendasDoServidor
        ? "O servidor não enviou as vendas: ele está numa versão antiga. Reinicie o servidor para ver o gráfico."
        : "Nenhum remédio vendido neste período.";
    vazio.classList.toggle("aviso-grafico", semVendasDoServidor);
    grade.hidden = linhas.length === 0;

    // Tabela para leitores de tela.
    const tabela = $("barras-tabela");
    tabela.replaceChildren(el("caption", `Remédios mais vendidos ${p.texto}`));
    const cabecalho = el("tr");
    cabecalho.append(el("th", "Remédio"), el("th", "Unidades vendidas"));
    tabela.append(cabecalho);
    for (const r of remedios) {
        const tr = el("tr");
        tr.append(el("td", r.nome), el("td", String(r.unidades)));
        tabela.append(tr);
    }

    if (!linhas.length) return;

    // Escala: o maior valor, arredondado para um número "redondo".
    const passo = passoDoEixo(linhas[0].unidades);
    const topo = Math.ceil(linhas[0].unidades / passo) * passo;
    const pct = (v) => `${(v / topo) * 100}%`;

    // Linhas verticais da grade, atrás das barras.
    const fundo = el("div", undefined, "barras-grade");
    fundo.style.gridRow = `1 / span ${linhas.length}`;
    for (let v = 0; v <= topo; v += passo) {
        const linha = el("span");
        linha.style.left = pct(v);
        fundo.append(linha);
    }
    grade.append(fundo);

    linhas.forEach((r, i) => {
        const nome = el("div", r.nome, "barras-nome");
        nome.style.gridRow = String(i + 1);
        const grupo = el("div", undefined, "barras-grupo");
        grupo.style.gridRow = String(i + 1);
        const faixa = el("div", undefined, "barras-faixa");
        const barra = el("span", undefined, "barra-h");
        barra.style.width = pct(r.unidades);
        faixa.append(barra, el("span", String(r.unidades), "barras-valor"));
        grupo.append(faixa);

        // Passar o mouse (ou tocar) na linha mostra as unidades e a parte do total.
        const mostrar = () => {
            dica.replaceChildren(
                el("strong", r.nome),
                el("p", unidadesVendidas(r.unidades), "valor"),
                el("p", `${formatoParte.format(r.unidades / total)} do total · ${intervaloTexto(p.inicio, p.fim)}`, "sub"),
            );
            dica.hidden = false;
            const caixa = grade.getBoundingClientRect();
            const alvo = grupo.getBoundingClientRect();
            dica.style.top = `${alvo.bottom - caixa.top + 6}px`;
        };
        for (const alvo of [nome, grupo]) {
            alvo.addEventListener("pointerenter", mostrar);
            alvo.addEventListener("pointerleave", () => { dica.hidden = true; });
        }
        grade.append(nome, grupo);
    });

    // Números do eixo, embaixo das barras.
    const eixo = el("div", undefined, "barras-eixo");
    eixo.style.gridRow = String(linhas.length + 1);
    for (let v = 0; v <= topo; v += passo) {
        const t = el("span", String(v));
        t.style.left = pct(v);
        eixo.append(t);
    }
    grade.append(eixo);
}

/* ---------- Página: Agenda ---------- */

function paginaAgenda() {
    let filtro = "todos";
    let dia = dataValida(parametro("data")) ? parametro("data") : hoje();
    const mapa = $("mapa-agenda");
    const dialogo = $("dialogo-horario");
    const campoDia = $("dia-agenda");
    const botoes = document.querySelectorAll("[data-filtro]");
    const situacaoDoFiltro = { livres: "livre", agendadas: "agendada", atendimento: "atendimento", finalizadas: "finalizada" };

    function irPara(novo) {
        if (!dataValida(novo)) return;
        dia = novo;
        // Guarda o dia no endereço: recarregar a página continua no mesmo dia.
        history.replaceState(null, "", novo === hoje() ? location.pathname : `?data=${novo}`);
        render();
    }

    $("dia-anterior").addEventListener("click", () => irPara(somarDias(dia, -1)));
    $("dia-seguinte").addEventListener("click", () => irPara(somarDias(dia, 1)));
    $("dia-hoje").addEventListener("click", () => irPara(hoje()));
    campoDia.addEventListener("change", () => irPara(campoDia.value));

    botoes.forEach((b) => b.addEventListener("click", () => {
        filtro = b.dataset.filtro;
        render();
    }));

    $("fechar-dialogo").addEventListener("click", () => dialogo.close());
    dialogo.addEventListener("click", (ev) => { if (ev.target === dialogo) dialogo.close(); });

    function abrir(h) {
        const c = h.consulta;
        $("dialogo-titulo").textContent = h.hora;
        $("dialogo-turno").textContent = `${TURNOS[h.turno]} · ${diaCurto(dia)}`;
        const dl = $("dialogo-detalhes");
        const botoesDialogo = $("dialogo-botoes");
        dl.replaceChildren();
        botoesDialogo.replaceChildren();

        const linha = (rotulo, valor) => {
            const d = el("div");
            const dd = el("dd");
            dd.append(valor);
            d.append(el("dt", rotulo), dd);
            dl.append(d);
        };
        const link = (texto, href, classe) => {
            const a = el("a", texto, classe);
            a.href = href;
            return a;
        };

        if (c) {
            const p = c.paciente;
            const car = caracteristicaDe(p);
            linha("Código da consulta", `#${c.codigo}`);
            linha("Situação", seloStatus(c.status));
            linha("Paciente", `${p.nome} · ${descricaoAnimal(p)}`);
            if (car) linha(car.rotulo, p[car.campo]);
            linha("Tutor", p.tutor.nome);
            linha("Telefone", p.tutor.telefone);
            linha("Queixa", c.sintomas);
            if (c.status === "atendimento") {
                linha("Farmácia", brl(c.valorConsumido));
                botoesDialogo.append(link("Lançar itens da farmácia", `farmacia.html?consulta=${c.id}`, "botao"));
            } else if (c.status === "finalizada") {
                linha("Farmácia paga", brl(c.valorConsumido));
            }
            const podeIniciar = c.status === "agendada" && c.data === hoje();
            botoesDialogo.append(link(
                podeIniciar ? "Ver consulta e iniciar atendimento" : "Ver consulta",
                `consultas.html?consulta=${c.id}`,
                podeIniciar ? "botao" : "botao contorno",
            ));
            if (c.status !== "agendada") {
                botoesDialogo.append(link("Agendar retorno", `consultas.html?retorno=${c.id}`, "botao contorno"));
            }
        } else if (h.situacao === "livre") {
            linha("Situação", "Disponível");
            linha("Turno", TURNOS[h.turno]);
            botoesDialogo.append(link("Agendar este horário", `consultas.html?data=${dia}&horario=${h.numero}`, "botao"));
        } else {
            linha("Situação", "Horário encerrado");
            linha("Turno", TURNOS[h.turno]);
            botoesDialogo.append(link("Agendar em outro horário", "consultas.html", "botao contorno"));
        }

        // Histórico: outras consultas do mesmo paciente.
        const historico = c ? historicoDoPaciente(c).slice(0, 5) : [];
        const lista = $("dialogo-historico");
        lista.replaceChildren();
        $("dialogo-historico-bloco").hidden = historico.length === 0;
        if (c) $("dialogo-historico-titulo").textContent = `Outras consultas de ${c.paciente.nome}`;
        for (const x of historico) {
            const li = el("li");
            const a = link("", `consultas.html?consulta=${x.id}`);
            a.append(el("span", `#${x.codigo} · ${STATUS[x.status]}`, "nome"), el("span", `${diaCurto(x.data)} · ${hora(x.horario)}`, "sub"));
            li.append(a);
            lista.append(li);
        }
        dialogo.showModal();
    }

    function render() {
        campoDia.value = dia;
        $("dia-titulo").textContent = diaPorExtenso(dia);
        const distancia = diasEntre(hoje(), dia);
        $("dia-sub").textContent = distancia === 0 ? "Hoje"
            : distancia === 1 ? "Amanhã"
                : distancia === -1 ? "Ontem"
                    : distancia > 0 ? `Daqui a ${distancia} dias` : `Há ${-distancia} dias`;
        $("dia-hoje").disabled = distancia === 0;

        const agenda = agendaDoDia(dia);
        const contar = (situacao) => agenda.filter((h) => h.situacao === situacao).length;
        const n = {
            todos: agenda.length,
            livres: contar("livre"),
            agendadas: contar("agendada"),
            atendimento: contar("atendimento"),
            finalizadas: contar("finalizada"),
        };
        botoes.forEach((b) => {
            b.setAttribute("aria-pressed", String(b.dataset.filtro === filtro));
            b.querySelector(".contagem").textContent = n[b.dataset.filtro];
        });

        const visiveis = agenda.filter((h) => filtro === "todos" || h.situacao === situacaoDoFiltro[filtro]);

        mapa.replaceChildren();
        if (!visiveis.length) {
            mapa.append(el("p", "Nenhum horário neste filtro.", "aviso"));
            return;
        }

        for (const t of turnos()) { // manhã em cima
            const doTurno = visiveis.filter((h) => h.turno === t);
            if (!doTurno.length) continue;

            const noTurno = agenda.filter((h) => h.turno === t);
            const livresNoTurno = noTurno.filter((h) => h.situacao === "livre").length;
            const bloco = el("section", undefined, "turno");
            const rotulo = el("div", undefined, "turno-rotulo");
            rotulo.append(el("h2", TURNOS[t]), el("span", `${livresNoTurno} de ${noTurno.length} livres`));
            const grade = el("div", undefined, "grade-horarios");

            for (const h of doTurno) grade.append(cartaoHorario(h));
            bloco.append(rotulo, grade);
            mapa.append(bloco);
        }
    }

    function cartaoHorario(h) {
        const c = h.consulta;
        const card = el("button", undefined, `horario ${h.situacao}`);
        card.type = "button";
        card.setAttribute("aria-label", c
            ? `${h.hora}, ${STATUS[c.status].toLowerCase()}, ${c.paciente.nome}`
            : `${h.hora}, ${h.situacao === "livre" ? "livre" : "horário encerrado"}`);

        const topo = el("div", undefined, "horario-topo");
        topo.append(el("span", h.hora, "horario-hora"), el("i", undefined, `ponto ${h.situacao}`));

        const info = el("div", undefined, "horario-info");
        if (!c) {
            const livre = h.situacao === "livre";
            info.append(el("span", livre ? "Disponível" : "Sem consulta", "nome"),
                el("span", livre ? "Pronto para agendar" : "Horário encerrado", "sub"));
        } else {
            const sub = { agendada: "Aguardando atendimento", atendimento: "Em atendimento", finalizada: "Atendimento finalizado" };
            info.append(el("span", c.paciente.nome, "nome"), el("span", sub[c.status], "sub"));
        }

        card.append(topo, info);
        card.addEventListener("click", () => abrir(h));
        return card;
    }

    render();
    return render;
}

/* ---------- Página: Consultas ---------- */

function paginaConsultas() {
    const formAgendar = $("form-agendar");
    const selLivre = $("sel-horario-livre");
    const lista = $("lista-consultas");
    const dialogo = $("dialogo-consulta");
    const painelEditar = $("painel-editar");
    const painelConfirmar = $("painel-confirmar");
    const formEditar = $("form-editar");
    const selEditarHorario = $("editar-horario");
    const botaoPerigo = $("acao-perigo");
    const pagamentoOk = $("pagamento-ok");
    const botaoConfirmar = $("confirmar-acao");

    [formAgendar, formEditar].forEach((form) => {
        // Telefone: aceita só números e formata como (11) 90000-0000 enquanto digita.
        const telefone = form.elements.telefone;
        telefone.addEventListener("input", () => { telefone.value = formatarTelefone(telefone.value); });
        // Espécie: cachorro tem porte; gato tem temperamento.
        form.elements.especie.addEventListener("change", () => atualizarCaracteristica(form));
        atualizarCaracteristica(form);
    });

    let aberta = null;            // id da consulta aberta no diálogo
    let acaoPendente = null;      // "finalizar" | "cancelar"
    let confirmacaoVeioDaEdicao = false;

    const fecharDialogo = () => dialogo.close();
    $("fechar-consulta").addEventListener("click", fecharDialogo);
    dialogo.addEventListener("click", (ev) => { if (ev.target === dialogo) fecharDialogo(); });
    dialogo.addEventListener("close", () => { aberta = null; acaoPendente = null; });

    function mostrarPainel(qual) {
        painelEditar.hidden = qual !== "editar";
        painelConfirmar.hidden = qual !== "confirmar";
    }

    /* ----- Diálogo: ver / editar ----- */

    function abrirConsulta(c) {
        aberta = c.id;
        const finalizada = c.status === "finalizada";
        const p = c.paciente;
        const dia = hoje();

        $("consulta-sobretitulo").textContent = `Consulta #${c.codigo} · ${diaCurto(c.data)} às ${hora(c.horario)}`;
        $("consulta-titulo").textContent = finalizada ? "Detalhes da consulta" : "Editar consulta";
        $("consulta-selo").replaceChildren(seloStatus(c.status));

        // Faixa com a situação da consulta (e o início do atendimento, quando agendada).
        const faixa = $("consulta-faixa");
        faixa.className = `faixa-status ${c.status}`;
        $("consulta-faixa-texto").textContent = {
            agendada: c.data > dia
                ? `Atendimento no dia ${dataCurta(c.data)} às ${hora(c.horario)}. Consulta agendada em ${dataHora(c.criadaEm)}.`
                : c.data === dia
                    ? `Aguardando a chegada de ${p.nome}. Consulta hoje às ${hora(c.horario)}.`
                    : `A data desta consulta já passou (${dataCurta(c.data)}). Remarque para atender.`,
            atendimento: `${p.nome} em atendimento desde ${dataHora(c.iniciadaEm)}.`,
            finalizada: `Finalizada em ${dataHora(c.finalizadaEm)}. Farmácia paga: ${brl(c.valorConsumido)}.`,
        }[c.status];
        const botaoIniciar = $("iniciar-atendimento");
        botaoIniciar.hidden = c.status !== "agendada";
        botaoIniciar.disabled = c.data !== dia;

        const campos = formEditar.elements;
        preencherPaciente(formEditar, c);
        // Consultas finalizadas mostram o telefone como foi gravado.
        if (finalizada) campos.telefone.value = p.tutor.telefone;
        campos.sintomas.value = c.sintomas;
        campos.data.value = c.data;
        [campos.tutor, campos.email, campos.telefone, campos.animal, campos.idade,
            campos.especie, campos.caracteristica, campos.sintomas].forEach((x) => { x.disabled = finalizada; });
        // Depois que o atendimento começa, a data e o horário ficam fixos.
        campos.data.disabled = c.status !== "agendada";
        campos.data.min = c.data < dia ? c.data : dia;
        selEditarHorario.replaceChildren(); // começa do horário desta consulta
        atualizarHorariosEdicao(c);

        $("editar-farmacia-rotulo").textContent = finalizada ? "Farmácia paga" : "Itens da farmácia";
        $("editar-farmacia").textContent = brl(c.valorConsumido);

        $("acoes-edicao").hidden = finalizada;
        const cancelar = c.status === "agendada";
        botaoPerigo.className = cancelar ? "botao perigo" : "botao contorno";
        $("acao-perigo-icone").innerHTML = cancelar ? ICONE_LIXEIRA : ICONE_CHECK;
        $("acao-perigo-texto").textContent = cancelar ? "Cancelar consulta" : "Finalizar atendimento";

        mostrarPainel("editar");
        if (!dialogo.open) dialogo.showModal();
        if (!finalizada) campos.tutor.focus();
    }

    /** Horário atual + horários livres na data escolhida na edição. */
    function atualizarHorariosEdicao(c) {
        const rotulo = (n, atual) => `${hora(n)} · ${TURNOS[turnoDe(n)]}${atual ? " · atual" : ""}`;
        if (c.status !== "agendada") {
            selEditarHorario.replaceChildren(opcao(c.horario, rotulo(c.horario, false)));
            selEditarHorario.disabled = true;
            return;
        }
        const data = formEditar.elements.data.value || c.data;
        const atual = selEditarHorario.value || String(c.horario);
        const mesmaData = data === c.data;
        const numeros = dataValida(data) ? horariosDisponiveis(data, c.id) : [];
        // Na mesma data, o horário desta consulta continua na lista (mesmo que já tenha passado).
        if (mesmaData && !numeros.includes(c.horario)) numeros.push(c.horario);
        numeros.sort((a, b) => hora(a).localeCompare(hora(b)));

        preencherSelect(selEditarHorario,
            numeros.map((n) => [n, rotulo(n, mesmaData && n === c.horario)]),
            "Nenhum horário livre nessa data");
        if (numeros.some((n) => String(n) === atual)) selEditarHorario.value = atual;
    }

    formEditar.elements.data.addEventListener("change", () => {
        const c = buscarConsulta(aberta);
        if (c) atualizarHorariosEdicao(c);
    });

    formEditar.addEventListener("submit", async (ev) => {
        ev.preventDefault();
        const id = aberta;
        if (id === null) return;
        const atual = buscarConsulta(id);
        const d = lerFormulario(formEditar);
        // Em atendimento, data e horário ficam desativados e não vêm no formulário.
        if (atual && atual.status !== "agendada") {
            d.data = atual.data;
            d.horario = atual.horario;
        }
        const r = await aguardando(formEditar.querySelector("button[type=submit]"), () => editarConsulta(id, d));
        avisar(r);
        render();
        if (r.ok) fecharDialogo();
    });

    $("iniciar-atendimento").addEventListener("click", async () => {
        const id = aberta;
        const r = await aguardando($("iniciar-atendimento"), () => iniciarAtendimento(id));
        avisar(r);
        render();
        // Continua aberta, agora em atendimento (ou com os dados atuais, se deu erro).
        const atualizada = buscarConsulta(id);
        if (atualizada && dialogo.open && aberta === id) abrirConsulta(atualizada);
    });

    /* ----- Diálogo: confirmação (cancelar ou finalizar com pagamento) ----- */

    function pedirConfirmacao(c, veioDaEdicao) {
        aberta = c.id;
        confirmacaoVeioDaEdicao = veioDaEdicao;
        acaoPendente = c.status === "agendada" ? "cancelar" : "finalizar";
        const codigo = el("strong", `#${c.codigo}`);
        const nome = el("strong", c.paciente.nome);
        const icone = $("confirmar-icone");

        if (acaoPendente === "cancelar") {
            icone.className = "icone-perigo";
            icone.innerHTML = ICONE_LIXEIRA;
            $("confirmar-titulo").textContent = "Cancelar consulta?";
            $("confirmar-texto").replaceChildren(
                "A consulta ", codigo, " de ", nome, " em ", el("strong", `${dataCurta(c.data)} às ${hora(c.horario)}`),
                " será apagada e o horário ficará livre. Não dá para desfazer.",
            );
            $("confirmar-pagamento").hidden = true;
            botaoConfirmar.className = "botao perigo-cheio";
            botaoConfirmar.textContent = "Cancelar consulta";
            botaoConfirmar.disabled = false;
        } else {
            icone.className = "icone-perigo sucesso";
            icone.innerHTML = ICONE_CHECK;
            $("confirmar-titulo").textContent = "Finalizar atendimento?";
            $("confirmar-texto").replaceChildren(
                "A consulta ", codigo, " de ", nome,
                " será finalizada e ficará no histórico do paciente.",
            );
            const temConsumo = c.valorConsumido > 0;
            $("confirmar-pagamento").hidden = false;
            $("pagamento-valor").textContent = brl(c.valorConsumido);
            $("pagamento-rotulo").textContent = temConsumo ? "Farmácia a pagar" : "Farmácia";
            $("pagamento-checagem").hidden = !temConsumo;
            $("pagamento-sem-consumo").hidden = temConsumo;
            pagamentoOk.checked = false;
            botaoConfirmar.className = "botao";
            botaoConfirmar.textContent = "Finalizar consulta";
            botaoConfirmar.disabled = temConsumo;
        }

        mostrarPainel("confirmar");
        if (!dialogo.open) dialogo.showModal();
        $("confirmar-voltar").focus();
    }

    pagamentoOk.addEventListener("change", () => { botaoConfirmar.disabled = !pagamentoOk.checked; });

    botaoPerigo.addEventListener("click", () => {
        const c = buscarConsulta(aberta);
        if (c && ativa(c)) pedirConfirmacao(c, true);
    });

    $("confirmar-voltar").addEventListener("click", () => {
        const c = buscarConsulta(aberta);
        if (!confirmacaoVeioDaEdicao || !c) return fecharDialogo();
        abrirConsulta(c);
        botaoPerigo.focus();
    });

    botaoConfirmar.addEventListener("click", async () => {
        const id = aberta;
        if (id === null) return;
        const cancelar = acaoPendente === "cancelar";
        const pago = pagamentoOk.checked;
        const r = await aguardando(botaoConfirmar, () => (cancelar ? cancelarConsulta(id) : finalizarConsulta(id, pago)));
        avisar(r);
        render();
        if (r.ok) fecharDialogo();
        // Se deu erro, o botão volta ao estado da caixa de pagamento.
        else if (!cancelar) botaoConfirmar.disabled = $("pagamento-checagem").hidden ? false : !pagamentoOk.checked;
    });

    /* ----- Lista de consultas ----- */

    function cartaoConsulta(c) {
        // O card todo abre a consulta com o mouse; pelo teclado, o botão do olho faz o mesmo.
        const p = c.paciente;
        const cartao = el("article", undefined, `consulta ${c.status}`);
        cartao.id = `consulta-${c.id}`;

        // O código da consulta é o destaque do card; data e horário vão para o rodapé.
        const num = el("span", undefined, "consulta-num");
        num.append(el("small", "Código"), `#${c.codigo}`);

        const quem = el("span", undefined, "consulta-quem");
        const titulo = el("span", undefined, "consulta-titulo");
        titulo.append(el("span", p.nome, "consulta-nome"), seloStatus(c.status));
        quem.append(
            titulo,
            el("span", descricaoAnimal(p), "consulta-contato"),
            el("span", p.tutor.nome, "consulta-contato"),
            el("span", p.tutor.telefone, "consulta-contato"),
        );

        const acoes = el("span", undefined, "consulta-acoes");
        const botaoIcone = (icone, classe, rotulo, dica, acao) => {
            const b = el("button", undefined, classe);
            b.type = "button";
            b.title = dica;
            b.setAttribute("aria-label", rotulo);
            b.innerHTML = icone;
            b.addEventListener("click", (ev) => {
                ev.stopPropagation(); // não dispara o clique do card
                acao();
            });
            return b;
        };
        acoes.append(botaoIcone(ICONE_OLHO, "icone-botao", `Ver a consulta #${c.codigo} (${p.nome})`,
            "Ver consulta", () => abrirConsulta(c)));
        if (c.status === "agendada") {
            acoes.append(botaoIcone(ICONE_LIXEIRA, "icone-botao perigo", `Cancelar a consulta #${c.codigo} (${p.nome})`,
                "Cancelar consulta", () => pedirConfirmacao(c, false)));
        } else if (c.status === "atendimento") {
            acoes.append(botaoIcone(ICONE_CHECK, "icone-botao sucesso", `Finalizar o atendimento da consulta #${c.codigo} (${p.nome})`,
                "Finalizar atendimento", () => pedirConfirmacao(c, false)));
        }

        const rodape = el("span", undefined, "consulta-rodape");
        let direita;
        if (c.status === "agendada") {
            direita = el("span", "Atendimento pendente");
        } else {
            direita = el("span", c.status === "finalizada" ? "Farmácia paga " : "Farmácia ");
            direita.append(el("strong", brl(c.valorConsumido)));
        }
        const quando = el("span");
        quando.append(el("strong", hora(c.horario)), ` · ${diaCurto(c.data)}`);
        rodape.append(quando, direita);

        cartao.append(num, quem, acoes, rodape);
        cartao.addEventListener("click", () => abrirConsulta(c));
        return cartao;
    }

    /* ----- Nova consulta: a data define quais horários aparecem ----- */

    const novaData = formAgendar.elements.data;
    let horarioPreferido = null; // vindo da agenda (?data=...&horario=N)

    function dataPadrao() {
        novaData.value = primeiroDiaLivre();
    }

    function atualizarNovaConsulta() {
        novaData.min = hoje();
        const data = novaData.value;
        const problema = erroDeData(data);
        const disponiveis = problema ? [] : horariosDisponiveis(data);

        preencherSelect(selLivre,
            disponiveis.map((n) => [n, `${hora(n)} · ${TURNOS[turnoDe(n)]}`]),
            problema ? "Escolha uma data válida" : "Nenhum horário livre nessa data");
        if (horarioPreferido && disponiveis.includes(horarioPreferido)) selLivre.value = String(horarioPreferido);
        formAgendar.querySelector("button[type=submit]").disabled = selLivre.disabled;

        $("nova-info").textContent = problema
            || `${diaCurto(data)} · ${disponiveis.length} ${disponiveis.length === 1 ? "horário livre" : "horários livres"}`;
    }

    novaData.addEventListener("change", atualizarNovaConsulta);

    function render(resultado) {
        if (resultado && resultado.ok) {
            horarioPreferido = null;
            atualizarCaracteristica(formAgendar); // o reset volta a espécie para Cachorro
        }
        if (!dataValida(novaData.value)) dataPadrao(); // o reset do formulário limpa a data
        atualizarNovaConsulta();
        renderLista();
    }

    /* ----- Abas de status, busca e filtros ----- */

    let filtroStatus = "ativas";
    const abas = document.querySelectorAll("[data-status]");
    const busca = $("busca-consultas");
    const filtroEspecie = $("filtro-especie");
    const filtroFarmacia = $("filtro-farmacia");
    const filtroOrdem = $("filtro-ordem");
    const barraFiltros = document.querySelector(".filtros-consultas");
    const barraAbas = document.querySelector(".abas-status");

    abas.forEach((b) => b.addEventListener("click", () => {
        filtroStatus = b.dataset.status;
        renderLista();
    }));

    function combinaBusca(c, termo) {
        if (!termo) return true;
        // Começando com "#", procura só pelo código da consulta.
        if (termo.startsWith("#")) {
            const cod = soDigitos(termo);
            return cod !== "" && parseInt(cod, 10) === parseInt(c.codigo, 10);
        }
        const p = c.paciente;
        const texto = normalizar([`#${c.codigo}`, p.nome, p.tutor.nome, p.tutor.email, p.tutor.telefone,
            hora(c.horario), dataCurta(c.data)].join(" "));
        if (texto.includes(normalizar(termo))) return true;
        const digitos = soDigitos(termo);
        if (!digitos) return false;
        // Código da consulta ("7", "0007" ou "#0007") ou trecho do telefone.
        return parseInt(digitos, 10) === parseInt(c.codigo, 10) || soDigitos(p.tutor.telefone).includes(digitos);
    }

    function doStatus(status) {
        if (status === "ativas") return estado.consultas.filter(ativa);
        if (status === "todas") return estado.consultas.slice();
        return comStatus(status);
    }

    function renderLista() {
        const todas = estado.consultas;
        const plural = (n) => (n === 1 ? "1 consulta" : `${n} consultas`);

        abas.forEach((b) => {
            b.setAttribute("aria-pressed", String(b.dataset.status === filtroStatus));
            b.querySelector(".contagem").textContent = doStatus(b.dataset.status).length;
        });
        barraAbas.hidden = barraFiltros.hidden = todas.length === 0;

        if (!todas.length) {
            $("total-consultas").textContent = plural(0);
            const vazio = el("div", undefined, "vazio");
            vazio.append(el("p", "Nenhuma consulta ainda. As novas consultas aparecem aqui com o status Agendada."));
            vazio.firstChild.style.margin = "0";
            lista.replaceChildren(vazio);
            return;
        }

        const base = doStatus(filtroStatus);
        const termo = busca.value.trim();
        const especie = filtroEspecie.value;
        const farmacia = filtroFarmacia.value;
        const filtrando = Boolean(termo || especie || farmacia);

        const visiveis = base.filter((c) =>
            combinaBusca(c, termo)
            && (!especie || c.paciente.especie === especie)
            && (farmacia !== "com" || c.valorConsumido > 0)
            && (farmacia !== "sem" || c.valorConsumido === 0));

        if (filtroOrdem.value === "codigo") {
            visiveis.sort(porCodigo);
        } else if (filtroOrdem.value === "animal") {
            visiveis.sort((a, b) => a.paciente.nome.localeCompare(b.paciente.nome, "pt-BR") || porData(a, b));
        } else if (filtroOrdem.value === "valor") {
            visiveis.sort((a, b) => b.valorConsumido - a.valorConsumido || porData(a, b));
        } else {
            visiveis.sort(porData); // padrão: pela data e horário da consulta
        }

        $("total-consultas").textContent = visiveis.length === base.length
            ? plural(base.length)
            : `${visiveis.length} de ${plural(base.length)}`;

        if (!visiveis.length) {
            const vazio = el("div", undefined, "vazio");
            if (filtrando) {
                const limpar = el("button", "Limpar filtros", "botao contorno");
                limpar.type = "button";
                limpar.addEventListener("click", limparFiltros);
                vazio.append(el("p", "Nenhuma consulta encontrada com esses filtros."), limpar);
            } else {
                const texto = {
                    ativas: "Nenhuma consulta em andamento no momento.",
                    agendada: "Nenhuma consulta agendada.",
                    atendimento: "Nenhum paciente em atendimento agora.",
                    finalizada: "Nenhuma consulta finalizada ainda.",
                }[filtroStatus] || "Nenhuma consulta.";
                vazio.append(el("p", texto));
                vazio.firstChild.style.margin = "0";
            }
            lista.replaceChildren(vazio);
            return;
        }
        lista.replaceChildren(...visiveis.map(cartaoConsulta));
    }

    function limparFiltros() {
        busca.value = "";
        filtroEspecie.value = "";
        filtroFarmacia.value = "";
        filtroOrdem.value = "data";
        renderLista();
        busca.focus();
    }

    busca.addEventListener("input", renderLista);
    busca.addEventListener("keydown", (ev) => { if (ev.key === "Escape" && busca.value) { busca.value = ""; renderLista(); } });
    [filtroEspecie, filtroFarmacia, filtroOrdem].forEach((s) => s.addEventListener("change", renderLista));

    ligar(formAgendar, () => agendar(lerFormulario(formAgendar)), render);

    dataPadrao();
    render();

    // Vindo da agenda:
    //  ?consulta=ID abre a consulta;
    //  ?retorno=ID prepara uma nova consulta para o mesmo paciente;
    //  ?data=AAAA-MM-DD&horario=N prepara uma nova consulta nesse horário.
    const pedidoConsulta = buscarConsulta(parseInt(parametro("consulta"), 10));
    const pedidoRetorno = buscarConsulta(parseInt(parametro("retorno"), 10));
    const pedidoData = parametro("data");
    if (pedidoConsulta) {
        if (!ativa(pedidoConsulta)) document.querySelector('[data-status="todas"]').click();
        abrirConsulta(pedidoConsulta);
    } else if (pedidoRetorno) {
        preencherPaciente(formAgendar, pedidoRetorno);
        formAgendar.elements.sintomas.value = `Retorno da consulta #${pedidoRetorno.codigo}`;
        novaData.focus();
    } else if (!erroDeData(pedidoData)) {
        novaData.value = pedidoData;
        const n = parseInt(parametro("horario"), 10);
        if (horarioValido(n)) horarioPreferido = n;
        atualizarNovaConsulta();
        formAgendar.elements.tutor.focus();
    }
    return render;
}

/* ---------- Página: Farmácia ---------- */

function paginaFarmacia() {
    const form = $("form-consumo");
    const selConsulta = $("sel-consulta");
    const listaProdutos = $("lista-produtos-consumo");
    const botaoLancar = form.querySelector("button[type=submit]");
    const aviso = $("aviso-sem-atendimento");

    const selAdicionar = $("sel-adicionar");

    const formRepor = $("form-repor");
    const formNovoItem = $("form-novo-item");
    const selItemEstoque = $("sel-estoque-item");

    // Itens adicionados a este lançamento, na ordem em que foram escolhidos.
    let itens = [];
    let semAtendimento = false;

    function atualizarResumo() {
        let total = 0;
        let unidades = 0;
        for (const { indice, quantidade } of itens) {
            total += estado.produtos[indice].preco * quantidade;
            unidades += quantidade;
        }
        $("resumo-total").textContent = brl(total);
        $("resumo-itens").textContent = unidades === 0
            ? "Nenhum item adicionado"
            : `${unidades} ${unidades === 1 ? "item" : "itens"} a lançar`;
        botaoLancar.disabled = semAtendimento || unidades === 0;
    }

    /** Monta a caixa "Adicionar item" só com o que ainda não foi adicionado e tem estoque. */
    function atualizarSeletor() {
        const disponiveis = estado.produtos
            .map((p, i) => [p, i])
            .filter(([p, i]) => p.quantidade > 0 && !itens.some((it) => it.indice === i));

        selAdicionar.replaceChildren(opcao("", itens.length ? "+ Adicionar outro item" : "+ Adicionar item"));
        for (const [p, i] of disponiveis) {
            selAdicionar.append(opcao(i, `${p.nomeProduto} · ${brl(p.preco)}`));
        }
        selAdicionar.value = "";
        selAdicionar.disabled = semAtendimento;
        selAdicionar.parentElement.hidden = disponiveis.length === 0;
    }

    function cartaoProduto(item) {
        const p = estado.produtos[item.indice];
        const cartao = el("div", undefined, "produto-linha ativo");

        const info = el("div", undefined, "produto-info");
        info.append(el("span", p.nomeProduto, "nome"));
        const sub = el("span", undefined, "sub");
        info.append(sub);

        const contador = el("div", undefined, "contador");
        const menos = el("button", "−");
        const mais = el("button", "+");
        const campo = el("input");
        menos.type = mais.type = "button";
        menos.setAttribute("aria-label", `Tirar uma unidade de ${p.nomeProduto}`);
        mais.setAttribute("aria-label", `Adicionar uma unidade de ${p.nomeProduto}`);
        campo.type = "number";
        campo.min = "1";
        campo.max = String(p.quantidade);
        campo.inputMode = "numeric";
        campo.setAttribute("aria-label", `Quantidade de ${p.nomeProduto}`);

        const remover = el("button", "×", "remover");
        remover.type = "button";
        remover.setAttribute("aria-label", `Remover ${p.nomeProduto}`);
        remover.addEventListener("click", () => {
            itens = itens.filter((it) => it !== item);
            cartao.remove();
            atualizarSeletor();
            atualizarResumo();
            selAdicionar.focus();
        });

        function definir(n) {
            item.quantidade = Math.max(1, Math.min(p.quantidade, Number.isFinite(n) ? Math.floor(n) : 1));
            campo.value = String(item.quantidade);
            sub.textContent = `${brl(p.preco)} · ${p.quantidade} em estoque · subtotal ${brl(p.preco * item.quantidade)}`;
            menos.disabled = semAtendimento || item.quantidade <= 1;
            mais.disabled = semAtendimento || item.quantidade >= p.quantidade;
            campo.disabled = remover.disabled = semAtendimento;
            atualizarResumo();
        }

        menos.addEventListener("click", () => definir(item.quantidade - 1));
        mais.addEventListener("click", () => definir(item.quantidade + 1));
        campo.addEventListener("input", () => {
            if (campo.value !== "") definir(parseInt(campo.value, 10));
        });
        campo.addEventListener("blur", () => definir(parseInt(campo.value, 10)));

        contador.append(menos, campo, mais);
        cartao.append(info, contador, remover);
        definir(item.quantidade);
        return cartao;
    }

    selAdicionar.addEventListener("change", () => {
        if (selAdicionar.value === "") return;
        const item = { indice: parseInt(selAdicionar.value, 10), quantidade: 1 };
        itens.push(item);
        listaProdutos.append(cartaoProduto(item));
        atualizarSeletor();
        atualizarResumo();
    });

    /* ----- Estoque: repor um item da lista ou cadastrar um remédio novo ----- */

    function preencherItensEstoque() {
        preencherSelect(selItemEstoque,
            estado.produtos.map((p) => [p.id, `${p.nomeProduto} · ${p.quantidade} un.`]),
            "Nenhum item cadastrado");
        formRepor.querySelector("button[type=submit]").disabled = selItemEstoque.disabled;
    }

    /** Mostra o formulário de repor ou o de remédio novo (um de cada vez). */
    function mostrarNovoItem(mostrar) {
        formRepor.hidden = mostrar;
        formNovoItem.hidden = !mostrar;
        if (mostrar) formNovoItem.elements.nome.focus();
    }

    $("abrir-novo-item").addEventListener("click", () => mostrarNovoItem(true));
    $("cancelar-novo-item").addEventListener("click", () => {
        formNovoItem.reset();
        mostrarNovoItem(false);
    });

    // Depois de repor, continua no mesmo item (o reset do formulário voltaria ao primeiro).
    let itemReposto = "";
    formRepor.addEventListener("submit", () => { itemReposto = selItemEstoque.value; });
    ligar(formRepor,
        () => reporEstoque(parseInt(selItemEstoque.value, 10), parseInt(formRepor.elements.quantidade.value, 10)),
        () => {
            render(); // sem o resultado: os itens que estão sendo lançados na consulta continuam lá
            if ([...selItemEstoque.options].some((o) => o.value === itemReposto)) selItemEstoque.value = itemReposto;
        });

    ligar(formNovoItem, () => {
        const campos = formNovoItem.elements;
        const preco = campos.preco.value.trim();
        return cadastrarProduto({
            nome: campos.nome.value.trim(),
            preco: preco === "" ? null : Number(preco),
            quantidade: parseInt(campos.quantidade.value, 10),
        });
    }, (resultado) => {
        render();
        if (!resultado.ok) return;
        // Volta para o repor, já com o remédio novo escolhido.
        const novo = estado.produtos.reduce((a, p) => (!a || p.id > a.id ? p : a), null);
        if (novo) selItemEstoque.value = String(novo.id);
        mostrarNovoItem(false);
    });

    function render(resultado) {
        if (resultado && resultado.ok) itens = [];
        // Se o estoque mudou, tira o que esgotou.
        itens = itens.filter((it) => estado.produtos[it.indice] && estado.produtos[it.indice].quantidade > 0);

        // Só consultas com o atendimento iniciado recebem itens.
        preencherSelect(selConsulta,
            comStatus("atendimento").sort(porData).map((c) =>
                [c.id, `${hora(c.horario)} · ${c.paciente.nome} (${c.paciente.tutor.nome}) · #${c.codigo}`]),
            "Nenhum paciente em atendimento");
        semAtendimento = selConsulta.disabled;
        aviso.hidden = !semAtendimento;

        listaProdutos.replaceChildren(...itens.map(cartaoProduto));
        atualizarSeletor();
        atualizarResumo();
        preencherItensEstoque();

        const ul = $("lista-estoque");
        ul.replaceChildren();
        for (const p of estado.produtos) {
            const li = el("li");
            const linha = el("div", undefined, "estoque-linha");
            linha.append(el("span", p.nomeProduto, "estoque-nome"), el("span", brl(p.preco), "estoque-preco"));

            const baixo = p.quantidade <= ESTOQUE_BAIXO;
            const barra = el("div", undefined, "barra" + (baixo ? " baixo" : ""));
            const preenchido = el("span");
            preenchido.style.width = Math.min(100, (p.quantidade / ESTOQUE_REFERENCIA) * 100) + "%";
            barra.append(preenchido);

            const linhaBarra = el("div", undefined, "estoque-barra");
            linhaBarra.append(barra, el("span", `${p.quantidade} un.`, "estoque-qtd"));
            li.append(linha, linhaBarra);
            if (baixo) {
                const selo = el("span", p.quantidade ? "Estoque baixo" : "Esgotado", "selo alerta");
                selo.style.marginTop = "10px";
                li.append(selo);
            }
            ul.append(li);
        }
        $("total-itens").textContent = estado.produtos.reduce((s, p) => s + p.quantidade, 0) + " itens";
    }

    // Depois de lançar, continua na mesma consulta (o reset do formulário voltaria à primeira).
    let ultimaConsulta = "";
    form.addEventListener("submit", () => { ultimaConsulta = selConsulta.value; });
    ligar(form, () => registrarConsumo(parseInt(selConsulta.value, 10), itens), (resultado) => {
        render(resultado);
        if ([...selConsulta.options].some((o) => o.value === ultimaConsulta)) selConsulta.value = ultimaConsulta;
    });

    render();

    const pedido = parametro("consulta");
    if (pedido && [...selConsulta.options].some((o) => o.value === pedido)) selConsulta.value = pedido;
    return render;
}

/* ---------- Inicialização ---------- */

// Cada página monta a tela e devolve a função que a redesenha com dados novos.
const paginas = {
    inicio: paginaInicio,
    agenda: paginaAgenda,
    consultas: paginaConsultas,
    farmacia: paginaFarmacia,
};

/** Sem dados do banco, a página mostra só o aviso e um botão para tentar de novo. */
function mostrarFalha(mensagem) {
    const container = document.querySelector("main .container");
    for (const filho of container.children) filho.hidden = true;
    const caixa = el("div", undefined, "vazio falha-conexao");
    caixa.setAttribute("role", "alert");
    const tentar = el("button", "Tentar novamente", "botao");
    tentar.type = "button";
    tentar.addEventListener("click", () => location.reload());
    caixa.append(el("p", "Não foi possível carregar os dados do banco de dados.", "falha-titulo"), el("p", mensagem), tentar);
    container.prepend(caixa);
}

async function iniciar() {
    document.querySelectorAll(".nav a").forEach((a) => {
        if (a.dataset.nav === document.body.dataset.page) a.setAttribute("aria-current", "page");
    });

    const botaoRedefinir = $("redefinir");
    if (botaoRedefinir) {
        botaoRedefinir.addEventListener("click", async () => {
            if (!confirm("Apagar todas as consultas, animais e tutores do banco de dados e restaurar o estoque da farmácia?")) return;
            const r = await aguardando(botaoRedefinir, redefinirDados);
            if (r.ok) location.reload();
            else avisar(r);
        });
    }

    const pagina = paginas[document.body.dataset.page];
    if (!pagina) return;
    try {
        await carregarEstado();
    } catch (e) {
        mostrarFalha(e.message);
        return;
    }
    const redesenhar = pagina();

    // Ao voltar para esta aba, busca de novo no banco: outra pessoa pode ter mudado algo.
    document.addEventListener("visibilitychange", async () => {
        if (document.visibilityState !== "visible") return;
        try {
            await carregarEstado();
            redesenhar();
        } catch (e) { /* mantém a tela como está; a próxima ação mostra o erro */ }
    });
}

iniciar();
