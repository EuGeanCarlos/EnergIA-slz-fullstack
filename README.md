# ⚡ EnergIA SLZ — Inteligência Energética para Microempresas

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white)
![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=F7DF1E)
![Google Gemini](https://img.shields.io/badge/Google%20Gemini-8E75C2?style=for-the-badge&logo=googlegemini&logoColor=white)
![Maven](https://img.shields.io/badge/apache_maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

> **Status do Projeto:** 🚀 Em desenvolvimento / Funcional

O **EnergIA SLZ** é uma solução Full Stack que une o rigor do cálculo matemático à flexibilidade da IA Generativa. O sistema ajuda microempresas a transformarem contas de luz complexas em diagnósticos claros e planos de ação priorizados.

---

## 🎯 O Desafio vs. 💡 A Solução

| Problema (Dor do Cliente) | Solução EnergIA SLZ |
| --- | --- |
| Contas elevadas e imprevisíveis | Cálculo determinístico de consumo real |
| Falta de indicadores (kWh, CO₂) | Painel executivo com KPIs automáticos |
| Dificuldade em priorizar ações | Diagnóstico via **Google Gemini API** |
| Decisões baseadas em intuição | Recomendações baseadas em dados estruturados |

---

## 🧠 Arquitetura e Fluxo de Dados

A arquitetura separa a **lógica de negócio (Java)** da **geração de insights (IA)**, garantindo que os valores financeiros sejam sempre precisos.

### Fluxo da Aplicação

1. **Input:** Usuário cadastra empresa e equipamentos.
2. **Processamento:** O Backend calcula as métricas base:
    $consumoMensalKwh = \frac{potencia \times horasUso \times quantidade \times 30}{1000}$ $custoEstimado = consumoMensalKwh \times tarifa$
3. **Inteligência:** O Backend envia os dados estruturados para o **Gemini**, que retorna um JSON com o plano de ação.
4. **Output:** Frontend renderiza os KPIs e o diagnóstico.

---

## 🛠️ Stack Tecnológica

### **Backend & Base**

* **Linguagem:** Java 17
* **Framework:** Spring Boot 3.x (Web, Data MongoDB, Validation)
* **Banco de Dados:** MongoDB (NoSQL)
* **Gerenciador de Dependências:** Maven

### **Inteligência Artificial**

* **Modelo:** Google Gemini API
* **Features:** Sanitização de JSON, Fallback determinístico e tratamento de respostas.

### **Frontend**

* HTML5, CSS3 e JavaScript Vanilla (Fetch API)

---

## 📂 Estrutura do Projeto

```text
br.com.energia.energiaslz
├── controller/    # Endpoints REST
├── service/       # Lógica de negócio e Integração Gemini
├── repository/    # Persistência MongoDB
├── model/         # Entidades (Usuario, Consumo)
├── dto/           # Objetos de transferência de dados
└── resources/
    └── static/    # Frontend (Interface B2B)

```

---

## 🌐 API Endpoints (Resumo)

### Empresas & Consumo

* `POST /api/usuarios` - Cadastro de nova empresa.
* `POST /api/consumos` - Registro de equipamentos.
* `GET /api/relatorios/{usuarioId}` - Relatório matemático puro.

### Inteligência

* `POST /api/chat` - Solicita o diagnóstico contextual à IA.

<details>
<summary><b>Clique para ver um exemplo de resposta da IA (JSON)</b></summary>

```json
{
  "resposta": "Diagnóstico indica que a geladeira é o principal consumidor.",
  "impacto": {
    "economiaPercentual": 8,
    "economiaMensalReais": 21.60,
    "co2EvitadoKgMes": 2.1
  }
}

```

</details>

---

## 🚀 Como Executar

1. **Clonar o repositório:**
```bash
git clone [https://github.com/EuGeanCarlos/EnergIA-slz-fullstack.git]

```


2. **Configurar Variável de Ambiente:**
* **Linux/macOS:** `export GEMINI_API_KEY="SUA_CHAVE"`
* **Windows:** `setx GEMINI_API_KEY "SUA_CHAVE"`


3. **Rodar:**
```bash
mvn spring-boot:run

```


Acesse: `http://localhost:8080`

---

## 👨‍💻 Autor

**Gean Carlos** - [LinkedIn](https://www.linkedin.com/in/gean-carlos-a9903a220/)  
*Desenvolvedor Full Stack em formação com foco em APIs e IA.*

---

