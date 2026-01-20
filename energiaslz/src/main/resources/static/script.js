/*************************
 * ESTADO DA APLICAÇÃO
 *************************/
const estado = {
  usuario: null,
  aparelhos: [],
  relatorio: null
};

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

    if (telaAlvo === 'aparelhos' && estado.usuario) {
      carregarConsumosBackend();
    }
  });
});

/*************************
 * NOTIFICAÇÃO
 *************************/
function mostrarNotificacao(mensagem, tipo = 'sucesso') {
  notification.textContent = mensagem;
  notification.className = `notification ${tipo} show`;
  setTimeout(() => notification.classList.remove('show'), 3000);
}

/*************************
 * BACKEND — USUÁRIO
 *************************/
async function salvarUsuarioBackend(usuario) {
  const response = await fetch('/api/usuarios', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(usuario)
  });

  if (!response.ok) throw new Error('Erro ao salvar usuário');

  return response.json(); // RETORNA COM ID
}

/*************************
 * BACKEND — CONSUMO
 *************************/
async function salvarConsumoBackend(aparelho) {
  const consumoDTO = {
    usuarioId: estado.usuario.id, // 🔑 VÍNCULO CRÍTICO
    nomeAparelho: aparelho.nome,
    potencia: aparelho.potencia,
    horasUso: aparelho.horas_uso,
    quantidade: aparelho.quantidade
  };

  const response = await fetch('/api/consumos', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(consumoDTO)
  });

  if (!response.ok) throw new Error('Erro ao salvar consumo');
}

async function carregarConsumosBackend() {
  const response = await fetch('/api/consumos');
  const dados = await response.json();

  estado.aparelhos = dados
    .filter(c => c.usuarioId === estado.usuario.id)
    .map(c => ({
      id: c.id,
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
    estado.usuario = await salvarUsuarioBackend(usuarioDTO);
    mostrarNotificacao('Usuário cadastrado!');
    document.querySelector('[data-tela="aparelhos"]').click();
  } catch {
    mostrarNotificacao('Erro ao cadastrar usuário', 'erro');
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
  } catch {
    mostrarNotificacao('Erro ao salvar aparelho', 'erro');
  }
});

/*************************
 * RENDERIZAÇÃO
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
 * RELATÓRIO — BACKEND REAL
 *************************/
btnGerarRelatorio.addEventListener('click', async () => {
  try {
    const response = await fetch(`/api/relatorios/${estado.usuario.id}`);
    const relatorio = await response.json();

    document.getElementById('consumo-mensal').innerText =
      relatorio.consumoMensal.toFixed(2) + ' kWh';

    document.getElementById('custo-mensal').innerText =
      'R$ ' + relatorio.custoEstimado.toFixed(2);

    document.getElementById('emissao-co2').innerText =
      (relatorio.consumoMensal * 0.084).toFixed(2) + ' kg';

    mostrarNotificacao('Relatório gerado!');
  } catch {
    mostrarNotificacao('Erro ao gerar relatório', 'erro');
  }
});

/*************************
 * STATUS DA API
 *************************/
document.addEventListener('DOMContentLoaded', async () => {
  try {
    const res = await fetch('/api/status');
    const data = await res.json();
    document.getElementById('status').innerText = data.mensagem;
  } catch {
    console.warn('API offline');
  }
});
