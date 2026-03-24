CREATE USER IF NOT EXISTS 'Nestor'@'%' IDENTIFIED BY 'Java2526';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, REFERENCES ON fastum.* TO 'Nestor'@'%';
FLUSH PRIVILEGES;

DROP DATABASE IF EXISTS fastum;
CREATE DATABASE fastum;
USE fastum;

CREATE TABLE restaurante(
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    morada VARCHAR(45) NOT NULL
);

CREATE TABLE posto (
    id VARCHAR(45) NOT NULL,
    nome VARCHAR(45) NOT NULL,
    restaurante VARCHAR(45) NOT NULL,
    PRIMARY KEY (id, restaurante),
    FOREIGN KEY (restaurante) REFERENCES restaurante (id) ON DELETE CASCADE
);

CREATE TABLE estado_pedido (
	id INT NOT NULL PRIMARY KEY,
	nome VARCHAR(45) NOT NULL
);

CREATE TABLE etapa (
	id INT NOT NULL PRIMARY KEY,
	nome VARCHAR(45) NOT NULL
);

CREATE TABLE etapas_posto (
    posto VARCHAR(45) NOT NULL,
    restaurante VARCHAR(45) NOT NULL,
    etapa INT NOT NULL,
    PRIMARY KEY (posto, restaurante, etapa),
    FOREIGN KEY ( etapa ) REFERENCES etapa ( id ) ON DELETE CASCADE,
    FOREIGN KEY (posto, restaurante) REFERENCES posto (id, restaurante) ON DELETE CASCADE
);

CREATE TABLE tipo_utilizador (
	id INT NOT NULL AUTO_INCREMENT,
	descricao VARCHAR(45) NOT NULL,
	PRIMARY KEY ( id )
);

ALTER TABLE tipo_utilizador AUTO_INCREMENT=0;

CREATE TABLE utilizador(
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    nome VARCHAR(45) NOT NULL,
    nif VARCHAR(9) NOT NULL,
    iban VARCHAR(34) NOT NULL,
    email VARCHAR(45) NOT NULL,
    telemovel VARCHAR(20) NOT NULL,
    salario DECIMAL(19,4) NOT NULL, 
    nascimento DATE NOT NULL,
    tipo INT,
    FOREIGN KEY (tipo) REFERENCES tipo_utilizador (id) ON DELETE SET NULL
);

CREATE TABLE coo(
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES utilizador (id) ON DELETE CASCADE
);

CREATE TABLE gerente(
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    restaurante VARCHAR(45),
    FOREIGN KEY (id) REFERENCES utilizador (id) ON DELETE CASCADE,
    FOREIGN KEY (restaurante) REFERENCES restaurante (id) ON DELETE SET NULL
);

CREATE TABLE funcionario (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    posto_id VARCHAR(45),
    restaurante_id VARCHAR(45),
    FOREIGN KEY (id) REFERENCES utilizador ( id ) ON DELETE CASCADE,
    FOREIGN KEY (posto_id, restaurante_id) REFERENCES posto (id, restaurante) ON DELETE SET NULL
);

CREATE TABLE item (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    nome VARCHAR(45) NOT NULL,
    preco DECIMAL(19,4) NOT NULL,
    desconto DECIMAL(19,4),
    tipo VARCHAR(10) NOT NULL,
    CONSTRAINT chkTipo CHECK (tipo IN ('Menu','Produto'))
);

CREATE TABLE itens_restaurante (
	restaurante VARCHAR(45) NOT NULL,
	item VARCHAR(45) NOT NULL,
	
	PRIMARY KEY (restaurante,item),
	FOREIGN KEY ( restaurante ) REFERENCES restaurante ( id ) ON DELETE CASCADE,
	FOREIGN KEY ( item ) REFERENCES item ( id ) ON DELETE CASCADE
);


CREATE TABLE menu (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES item (id) ON DELETE CASCADE	
);

CREATE TABLE produto (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES item (id) ON DELETE CASCADE
);

CREATE TABLE menu_produto (
    menu VARCHAR(45) NOT NULL,
    produto VARCHAR(45) NOT NULL,
    PRIMARY KEY (menu, produto),
    FOREIGN KEY (menu) REFERENCES menu (id) ON DELETE CASCADE,
    FOREIGN KEY (produto) REFERENCES produto (id) ON DELETE CASCADE
);

CREATE TABLE pedido (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    total DECIMAL(19,4) NOT NULL,
    hora_pedido DATETIME NOT NULL,
    hora_entrega DATETIME,
    estado INT NOT NULL,
    
    CONSTRAINT chk_estado_pedido CHECK (estado IN (0,1,2,3)),
    FOREIGN KEY ( estado ) REFERENCES estado_pedido ( id ) ON DELETE CASCADE
);

CREATE TABLE item_pedido (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    estado BOOLEAN NOT NULL,
    quantidade INT NOT NULL,
    pedido VARCHAR(45) NOT NULL,
    item_pedido_pai VARCHAR(45),
    item VARCHAR(45) NOT NULL,

    FOREIGN KEY (pedido) REFERENCES pedido (id) ON DELETE CASCADE,
    FOREIGN KEY (item_pedido_pai) REFERENCES item_pedido (id) ON DELETE CASCADE,
    FOREIGN KEY (item) REFERENCES item (id) ON DELETE CASCADE
);

CREATE TABLE tarefa (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    descricao VARCHAR(45) NOT NULL,
    grupo VARCHAR(45),
    opcional BOOLEAN NOT NULL,
    ajuste DECIMAL(19,4) NOT NULL,
    etapa INT NOT NULL,
    produto VARCHAR(45) NOT NULL,
    FOREIGN KEY ( etapa ) REFERENCES etapa ( id ) ON DELETE CASCADE,
    FOREIGN KEY (produto) REFERENCES produto (id) ON DELETE CASCADE
);


CREATE TABLE tarefas_por_instancia (
	posicao INT NOT NULL,
	item_pedido VARCHAR(45) NOT NULL,
	
	PRIMARY KEY ( posicao , item_pedido),
	FOREIGN KEY ( item_pedido ) REFERENCES item_pedido ( id ) ON DELETE CASCADE
);

CREATE TABLE tarefa_instancia_selecionada (
	
	posicao_instancia INT NOT NULL,
	item_pedido VARCHAR(45) NOT NULL,

	tarefa VARCHAR(45) NOT NULL,
	posicao_execucao INT NOT NULL,
	estado BOOLEAN NOT NULL,

	PRIMARY KEY ( posicao_instancia, item_pedido ,posicao_execucao ),
	FOREIGN KEY ( tarefa ) REFERENCES tarefa ( id ) ON DELETE CASCADE,
	FOREIGN KEY ( posicao_instancia,item_pedido ) REFERENCES tarefas_por_instancia ( posicao,item_pedido ) ON DELETE CASCADE
);

CREATE TABLE tarefas_alternativas (
    principal VARCHAR(45) NOT NULL,
    alternativa VARCHAR(45) NOT NULL,
    PRIMARY KEY (principal, alternativa),
    FOREIGN KEY (principal) REFERENCES tarefa (id) ON DELETE CASCADE,
    FOREIGN KEY (alternativa) REFERENCES tarefa (id) ON DELETE CASCADE
);

CREATE TABLE stock (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    restaurante VARCHAR(45) NOT NULL,
    FOREIGN KEY (restaurante) REFERENCES restaurante (id) ON DELETE CASCADE
);

CREATE TABLE ingrediente (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    nome VARCHAR(45) NOT NULL,
    preco DECIMAL(19,4) NOT NULL	
);

CREATE TABLE stock_ingrediente (
    stock VARCHAR(45) NOT NULL,
    ingrediente VARCHAR(45) NOT NULL,
    quantidade INT NOT NULL,
    PRIMARY KEY (stock, ingrediente),
    FOREIGN KEY (stock) REFERENCES stock (id) ON DELETE CASCADE,
    FOREIGN KEY (ingrediente) REFERENCES ingrediente (id) ON DELETE CASCADE
);

CREATE TABLE tarefa_ingrediente (
    tarefa VARCHAR(45) NOT NULL,
    ingrediente VARCHAR(45) NOT NULL,
    quantidade INT NOT NULL,
    PRIMARY KEY (tarefa, ingrediente),
    FOREIGN KEY (tarefa) REFERENCES tarefa (id) ON DELETE CASCADE,
    FOREIGN KEY (ingrediente) REFERENCES ingrediente (id) ON DELETE CASCADE
);
