# EnergIA SLZ — Gestão de Energia para Microempresas

**EnergIA SLZ** é uma aplicação web que ajuda microempresas a entenderem e reduzirem seus custos com energia elétrica.  
O sistema cadastra a empresa e seus equipamentos, calcula consumo mensal (kWh), custo estimado (R$) e emissões de CO₂, e entrega um **painel executivo** com indicadores e plano de ação.

> Projeto desenvolvido para apresentação e demo em contexto de competição/avaliação (ONE + EQT Lab).

---

## Problema (microempresas)
Microempresas geralmente enfrentam:
- Conta de energia alta e imprevisível  
- Equipamentos ligados fora do horário (desperdício invisível)
- Falta de indicadores claros (kWh, custo e impacto ambiental)
- Pouca orientação prática de economia com priorização

---

## Solução (o que o sistema entrega)
- Cadastro do perfil da empresa (responsável, tarifa, colaboradores e dados de contexto)
- Cadastro de equipamentos (potência, horas/dia, quantidade)
- **Cálculo determinístico (sem “chute”)**:
  - Consumo mensal total (kWh)
  - Custo mensal estimado (R$) com base na **tarifa da empresa**
  - Estimativa de CO₂
- Interface reposicionada para **microempresas** com fluxo em etapas e painel executivo

---

## Funcionalidades atuais
### Backend (Java + Spring Boot + MongoDB)
- CRUD básico de **Empresa (Usuario)**  
- CRUD de **Consumo/Equipamentos** com vínculo por `usuarioId`
- Relatório por empresa:
  - soma do consumo mensal por equipamento
  - custo calculado usando tarifa informada no cadastro

### Frontend (HTML/CSS/JS)
- Fluxo em 3 etapas: **Empresa → Equipamentos → Painel**
- Lista de equipamentos cadastrados
- Painel com KPIs:
  - consumo mensal (kWh)
  - custo estimado (R$)
  - CO₂ estimado (kg)

---

## Stack
- Java 17
- Spring Boot 3.x
- Spring Web (REST)
- Spring Data MongoDB
- Bean Validation
- Maven
- MongoDB local
- Front: HTML/CSS/JS puro em `src/main/resources/static/`

---

## Estrutura do projeto
`br.com.energia.energiaslz`
- `controller/` — endpoints REST
- `service/` — regras e cálculos
- `repository/` — Mongo repositories
- `model/` — entidades (Usuario, Consumo)
- `dto/` — objetos de resposta (RelatorioDTO)
- `static/` — front (index.html, styles.css, script.js)

---

## Endpoints principais
### Status
- `GET /status` → texto simples

### Empresa (Usuario)
- `POST /api/usuarios`
- `GET /api/usuarios`
- `GET /api/usuarios/{id}`

### Consumos / Equipamentos
- `POST /api/consumos` (salva consumo com `usuarioId` no body)
- `POST /api/consumos/{usuarioId}` (rota alternativa)
- `GET /api/consumos`
- `GET /api/consumos/usuario/{usuarioId}`

### Relatório
- `GET /api/relatorios/{usuarioId}` → retorna `RelatorioDTO`

---

## Como rodar localmente
### 1) Pré-requisitos
- Java 17 instalado
- Maven instalado (ou use wrapper, se existir)
- MongoDB rodando localmente em `localhost:27017`

### 2) Rodar MongoDB
Exemplo (dependendo do seu ambiente):
- Serviço do Mongo ligado
- Ou via Docker:
  ```bash
  docker run --name mongo -p 27017:27017 -d mongo
