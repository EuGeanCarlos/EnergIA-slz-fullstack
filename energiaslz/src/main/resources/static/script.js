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
 * NAVEGAÇÃO ENTRE TELAS
 *************************/
botoesNavegacao.forEach(botao => {
  botao.addEventListener('click', () => {
    const telaAlvo = botao.getAttribute('data-tela');

    botoesNavegacao.forEach(b => b.classList.remove('active'));
    botao.classList.add('active');

    telas.forEach(tela => {
      tela.classList.remove('ativa');
      if (tela.id === telaAlvo) {
        tela.classList.add('ativa');
      }
    });

    if (telaAlvo === 'aparelhos') {
      carregarConsumosBackend();
    }
  });
});

/*************************
 * NOTIFICAÇÕES
 *************************/
function mostrarNotificacao(mensagem, tipo = 'sucesso') {
  notification.textContent = mensagem;
  notification.className = `notification ${tipo} show`;

  setTimeout(() => {
    notification.classList.remove('show');
  }, 3000);
}

/*************************
 * BACKEND — CONSUMO
 *************************/
async function salvarConsumoBackend(aparelho) {
  const consumoDTO = {
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

  if (!response.ok) {
    throw new Error('Erro ao salvar consumo');
  }
}

async function carregarConsumosBackend() {
  try {
    const response = await fetch('/api/consumos');
    const dados = await response.json();

    estado.aparelhos = dados.map(d => ({
      id: d.id,
      nome: d.nomeAparelho,
      potencia: d.potencia,
      horas_uso: d.horasUso,
      quantidade: d.quantidade
    }));

    renderizarAparelhos();
  } catch (error) {
    console.error(error);
    mostrarNotificacao('Erro ao carregar aparelhos', 'erro');
  }
}

/*************************
 * CADASTRO DE USUÁRIO
 *************************/
formUsuario.addEventListener('submit', (e) => {
  e.preventDefault();

  estado.usuario = {
    nome: document.getElementById('nome').value,
    email: document.getElementById('email').value,
    telefone: document.getElementById('telefone').value,
    endereco: document.getElementById('endereco').value,
    tarifa: parseFloat(document.getElementById('tarifa').value),
    num_residentes: parseInt(document.getElementById('num_residentes').value),
    data_cadastro: new Date().toISOString()
  };

  mostrarNotificacao('Usuário cadastrado com sucesso!');
  document.querySelector('[data-tela="aparelhos"]').click();
});

/*************************
 * CADASTRO DE APARELHOS
 *************************/
formAparelho.addEventListener('submit', async (e) => {
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

    mostrarNotificacao('Aparelho salvo com sucesso!');
    formAparelho.reset();
  } catch (error) {
    console.error(error);
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

  estado.aparelhos.forEach(aparelho => {
    const consumoDiario =
      (aparelho.potencia * aparelho.horas_uso * aparelho.quantidade) / 1000;

    const div = document.createElement('div');
    div.className = 'appliance-item';
    div.innerHTML = `
      <div class="appliance-header">
        <div class="appliance-name">${aparelho.nome}</div>
      </div>
      <p>Potência: ${aparelho.potencia} W</p>
      <p>Uso diário: ${aparelho.horas_uso} h</p>
      <p>Quantidade: ${aparelho.quantidade}</p>
      <p><strong>Consumo diário:</strong> ${consumoDiario.toFixed(2)} kWh</p>
    `;

    containerAparelhos.appendChild(div);
  });
}

/*************************
 * RELATÓRIO (LOCAL)
 *************************/
btnGerarRelatorio.addEventListener('click', () => {
  if (!estado.usuario || estado.aparelhos.length === 0) {
    mostrarNotificacao('Cadastre usuário e aparelhos primeiro!', 'erro');
    return;
  }

  const consumoTotal = estado.aparelhos.reduce((total, a) => {
    return total + (a.potencia * a.horas_uso * a.quantidade * 30) / 1000;
  }, 0);

  const custo = consumoTotal * estado.usuario.tarifa;

  document.getElementById('consumo-mensal').innerText =
    consumoTotal.toFixed(2) + ' kWh';
  document.getElementById('custo-mensal').innerText =
    'R$ ' + custo.toFixed(2);
  document.getElementById('emissao-co2').innerText =
    (consumoTotal * 0.084).toFixed(2) + ' kg';

  mostrarNotificacao('Relatório gerado!');
});

/*************************
 * STATUS DA API + CARGA INICIAL
 *************************/
document.addEventListener('DOMContentLoaded', async () => {
  try {
    const response = await fetch('/api/status');
    const data = await response.json();
    const statusEl = document.getElementById('status');
    if (statusEl) statusEl.innerText = data.mensagem;
  } catch {
    console.warn('API status indisponível');
  }

  carregarConsumosBackend();
});
