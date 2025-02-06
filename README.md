[JAVA_BADGE]:https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white
[SPRING_BADGE]: https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white
[MYSQL_BADGE]:https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white
[DOCKER_BADGE]:https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white
[APACHE_MAVEN_BADGE]:https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white
[SWAGGER_BADGE]:https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white


<h1 align="center" style="font-weight: bold;">Back-end Gerenciador de Tarefas </h1>

<div style="text-align: center;">

  ![Java][JAVA_BADGE]
  ![Spring][SPRING_BADGE]
  ![MySQL][MYSQL_BADGE]
  ![Docker][DOCKER_BADGE]
  ![Apache Maven][APACHE_MAVEN_BADGE]
  ![Swagger][SWAGGER_BADGE]

</div>
<p align="center">
  <a href="#started">Como utilizar</a> • 
  <a href="#routes">Alguns Endpoints</a> •
</p>

<p align="center">
  <b>API REST criada com o intuito de consolidar meus estudos recentes</b>
</p>

<h2>💻 O que você encontrará?</h2>
 
* Java
* Spring Boot
* MySQL
* Autenticação e Segurança com Spring Security.  
* Mapeamento ORM com Spring Data JPA  
* Testes Unitários com JUnit e Mockito  
* Testes de Integração com RestAssured e TestContainers 
* Flyway
* Paginação e adição de links com Spring HATEOAS  
* Docker  
* Padrão MVC
* DTOs
* Exception Handler
* Documentação com Swagger

<h2>💡 O que foi feito?</h2>

Desenvolvi o back-end de um sistema de Gerenciamento de Tarefas utilizando Java e Spring Boot. Implementei autenticação e segurança por meio de um controle de acessos baseado em cargos (roles), garantindo que os usuários tenham permissões específicas para os recursos disponíveis. A aplicação conecta-se a um banco de dados MySQL, proporcionando persistência segura e eficiente dos dados. 

Entre as funcionalidades implementadas estão a criação, edição, remoção e pesquisa de tarefas, subtarefas e tags. Adicionei a possibilidade de realizar buscas personalizadas, com base em parâmetros fornecidos pelo usuário. A API foi containerizada com Docker, permitindo a execução da aplicação e do banco de dados em containers distintos. 

Além disso, implementei paginação e links utilizando Spring HATEOAS para uma navegação mais intuitiva entre os recursos da API. Realizei testes automatizados (unitários e de integração) para garantir a robustez e a qualidade do código. Por fim, documentei a API utilizando Swagger, facilitando a integração e uso por outros desenvolvedores. 
 
<h2 id="started">🚀 Como utilizar?</h2>

### OBS
   - Não criei um arquivo .env e deixei as variáveis de ambiente expostas de propósito, com o intuito de facilitar para quem está clonando o projeto.

###  Pré-requisitos

1. **Instalar Docker Desktop**  
   Baixe e instale o Docker Desktop de acordo com o seu sistema operacional:  

   - [Docker Desktop para Windows/Mac](https://www.docker.com/products/docker-desktop)
   - 
2. **Verifique a instalação do Docker**  
   Após a instalação, execute os seguintes comandos no terminal para garantir que o Docker está instalado corretamente:
   ```bash
   docker --version
   docker-compose --version
   ```
   Isso deve exibir as versões instaladas do Docker e Docker Compose.

3. **Baixar o Postman**  
   Para facilitar o teste e a interação com os endpoints da API, é recomendado baixar o Postman. O Postman é uma ferramenta popular para testar APIs REST, permitindo enviar requisições HTTP, visualizar respostas e organizar suas chamadas de API de maneira eficiente.

   Além disso, a API implementa autenticação e segurança, o que significa que você precisará fornecer tokens de acesso para interagir com os endpoints. Esses processos de autenticação não podem ser realizados diretamente por um navegador. O Postman facilita esse processo, permitindo que você configure facilmente os cabeçalhos e parâmetros necessários para autenticar suas requisições.
   - [Postman](https://www.postman.com/downloads/)


### 🛠️ Como rodar a aplicação

1. **Navegue até o diretório onde você deseja clonar o projeto.**  

2. **Abra o Terminal (ou o Git Bash, se estiver no Windows).**    

3. **Clonar o repositório**  
   Clone o projeto do GitHub:
   ```bash
   git clone https://github.com/Victor-Marins-Dev/Gerenciador-de-Tarefas.git
   ```
    Se estiver usando SSH:
    ```bash
   git clone git@github.com:Victor-Marins-Dev/Gerenciador-de-Tarefas.git
   ```

4. **Ir para o diretório do projeto**  
   Navegue até a pasta do projeto clonado:
   ```bash
   cd Gerenciador-de-Tarefas
   ```
5. **Construir e rodar os contêineres**  
   No diretório do projeto, execute o comando abaixo para construir as imagens e iniciar os containers com o Docker Compose:
   ```bash
   docker-compose up --build
   ```
    O `--build` garante que as imagens sejam construídas a partir do Dockerfile.
    Se a imagem já foi construída e você não quiser reconstruí-la, pode rodar apenas:
    ```bash
    docker-compose up
    ```
6. **Verificar se tudo está rodando**  
   Após o comando acima, o Docker Compose deve iniciar todos os containers definidos no arquivo `docker-compose.yml`. Você pode verificar se os containers estão rodando corretamente com:
    ```bash
    docker ps
    ```
    Caso só apareça o container do banco de dados, vá até o docker hub e suba o container da aplicação novamente.
7. **Acessar a documentação**  
   Feita a verificação que os containers estão rodando perfeitamente, basta abrir o navegador e acessar a URL da documentação: [http://localhost:8085/swagger-ui/index.html](http://localhost:8085/swagger-ui/index.html).

8. **Utilizando a aplicação**  
    Por fim basta executar o Postman e fazer as requisoções http de acordo com a documentação.

<h2 id="started"> Overview dos Endpoints </h2>

![](images/img1.jpg)
![](images/img2.jpg)
![](images/img3.jpg)
![](images/img4.jpg)

<h2 id="routes">📍 Alguns Endpoints da API</h2>

### Autenticação

**Endpoint:** `POST /api/auth/login`

***REQUEST***
```json
{
  "username" : "victor",
  "password" : "1234567aZ#"
}
```

***RESPONSE***
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJhdXRoLWFwaSIsInN1YiI6InZpY3RvciIsImV4cCI6MTcyNzQwNTAzN30.mIXYswRCpKSt-MmQ8C0n-OhGkAPSyOU7nyAkVOnmMWI"
}
```

### Criando uma task

**Endpoint:** `POST /api/tasks`

***REQUEST***
```json
{
    "title": "Task 1",
    "description": "Task 1 description"
}
```

***RESPONSE***
```json
{
    "id": 1,
    "title": "Task 1",
    "description": "Task 1 description",
    "status": "UNDONE",
    "priority": "NONE",
    "createdDate": "2024-09-26",
    "dueDate": null,
    "tags": [],
    "subtasks": [],
    "_links": {
        "self": {
            "href": "http://localhost:8080/api/tasks/1"
        },
        "update": {
            "href": "http://localhost:8080/api/tasks/1"
        },
        "delete": {
            "href": "http://localhost:8080/api/tasks/1"
        },
        "findAllTasks": {
            "href": "http://localhost:8080/api/tasks{?page,size}",
            "templated": true
        },
        "addSubtask": {
            "href": "http://localhost:8080/api/subtasks/1"
        },
        "addTag": {
            "href": "http://localhost:8080/api/tags/add/1/{tagId}",
            "templated": true
        }
    }
}
```

### Criando uma Subtask

**Endpoint:** `POST /api/subtasks/{taskId}`

***REQUEST***
```json
{
    "title": "Subtask 1 title",
    "description": "xD"
}
```

***RESPONSE***
```json
{
    "id": 1,
    "title": "Task 1",
    "description": "Task 1 description",
    "status": "UNDONE",
    "priority": "NONE",
    "createdDate": "2024-09-26",
    "dueDate": null,
    "tags": [],
    "subtasks": [
        {
            "id": 1,
            "title": "Subtask 1 title",
            "description": "xD",
            "createdDate": "2024-09-26",
            "dueDate": null,
            "status": "UNDONE"
        }
    ],
    "_links": {
        "self": {
            "href": "http://localhost:8080/api/tasks/1"
        },
        "update": {
            "href": "http://localhost:8080/api/tasks/1"
        },
        "delete": {
            "href": "http://localhost:8080/api/tasks/1"
        },
        "findAllTasks": {
            "href": "http://localhost:8080/api/tasks{?page,size}",
            "templated": true
        },
        "addSubtask": {
            "href": "http://localhost:8080/api/subtasks/1"
        },
        "addTag": {
            "href": "http://localhost:8080/api/tags/add/1/{tagId}",
            "templated": true
        }
    }
}
```

### Procurando todas as tasks de um usuário

**Endpoint:** `GET /api/tasks`

***REQUEST***

 Não tem body, somente o token deve ser fornecido no header.

***RESPONSE***
```json
{
    "_embedded": {
        "taskResponseList": [
            {
                "id": 1,
                "title": "Task 1",
                "description": "Task 1 description",
                "status": "UNDONE",
                "priority": "NONE",
                "createdDate": "2024-09-26",
                "dueDate": null,
                "tags": [],
                "subtasks": [
                    {
                        "id": 1,
                        "title": "Subtask 1 title",
                        "description": "xD",
                        "createdDate": "2024-09-26",
                        "dueDate": null,
                        "status": "UNDONE"
                    }
                ],
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/api/tasks/1"
                    }
                }
            },
            {
                "id": 2,
                "title": "Task 2",
                "description": null,
                "status": "UNDONE",
                "priority": "HIGH",
                "createdDate": "2024-09-26",
                "dueDate": null,
                "tags": [],
                "subtasks": [],
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/api/tasks/2"
                    }
                }
            },
            {
                "id": 3,
                "title": "Task 3",
                "description": null,
                "status": "DONE",
                "priority": "MEDIUM",
                "createdDate": "2024-09-26",
                "dueDate": null,
                "tags": [],
                "subtasks": [],
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/api/tasks/3"
                    }
                }
            },
            {
                "id": 4,
                "title": "Task 4",
                "description": null,
                "status": "DONE",
                "priority": "LOW",
                "createdDate": "2024-09-26",
                "dueDate": null,
                "tags": [],
                "subtasks": [],
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/api/tasks/4"
                    }
                }
            },
            {
                "id": 5,
                "title": "Task 5",
                "description": null,
                "status": "UNDONE",
                "priority": "LOW",
                "createdDate": "2024-09-26",
                "dueDate": null,
                "tags": [],
                "subtasks": [],
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/api/tasks/5"
                    }
                }
            }
        ]
    },
    "_links": {
        "first": {
            "href": "http://localhost:8080/api/tasks?page=0&size=5&sort=id,asc"
        },
        "self": {
            "href": "http://localhost:8080/api/tasks?page=0&size=5&sort=id,asc"
        },
        "next": {
            "href": "http://localhost:8080/api/tasks?page=1&size=5&sort=id,asc"
        },
        "last": {
            "href": "http://localhost:8080/api/tasks?page=1&size=5&sort=id,asc"
        }
    },
    "page": {
        "size": 5,
        "totalElements": 7,
        "totalPages": 2,
        "number": 0
    }
}
```
### Busca personalizada

**Endpoint:** `GET /api/tasks/search?status={status}&priority={priority}&tagName={tagName}`

***REQUEST***  
- **status**: `DONE`
- **priority**:`LOW`
- **tagName**: Não fornecido


***RESPONSE***
```json
{
    "_embedded": {
        "taskResponseList": [
            {
                "id": 4,
                "title": "Task 4",
                "description": null,
                "status": "DONE",
                "priority": "LOW",
                "createdDate": "2024-09-26",
                "dueDate": null,
                "tags": [],
                "subtasks": [],
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/api/tasks/4"
                    }
                }
            }
        ]
    },
    "_links": {
        "self": {
            "href": "http://localhost:8080/api/tasks/search?status=DONE&priority=LOW&page=0&size=5&sort=id,asc"
        }
    },
    "page": {
        "size": 5,
        "totalElements": 1,
        "totalPages": 1,
        "number": 0
    }
}
```

**Esses foram alguns endpoints, você pode conferir o resto na documentação do swagger!**

