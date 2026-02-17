⚡ EnergIA SLZ — Inteligência Energética para Microempresas

EnergIA SLZ é uma aplicação web full stack desenvolvida em Java + Spring Boot + MongoDB + JavaScript, com integração a IA generativa (Google Gemini) para geração de diagnósticos energéticos automatizados.

O sistema permite que microempresas entendam seu consumo elétrico, estimem custos reais com base na tarifa cadastrada e recebam recomendações priorizadas para redução de despesas operacionais.

Projeto arquitetado com foco em backend estruturado, API REST bem definida e separação clara entre cálculo determinístico e inteligência artificial.

🎯 Problema

Microempresas frequentemente enfrentam:

Conta de energia elevada e imprevisível

Falta de controle sobre equipamentos e tempo de uso

Ausência de indicadores claros (kWh, custo, CO₂)

Dificuldade em priorizar ações de economia

Decisões baseadas em intuição, não em dados

💡 Solução

O EnergIA SLZ combina:

Cálculo determinístico confiável

Análise contextual via IA

Painel executivo orientado a decisão

A aplicação realiza:

Cadastro da empresa (tarifa personalizada)

Cadastro de equipamentos (potência, horas/dia, quantidade)

Cálculo automático de:

Consumo mensal (kWh)

Custo mensal estimado (R$)

Emissão estimada de CO₂

Geração de diagnóstico inteligente com 5 recomendações priorizadas

Exibição em painel executivo com KPIs

🧠 Arquitetura da Solução
Separação de Responsabilidades

O projeto foi estruturado em camadas claras:

Controller → Service → Repository → MongoDB
↘ IA (Gemini API)

🔹 Backend (Spring Boot)

Responsável por:

Persistência de dados

Cálculos determinísticos

Orquestração da IA

Normalização e validação das respostas

🔹 IA Generativa (Google Gemini)

Responsável por:

Interpretar dados de consumo

Gerar diagnóstico textual

Criar recomendações contextualizadas

Estimar impacto (quando possível)

Importante:
Os valores financeiros e de consumo não são inventados pela IA.
Eles são forçados a partir do cálculo backend (regra de negócio).

🔹 Frontend (HTML + CSS + JS puro)

Responsável por:

Fluxo guiado em 3 etapas

Consumo da API REST

Renderização do painel executivo

Interface estilo B2B

🧩 Fluxo da Aplicação

Usuário cadastra empresa

Cadastra equipamentos

Backend calcula:

consumoMensalKwh = (potencia × horasUso × quantidade × 30) / 1000
custoEstimado = consumoMensalKwh × tarifa


Backend envia dados estruturados para a IA

IA retorna JSON estruturado

Backend valida, sanitiza e normaliza resposta

Frontend renderiza KPIs + plano de ação

🏗️ Stack Tecnológica
Backend

Java 17

Spring Boot 3.x

Spring Web (REST)

Spring Data MongoDB

Bean Validation

Jackson (serialização JSON)

Maven

RestClient (integração externa)

Banco de Dados

MongoDB (NoSQL)

Modelagem baseada em documentos

Relacionamento via usuarioId

IA

Google Gemini API

Sanitização de resposta JSON

Tratamento de respostas incompletas

Fallback determinístico

Frontend

HTML5

CSS3 (design corporativo)

JavaScript Vanilla

Fetch API

📂 Estrutura do Projeto
br.com.energia.energiaslz
├── controller/       → Endpoints REST
├── service/
│   ├── UsuarioService
│   ├── ConsumoService
│   ├── RelatorioService
│   └── ia/
│       ├── ChatService
│       └── GeminiClient
├── repository/       → Mongo repositories
├── model/            → Entidades (Usuario, Consumo)
├── dto/              → DTOs de request/response
└── resources/
    └── static/       → Frontend (index.html, styles.css, script.js)

🌐 Endpoints Principais
Status
GET /status
GET /api/status

Empresa
POST /api/usuarios
GET  /api/usuarios
GET  /api/usuarios/{id}

Equipamentos
POST /api/consumos
GET  /api/consumos
GET  /api/consumos/usuario/{usuarioId}

Relatório Determinístico
GET /api/relatorios/{usuarioId}

Chat com IA
POST /api/chat


Body:

{
  "usuarioId": "id_da_empresa",
  "mensagem": "Gere um diagnóstico energético"
}

🧪 Exemplo de Resposta da IA
{
  "resposta": "Diagnóstico indica que a geladeira é o principal consumidor.",
  "recomendacoes": [
    { "titulo": "Otimização da Geladeira", "descricao": "...", "impacto": "..." }
  ],
  "impacto": {
    "economiaPercentual": 8,
    "economiaMensalReais": 21.60,
    "economiaAnualReais": 259.20,
    "co2EvitadoKgMes": 2.1
  },
  "relatorio": {
    "consumoMensalKwh": 360.0,
    "custoEstimado": 270.0
  }
}

🚀 Como Rodar Localmente
Pré-requisitos

Java 17

Maven

MongoDB rodando em localhost:27017

Variável de ambiente:

Windows:

setx GEMINI_API_KEY "SUA_CHAVE"


Linux/macOS:

export GEMINI_API_KEY="SUA_CHAVE"

Rodar aplicação
mvn spring-boot:run


Acesse:

http://localhost:8080

🧠 Diferenciais Técnicos

✔ Separação entre cálculo determinístico e IA
✔ Sanitização de JSON retornado pela IA
✔ Tratamento de respostas truncadas
✔ Fallback automático para recomendações padrão
✔ Arquitetura limpa e extensível
✔ API REST estruturada
✔ Projeto orientado a contexto real de negócio

📌 Possíveis Evoluções

Dashboard com gráficos (Chart.js)

Autenticação JWT

Multi-empresa por usuário

Deploy em nuvem (Railway / Render / AWS)

Banco Mongo Atlas

Histórico de relatórios

Exportação PDF executiva

👨‍💻 Autor

Gean Carlos
Desenvolvedor Full Stack em formação
Foco em Backend, APIs REST e Integração com IA
