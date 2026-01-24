// script.js

/*************************
 * ESTADO DA APLICAÇÃO
 *************************/
const estado = {
  usuario: null,
  aparelhos: [],
  relatorio: null
};

/*************************
 * CONFIG
 *************************/
const API = {
  statusJson: '/api/status',
  statusTexto: '/status',
  usuarios: '/api/usuarios',
  consumos: '/api/consumos',
  relatorios: '/api/relatorios',
  chat: '/api/chat'
};

/*************************
 * LOCAL STORAGE
 *************************/
const LS_KEY_USUARIO = 'energiaslz_usuario';

function salvarUsuarioLocal(usuario) {
  localStorage.setItem(LS_KEY_USUARIO, JSON.stringify(usuario));
}

function carregarUsuarioLocal() {
  try {
    const raw = localStorage.getItem(LS_KEY_USUARIO);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function limparUsuarioLocal() {
  localStorage.removeItem(LS_KEY_USUARIO);
}

/*************************
 * UTIL — ID DO USUÁRIO (BLINDADO)
 *************************/
function getUsuarioId() {
  if (!estado.usuario) return null;
  return estado.usuario.id || estado.usuario._id || estado.usuario.usuarioId || null;
}

/*************************
 * ELEMENTOS DO DOM
 *************************/
const telas = document.querySelectorAll('.tela');
const botoesNavegacao = document.querySelectorAll('.nav-btn');
const formUsuario = document.getElementById('formUsuario');
const formAparelho = document.getElementById('formAparelho');
const containerAparelhos = document.getElementById('aparelhos-container');
const btnGerarRelatorio = document.getElementById('btn-gerar-relatorio');
const notification = document.getElementById('notification');
const statusEl = document.getElementById('status'); // pode não existir (sem crash)
const statusPill = document.getElementById('status-pill');

// Painel
const diagnosticoStatusEl = document.getElementById('diagnostico-status');
const diagnosticoHelperEl = document.getElementById('diagnostico-helper');

// Chat
const chatMessagesEl = document.getElementById('chat-messages');
const chatTextEl = document.getElementById('chat-text');
const chatSendBtn = document.getElementById('chat-send');

/*************************
 * NOTIFICAÇÃO
 *************************/
function mostrarNotificacao(mensagem, tipo = 'sucesso') {
  notification.textContent = mensagem;
  notification.className = `notification ${tipo} show`;
  setTimeout(() => notification.classList.remove('show'), 3000);
}

/*************************
 * NAVEGAÇÃO
 *************************/
botoesNavegacao.forEach(botao => {
  botao.addEventListener('click', () => {
    const telaAlvo = botao.getAttribute('data-tela');

    botoesNavegacao.forEach(b => b.classList.remove('active'));
    botao.classList.add('active');

    telas.forEach(tela => {
      tela.classList.remove('ativa');
      if (tela.id === telaAlvo) tela.classList.add('ativa');
    });

    // ao entrar em aparelhos, tenta carregar
    if (telaAlvo === 'aparelhos') {
      if (!getUsuarioId()) {
        mostrarNotificacao('Cadastre um usuário primeiro.', 'erro');
        return;
      }
      carregarConsumosBackend().catch(err => {
        console.error(err);
        mostrarNotificacao(err.message || 'Erro ao carregar consumos', 'erro');
      });
    }

    // ao entrar no painel, tenta hidratar KPIs (se houver usuário)
    if (telaAlvo === 'relatorio') {
      if (getUsuarioId()) {
        gerarRelatorioBackend(false).catch(() => {});
      }
    }
  });
});

/*************************
 * BACKEND — USUÁRIO
 *************************/
async function salvarUsuarioBackend(usuario) {
  const response = await fetch(API.usuarios, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(usuario)
  });

  if (!response.ok) {
    const txt = await response.text().catch(() => '');
    throw new Error('Erro ao salvar usuário. ' + txt);
  }

  return response.json();
}

/*************************
 * BACKEND — CONSUMO
 *************************/
async function salvarConsumoBackend(aparelho) {
  const usuarioId = getUsuarioId();
  if (!usuarioId) throw new Error('Usuário não carregado. Cadastre o usuário.');

  const consumoDTO = {
    usuarioId,
    nomeAparelho: aparelho.nome,
    potencia: aparelho.potencia,
    horasUso: aparelho.horas_uso,
    quantidade: aparelho.quantidade
  };

  const response = await fetch(API.consumos, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(consumoDTO)
  });

  if (!response.ok) {
    const txt = await response.text().catch(() => '');
    throw new Error('Erro ao salvar consumo. ' + txt);
  }
}

async function carregarConsumosBackend() {
  const usuarioId = getUsuarioId();
  if (!usuarioId) return;

  const response = await fetch(API.consumos);
  if (!response.ok) {
    const txt = await response.text().catch(() => '');
    throw new Error('Erro ao carregar consumos. ' + txt);
  }

  const dados = await response.json();

  estado.aparelhos = (dados || [])
    .filter(c => c.usuarioId === usuarioId)
    .map(c => ({
      id: c.id || c._id,
      nome: c.nomeAparelho,
      potencia: c.potencia,
      horas_uso: c.horasUso,
      quantidade: c.quantidade
    }));

  renderizarAparelhos();
}

/*************************
 * CADASTRO DE USUÁRIO
 *************************/
formUsuario?.addEventListener('submit', async e => {
  e.preventDefault();

  const usuarioDTO = {
    nome: document.getElementById('nome').value,
    email: document.getElementById('email').value,
    telefone: document.getElementById('telefone').value,
    endereco: document.getElementById('endereco').value,
    tarifa: parseFloat(document.getElementById('tarifa').value),
    numResidentes: parseInt(document.getElementById('num_residentes').value)
  };

  try {
    const usuarioSalvo = await salvarUsuarioBackend(usuarioDTO);

    // garante que o estado guarda o que veio do backend
    estado.usuario = usuarioSalvo;
    salvarUsuarioLocal(usuarioSalvo);

    // valida id imediatamente
    if (!getUsuarioId()) {
      console.warn('Resposta do backend não trouxe id:', usuarioSalvo);
      mostrarNotificacao('Usuário salvo, mas sem ID retornado. Verifique o backend.', 'erro');
      return;
    }

    mostrarNotificacao('Usuário cadastrado!');
    document.querySelector('[data-tela="aparelhos"]')?.click();
  } catch (err) {
    console.error(err);
    mostrarNotificacao(err.message || 'Erro ao cadastrar usuário', 'erro');
  }
});

/*************************
 * CADASTRO DE APARELHO
 *************************/
formAparelho?.addEventListener('submit', async e => {
  e.preventDefault();

  const aparelho = {
    nome: document.getElementById('nome_aparelho').value,
    potencia: parseInt(document.getElementById('potencia').value),
    horas_uso: parseFloat(document.getElementById('horas_uso').value),
    quantidade: parseInt(document.getElementById('quantidade').value)
  };

  try {
    await salvarConsumoBackend(aparelho);
    await carregarConsumosBackend();
    mostrarNotificacao('Aparelho salvo!');
    formAparelho.reset();
  } catch (err) {
    console.error(err);
    mostrarNotificacao(err.message || 'Erro ao salvar aparelho', 'erro');
  }
});

/*************************
 * RENDERIZAÇÃO — APARELHOS
 *************************/
function renderizarAparelhos() {
  if (!containerAparelhos) return;
  containerAparelhos.innerHTML = '';

  if (estado.aparelhos.length === 0) {
    containerAparelhos.innerHTML = '<p>Nenhum aparelho cadastrado.</p>';
    return;
  }

  estado.aparelhos.forEach(a => {
    const consumoDiario = (a.potencia * a.horas_uso * a.quantidade) / 1000;
    containerAparelhos.innerHTML += `
      <div class="appliance-item">
        <strong>${escapeHtml(a.nome)}</strong>
        <p>${consumoDiario.toFixed(2)} kWh/dia</p>
      </div>
    `;
  });
}

/*************************
 * FETCH UTIL (corrige charset/bytes)
 *************************/
async function fetchJsonSmart(url, options = {}) {
  const res = await fetch(url, options);

  // tenta JSON direto
  const contentType = (res.headers.get('content-type') || '').toLowerCase();
  if (contentType.includes('application/json')) {
    const data = await res.json().catch(() => null);
    return { ok: res.ok, status: res.status, data, rawText: null };
  }

  // fallback: lê bytes e decodifica como utf-8
  const buf = await res.arrayBuffer().catch(() => null);
  let text = '';
  if (buf) {
    try {
      text = new TextDecoder('utf-8', { fatal: false }).decode(buf);
    } catch {
      text = await res.text().catch(() => '');
    }
  } else {
    text = await res.text().catch(() => '');
  }

  let data = null;
  try { data = JSON.parse(text); } catch { /* ignore */ }

  return { ok: res.ok, status: res.status, data, rawText: text };
}

/*************************
 * RELATÓRIO — BACKEND (KPIs)
 *************************/
btnGerarRelatorio?.addEventListener('click', async () => {
  try {
    await gerarRelatorioBackend(true);
  } catch (err) {
    console.error(err);
    mostrarNotificacao(err.message || 'Erro ao gerar relatório', 'erro');
  }
});

async function gerarRelatorioBackend(alsoChat = true) {
  const usuarioId = getUsuarioId();
  if (!usuarioId) {
    mostrarNotificacao('Cadastre um usuário primeiro.', 'erro');
    return;
  }

  // KPIs
  const r = await fetchJsonSmart(`${API.relatorios}/${usuarioId}`);
  if (!r.ok || !r.data) {
    throw new Error('Falha ao buscar relatório. ' + (r.rawText || ''));
  }

  const relatorio = r.data;
  const consumo = relatorio.consumoMensalKwh;

  if (typeof consumo !== 'number') {
    console.warn('Relatório inesperado:', relatorio);
    throw new Error('Relatório veio em formato inesperado.');
  }

  document.getElementById('consumo-mensal').innerText = consumo.toFixed(2) + ' kWh';
  document.getElementById('custo-mensal').innerText = 'R$ ' + Number(relatorio.custoEstimado).toFixed(2);
  document.getElementById('emissao-co2').innerText = (consumo * 0.084).toFixed(2) + ' kg';

  mostrarNotificacao('Relatório gerado!');

  // Chat (IA)
  if (alsoChat) {
    await gerarDiagnosticoChat("Gere um diagnóstico energético e 5 recomendações priorizadas.");
  }
}

/*************************
 * CHAT — BACKEND (/api/chat)
 *************************/
chatSendBtn?.addEventListener('click', async () => {
  const txt = (chatTextEl?.value || '').trim();
  if (!txt) return;
  chatTextEl.value = '';
  await gerarDiagnosticoChat(txt);
});

chatTextEl?.addEventListener('keydown', async (e) => {
  if (e.key === 'Enter') {
    e.preventDefault();
    chatSendBtn?.click();
  }
});

async function gerarDiagnosticoChat(mensagemUsuario) {
  const usuarioId = getUsuarioId();
  if (!usuarioId) {
    mostrarNotificacao('Cadastre um usuário primeiro.', 'erro');
    return;
  }

  // render user bubble
  chatAddMessage('user', mensagemUsuario);

  // loading bubble
  const loadingId = chatAddLoading();

  diagnosticoStatusEl && (diagnosticoStatusEl.textContent = 'Gerando…');
  diagnosticoHelperEl && (diagnosticoHelperEl.textContent = 'Consultando IA e aplicando regras determinísticas do backend.');

  const payload = { usuarioId, mensagem: mensagemUsuario };

  const r = await fetchJsonSmart(API.chat, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json; charset=utf-8' },
    body: JSON.stringify(payload)
  });

  chatRemoveLoading(loadingId);

  if (!r.ok || !r.data) {
    diagnosticoStatusEl && (diagnosticoStatusEl.textContent = 'Erro');
    throw new Error('Falha ao consultar /api/chat. ' + (r.rawText || ''));
  }

  const resp = r.data;

  // Algumas vezes seu backend coloca a "resposta bruta" no campo resposta
  // e recomendações vem preenchidas (fallback). Então a gente mostra:
  // 1) Texto principal (resposta)
  // 2) Lista de recomendações (sempre)
  const resposta = safeString(resp.resposta) || 'Diagnóstico gerado com base nos seus dados.';
  const recs = Array.isArray(resp.recomendacoes) ? resp.recomendacoes : [];

  // limpa “NÃ£o...” (já vem corrigido pelo browser, mas deixo seguro)
  chatAddMessage('assistant', resposta);

  if (recs.length > 0) {
    chatAddRecommendations(recs);
  }

  // status card
  diagnosticoStatusEl && (diagnosticoStatusEl.textContent = 'OK');
  diagnosticoHelperEl && (diagnosticoHelperEl.textContent = 'Diagnóstico atualizado. Veja abaixo as recomendações priorizadas.');

  // scroll bottom
  chatScrollBottom();
}

/*************************
 * CHAT UI
 *************************/
function chatEnsureNotEmpty() {
  if (!chatMessagesEl) return;
  const empty = chatMessagesEl.querySelector('.chat-empty');
  if (empty) empty.remove();
}

function chatScrollBottom() {
  if (!chatMessagesEl) return;
  chatMessagesEl.scrollTop = chatMessagesEl.scrollHeight;
}

function chatAddMessage(role, text) {
  if (!chatMessagesEl) return;
  chatEnsureNotEmpty();

  const wrapper = document.createElement('div');
  wrapper.className = `chat-row ${role === 'user' ? 'user' : 'assistant'}`;

  const bubble = document.createElement('div');
  bubble.className = `chat-bubble ${role === 'user' ? 'user' : 'assistant'}`;

  // suporta quebras de linha
  bubble.innerHTML = formatMultiline(escapeHtml(text || ''));

  wrapper.appendChild(bubble);
  chatMessagesEl.appendChild(wrapper);
  chatScrollBottom();
}

function chatAddRecommendations(recs) {
  if (!chatMessagesEl) return;
  chatEnsureNotEmpty();

  const wrapper = document.createElement('div');
  wrapper.className = 'chat-row assistant';

  const bubble = document.createElement('div');
  bubble.className = 'chat-bubble assistant';

  const list = document.createElement('div');
  list.className = 'chat-recs';

  const title = document.createElement('div');
  title.className = 'chat-recs-title';
  title.innerHTML = `<i class="fa-solid fa-list-check"></i> Recomendações priorizadas`;
  list.appendChild(title);

  recs.forEach((r, idx) => {
    const item = document.createElement('div');
    item.className = 'chat-rec-item';

    const h = document.createElement('div');
    h.className = 'chat-rec-head';
    h.innerHTML = `<span class="chat-rec-index">${idx + 1}</span> <strong>${escapeHtml(safeString(r.titulo) || 'Recomendação')}</strong>`;
    item.appendChild(h);

    const d = document.createElement('div');
    d.className = 'chat-rec-desc';
    d.innerHTML = formatMultiline(escapeHtml(safeString(r.descricao) || ''));
    item.appendChild(d);

    const imp = document.createElement('div');
    imp.className = 'chat-rec-impact';
    imp.innerHTML = `<i class="fa-solid fa-bolt"></i> ${escapeHtml(safeString(r.impacto) || '')}`;
    item.appendChild(imp);

    list.appendChild(item);
  });

  bubble.appendChild(list);
  wrapper.appendChild(bubble);
  chatMessagesEl.appendChild(wrapper);
  chatScrollBottom();
}

function chatAddLoading() {
  if (!chatMessagesEl) return null;
  chatEnsureNotEmpty();

  const id = `loading-${Date.now()}-${Math.random().toString(16).slice(2)}`;

  const wrapper = document.createElement('div');
  wrapper.className = 'chat-row assistant';
  wrapper.dataset.loadingId = id;

  const bubble = document.createElement('div');
  bubble.className = 'chat-bubble assistant';

  bubble.innerHTML = `
    <div class="chat-loading">
      <span class="dot"></span><span class="dot"></span><span class="dot"></span>
      <span class="chat-loading-text">Processando diagnóstico…</span>
    </div>
  `;

  wrapper.appendChild(bubble);
  chatMessagesEl.appendChild(wrapper);
  chatScrollBottom();
  return id;
}

function chatRemoveLoading(id) {
  if (!chatMessagesEl || !id) return;
  const el = chatMessagesEl.querySelector(`[data-loading-id="${id}"]`);
  if (el) el.remove();
}

/*************************
 * STATUS DA API (TENTA JSON, DEPOIS TEXTO)
 *************************/
async function atualizarStatusApi() {
  if (!statusEl) return;

  // 1) tenta /api/status (JSON)
  try {
    const res = await fetch(API.statusJson);
    if (res.ok) {
      const data = await res.json().catch(() => ({}));
      statusEl.innerText = data.mensagem || data.message || 'ok';
      updatePill();
      return;
    }
  } catch (e) {}

  // 2) fallback: /status (texto)
  try {
    const res2 = await fetch(API.statusTexto);
    if (res2.ok) {
      const txt = await res2.text();
      statusEl.innerText = txt || 'ok';
      updatePill();
      return;
    }
  } catch (e) {}

  statusEl.innerText = 'offline';
  updatePill();
}

function updatePill() {
  if (!statusPill || !statusEl) return;
  statusPill.classList.remove('ok', 'offline');
  statusPill.classList.add(statusEl.innerText === 'offline' ? 'offline' : 'ok');
}

/*************************
 * BOOTSTRAP
 *************************/
document.addEventListener('DOMContentLoaded', async () => {
  // reidrata usuário
  const usuarioSalvo = carregarUsuarioLocal();
  if (usuarioSalvo) estado.usuario = usuarioSalvo;

  // status
  await atualizarStatusApi();

  // se já tem usuário salvo, opcional:
  // await carregarConsumosBackend();
});

/*************************
 * HELPERS
 *************************/
function safeString(v) {
  return (v === null || v === undefined) ? '' : String(v);
}

function escapeHtml(str) {
  return safeString(str)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

function formatMultiline(escapedText) {
  // recebe texto já escapado
  return escapedText.replace(/\n/g, '<br/>');
}
