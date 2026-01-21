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
  relatorios: '/api/relatorios'
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
      carregarConsumosBackend();
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
formUsuario.addEventListener('submit', async e => {
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
    document.querySelector('[data-tela="aparelhos"]').click();
  } catch (err) {
    console.error(err);
    mostrarNotificacao(err.message || 'Erro ao cadastrar usuário', 'erro');
  }
});

/*************************
 * CADASTRO DE APARELHO
 *************************/
formAparelho.addEventListener('submit', async e => {
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
  containerAparelhos.innerHTML = '';

  if (estado.aparelhos.length === 0) {
    containerAparelhos.innerHTML = '<p>Nenhum aparelho cadastrado.</p>';
    return;
  }

  estado.aparelhos.forEach(a => {
    const consumoDiario = (a.potencia * a.horas_uso * a.quantidade) / 1000;
    containerAparelhos.innerHTML += `
      <div class="appliance-item">
        <strong>${a.nome}</strong>
        <p>${consumoDiario.toFixed(2)} kWh/dia</p>
      </div>
    `;
  });
}

/*************************
 * RELATÓRIO — BACKEND
 *************************/
btnGerarRelatorio.addEventListener('click', async () => {
  try {
    const usuarioId = getUsuarioId();
    if (!usuarioId) {
      mostrarNotificacao('Cadastre um usuário primeiro.', 'erro');
      return;
    }

    const response = await fetch(`${API.relatorios}/${usuarioId}`);
    if (!response.ok) {
      const txt = await response.text().catch(() => '');
      throw new Error('Falha ao buscar relatório. ' + txt);
    }

    const relatorio = await response.json();

    // backend retorna consumoMensalKwh
    const consumo = relatorio.consumoMensalKwh;

    if (typeof consumo !== 'number') {
      console.warn('Relatório inesperado:', relatorio);
      throw new Error('Relatório veio em formato inesperado.');
    }

    document.getElementById('consumo-mensal').innerText =
      consumo.toFixed(2) + ' kWh';

    document.getElementById('custo-mensal').innerText =
      'R$ ' + Number(relatorio.custoEstimado).toFixed(2);

    document.getElementById('emissao-co2').innerText =
      (consumo * 0.084).toFixed(2) + ' kg';

    mostrarNotificacao('Relatório gerado!');
  } catch (err) {
    console.error(err);
    mostrarNotificacao(err.message || 'Erro ao gerar relatório', 'erro');
  }
});

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
      return;
    }
  } catch (e) {
    // ignora e tenta fallback
  }

  // 2) fallback: /status (texto)
  try {
    const res2 = await fetch(API.statusTexto);
    if (res2.ok) {
      const txt = await res2.text();
      statusEl.innerText = txt || 'ok';
      return;
    }
  } catch (e) {
    // ignora
  }

  statusEl.innerText = 'offline';
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

  // se já tem usuário salvo, você pode carregar aparelhos automaticamente:
  // await carregarConsumosBackend();
});
