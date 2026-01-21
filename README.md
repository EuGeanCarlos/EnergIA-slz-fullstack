⚡ EnergIA SLZ — Gestão Inteligente de Energia para Microempresas

EnergIA SLZ é uma aplicação web focada em microempresas, criada para transformar dados simples de consumo elétrico em informação clara, acionável e mensurável.

O sistema permite cadastrar a empresa e seus equipamentos, calcular consumo mensal (kWh), custo estimado (R$) e emissões de CO₂, entregando um painel executivo que apoia decisões de economia de energia.

Projeto desenvolvido como trabalho final no programa Oracle Next Education (ONE), em parceria com EQT Lab / Capacitech, com foco em impacto real, clareza técnica e viabilidade prática.

🎯 O problema

Microempresas enfrentam dificuldades recorrentes com energia elétrica:

Contas de energia altas e imprevisíveis

Equipamentos ligados fora do horário ideal (desperdício invisível)

Falta de indicadores claros (kWh, custo e impacto ambiental)

Ausência de orientação prática e priorizada para redução de consumo

Na prática, o problema não é falta de tecnologia, mas falta de informação organizada e compreensível.

💡 A solução proposta

O EnergIA SLZ organiza dados simples e entrega valor real:

Cadastro do perfil da empresa (responsável, tarifa, número de colaboradores)

Cadastro de equipamentos elétricos (potência, horas de uso e quantidade)

Cálculo determinístico e transparente:

Consumo mensal total (kWh)

Custo mensal estimado (R$), baseado na tarifa informada

Estimativa de emissões de CO₂

Interface pensada para microempresas, com fluxo guiado e painel executivo

Nada de “chute” ou mágica: todos os números são rastreáveis.

🧠 Funcionalidades atuais
Backend (Java + Spring Boot + MongoDB)

CRUD de Empresa (Usuario)

CRUD de Equipamentos / Consumos, vinculados por usuarioId

Serviço de relatório que:

Soma o consumo mensal por equipamento

Calcula o custo total com base na tarifa da empresa

Retorna dados consolidados via RelatorioDTO

Frontend (HTML / CSS / JavaScript)

Fluxo em 3 etapas:

Cadastro da empresa

Cadastro dos equipamentos

Visualização do painel

Listagem dos equipamentos cadastrados

Painel com KPIs principais:

Consumo mensal (kWh)

Custo estimado (R$)

Emissões de CO₂ (kg)

🧱 Stack tecnológica

Java 17

Spring Boot 3.x

Spring Web (API REST)

Spring Data MongoDB

Bean Validation

Maven

MongoDB (local)

Frontend estático em HTML / CSS / JavaScript puro

O foco do projeto é clareza arquitetural e domínio dos fundamentos, não dependência de frameworks no frontend.

🗂 Estrutura do projeto
br.com.energia.energiaslz
├── controller/   → Endpoints REST
├── service/      → Regras de negócio e cálculos
├── repository/   → Repositórios MongoDB
├── model/        → Entidades (Usuario, Consumo)
├── dto/          → Objetos de transferência (RelatorioDTO)
└── static/       → Frontend (index.html, styles.css, script.js)

🔌 Endpoints principais
Status

GET /status
Retorna texto simples para verificação da aplicação.

Empresa (Usuario)

POST /api/usuarios

GET /api/usuarios

GET /api/usuarios/{id}

Consumos / Equipamentos

POST /api/consumos

POST /api/consumos/{usuarioId} (rota alternativa)

GET /api/consumos

GET /api/consumos/usuario/{usuarioId}

Relatório

GET /api/relatorios/{usuarioId}
Retorna o resumo consolidado (RelatorioDTO).

▶️ Como rodar o projeto localmente
1️⃣ Pré-requisitos

Java 17 instalado

Maven instalado

MongoDB rodando em localhost:27017

2️⃣ Subir o MongoDB

Você pode usar o serviço local ou Docker:

docker run --name mongo -p 27017:27017 -d mongo

3️⃣ Rodar a aplicação

Na raiz do projeto:

mvn spring-boot:run


A aplicação ficará disponível em:

http://localhost:8080

🚀 Próximos passos (planejados)

Integração com IA para geração de recomendações automáticas de economia

Sugestão de priorização de equipamentos com maior impacto

Histórico mensal de consumo

Exportação de relatórios

👨‍💻 Contexto educacional

Este projeto foi desenvolvido no contexto do Oracle Next Education (ONE), com apoio da Alura, EQT Lab e Capacitech, como projeto final de formação em desenvolvimento backend e aplicações web.

O foco é demonstrar:

Lógica de negócio

Organização de código

Clareza arquitetural

Capacidade de transformar um problema real em solução técnica
