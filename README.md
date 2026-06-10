# AWS Manager — Backend ☁️

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![AWS SDK v2](https://img.shields.io/badge/AWS%20SDK%20v2-2.21.0-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)](https://aws.amazon.com/sdk-for-java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Container-2496ed?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

API REST robusta, segura e de alta performance desenvolvida em **Java 17** e **Spring Boot 3** para atuar como o núcleo do sistema AWS Manager. Este backend fornece toda a lógica de negócio e integração necessária para a administração simplificada de recursos na nuvem da Amazon Web Services (AWS), especificamente focado em **Amazon S3** e **Amazon EC2**.

---

## 📌 Sobre o Projeto

O **AWS Manager Backend** atua como a interface de comunicação (API) que abstrai a complexidade do AWS SDK para a aplicação cliente (Frontend em Vue.js). Ele fornece endpoints simplificados para operações cotidianas, como upload/download de arquivos no S3 e controle de ciclo de vida de servidores virtuais EC2. O projeto também inclui um banco de dados relacional para auditoria, histórico e metadados.

---

## 🚀 Funcionalidades

### 📦 Integração Amazon S3 (Armazenamento)
*   **Gestão de Arquivos**: Endpoints para listagem paginada, upload de objetos limitados até 100MB e deleção segura.
*   **Múltiplos Buckets**: Suporte à listagem de buckets S3 disponíveis.
*   **URLs Pré-assinadas**: Geração de URLs seguras (*Presigned URLs*) com tempo de expiração para possibilitar que os clientes façam download direto e seguro pela AWS.
*   **Estatísticas**: Cálculo de espaço utilizado e contagem de itens em um bucket.

### ⚙️ Integração Amazon EC2 (Servidores)
*   **Monitoramento e Listagem**: Listagem das instâncias ativas com detalhes como ID, status e tipo de máquina.
*   **Controle de Ciclo de Vida**: Acionamento seguro de operações como **Iniciar (Start)**, **Parar (Stop)** e **Reiniciar (Reboot)** de instâncias através de rotas POST dedicadas.
*   **Auditoria Integrada**: Registro automático de logs de todas as ações executadas nas instâncias, salvas no banco de dados para rastreabilidade.

### 🛡️ Core e Arquitetura
*   **Tratamento de Exceções Global**: Respostas de API padronizadas utilizando o formato customizado `ApiResponse` em cenários de sucesso e falhas (Exception Handlers).
*   **Monitoramento Ativo (Actuator)**: Endpoint de `/actuator/health` habilitado para checagem de saúde da aplicação e integração com orquestradores de infraestrutura.
*   **Integração LocalStack**: Configurações dinâmicas prontas para rodar simulações offline através do **LocalStack**.

---

## 🛠️ Tecnologias Utilizadas

*   **Java 17**: LTS atual utilizada com recursos modernos e otimização de performance.
*   **Spring Boot 3.2.0**: Framework principal contemplando os módulos `Web`, `Data JPA`, `Validation` e `Actuator`.
*   **AWS SDK for Java v2**: Biblioteca oficial da Amazon para integração em tempo real com serviços da nuvem (S3 e EC2), otimizada para ser reativa e performática.
*   **PostgreSQL**: Banco de dados relacional robusto para persistência dos logs de auditoria e informações persistentes.
*   **MapStruct & Lombok**: Ferramentas adotadas para automação na criação de DTOs, mapeamento de objetos e redução expressiva de código repetitivo (boilerplates).
*   **Jackson**: Bibliotecas eficientes para serialização e desserialização de JSON.
*   **Maven**: Automação e gerenciamento flexível de dependências e build do ciclo de vida.

---

## 💻 Como Executar o Projeto Localmente

### Pré-requisitos
*   **Java 17+**: (OpenJDK ou Oracle JDK).
*   **Apache Maven**: Gerenciador de dependências.
*   **PostgreSQL**: Instância rodando localmente na porta `5432` (ou via Docker).
*   **Acesso AWS ou LocalStack**: Credenciais de acesso configuradas OU a stack do [LocalStack](https://localstack.cloud/) executando (simulador AWS). O projeto já vem pré-configurado para o LocalStack.

### Passo a Passo

1.  **Clonar o repositório e entrar na pasta:**
    ```bash
    git clone <url-do-repositorio>
    cd aws-manager-backend
    ```

2.  **Configurar banco de dados:**
    Garanta que há uma database disponível com o nome configurado em `application.yml` (por padrão: `awsmanager`).

3.  **Compilar o projeto via Maven:**
    ```bash
    mvn clean install
    ```

4.  **Executar o servidor Spring Boot:**
    ```bash
    mvn spring-boot:run
    ```
    O servidor iniciará automaticamente na porta **`8080`**. O prefixo padrão das APIs é `/api`.

---

## 🐳 Deploy & Containerização (Docker)

O projeto está totalmente preparado para ser executado em ambientes de produção conteinerizados via **Docker**.

### Como buildar e rodar o container

1.  **Construir a imagem Docker:**
    ```bash
    docker build -t aws-manager-backend .
    ```

2.  **Executar o container:**
    ```bash
    docker run -d -p 8080:8080 \
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://seu-db-host:5432/awsmanager \
      -e SPRING_DATASOURCE_USERNAME=awsuser \
      -e SPRING_DATASOURCE_PASSWORD=awspass123 \
      --name aws-backend aws-manager-backend
    ```
    A aplicação estará acessível na porta **`8080`** da máquina hospedeira.

---

## ⚙️ Variáveis de Ambiente e Configuração

As configurações principais da aplicação estão no arquivo `src/main/resources/application.yml`. O projeto suporta as seguintes variáveis de ambiente essenciais para a execução:

### Banco de Dados
- `SPRING_DATASOURCE_URL` (Padrão: `jdbc:postgresql://localhost:5432/awsmanager`)
- `SPRING_DATASOURCE_USERNAME` (Padrão: `awsuser`)
- `SPRING_DATASOURCE_PASSWORD` (Padrão: `awspass123`)

### Integração com AWS / LocalStack
- `AWS_ACCESS_KEY_ID` (Padrão: `test`)
- `AWS_SECRET_ACCESS_KEY` (Padrão: `test`)
- `AWS_REGION` (Padrão: `us-east-1`)
- `AWS_S3_ENDPOINT` (Padrão: `http://localhost:4566` - LocalStack)
- `S3_BUCKET_NAME` (Padrão: `aws-manager-files`)
- `AWS_EC2_ENDPOINT` (Padrão: `http://localhost:4566` - LocalStack)

### Aplicação
- `APP_ENV` (Padrão: `development`)
- `SERVER_PORT` (Padrão: `8080`)

---

## 📡 Endpoints Principais (API)

A documentação base dos principais endpoints disponíveis. O caminho base para todos eles é `http://localhost:8080/api`.

### Armazenamento de Arquivos (`/s3`)
- `GET /s3/buckets` - Lista todos os buckets disponíveis.
- `GET /s3/files` - Lista arquivos de um bucket (suporta `?page=X&size=Y`).
- `POST /s3/files/upload` - Envio multipart-form com a chave `file`.
- `GET /s3/files/presigned-url` - Gera URL temporária baseada em `objectKey`.
- `DELETE /s3/files` - Exclui o arquivo identificado por `objectKey`.
- `GET /s3/stats` - Retorna a contagem total de objetos e tamanho.

### Servidores Virtuais (`/ec2`)
- `GET /ec2/instances` - Traz o consolidado das instâncias EC2 da conta.
- `GET /ec2/instances/{instanceId}` - Detalhes estendidos sobre a instância.
- `POST /ec2/instances/{instanceId}/start` - Dá partida na instância.
- `POST /ec2/instances/{instanceId}/stop` - Realiza a parada da instância.
- `POST /ec2/instances/{instanceId}/reboot` - Solicita reinicialização.
- `GET /ec2/stats` - Resumo contábil dos estados (Running, Stopped).
- `GET /ec2/logs` - Busca o histórico paginado de ações gravado no banco de dados.

### Monitoramento (`/actuator`)
- `GET /actuator/health` - Verifica o status da aplicação, conectividade com o banco e métricas de sistema operacional.

---

## 📁 Estrutura de Diretórios (Backend)

Uma visão geral da arquitetura de pacotes (dentro de `src/main/java/com/awsmanager/`):

```text
com.awsmanager/
├── config/         # Configurações globais (CORS, Beans S3/EC2, Interceptors)
├── controller/     # Exposição dos endpoints REST (S3Controller, Ec2Controller)
├── dto/            # Data Transfer Objects (Requests e Responses, ApiResponse)
├── exception/      # Tratamento de exceções e handlers globais (@ControllerAdvice)
├── model/          # Entidades persistentes do Banco de Dados (JPA) e MapStruct
├── service/        # Regras de negócio centrais integradas com AWS SDK
└── AwsManagerApplication.java # Bootstrap da aplicação Spring Boot
```
